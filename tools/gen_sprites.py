#!/usr/bin/env python3
"""
SPACE SHOOTER — NEON SPRITE GENERATOR
=====================================
Generates a coherent "modern neon arcade" sprite set for the game.

Why a generator instead of downloading a pack:
  - Guarantees CLEAN alpha (no ghost pixels / horizontal stretch artifacts
    that the previous generated sprites suffered from).
  - Consistent canvas, consistent visual scale, consistent neon style.
  - Ships face UP (nose at top) — the game code flips enemies 180° to face
    the player.

Method:
  - Every sprite is rendered at 4x supersampling, then downsampled with a
    high-quality Lanczos filter. This yields smooth anti-aliased edges and
    a hard, clean alpha mask (no bleeding streaks).
  - Colors are stored as neon palette values; the hull + glow layers are
    composited with additive glow.

All output files are written next to the script's sibling assets/ folder
(resolved relative to the repo root). Run:
    python3 tools/gen_sprites.py
"""

import os
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
ASSETS = os.path.join(REPO_ROOT, "assets")
SS = 4  # supersample factor


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
def canvas(size, color=(0, 0, 0, 0)):
    """A supersampled RGBA canvas of `size` logical px."""
    return np.zeros((size * SS, size * SS, 4), dtype=np.float32)


def blit_pil(img_np, draw_fn, w, h):
    """Apply `draw_fn(img_draw)` on a supersampled PIL surface, bake into array."""
    pil = Image.new("RGBA", (w * SS, h * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(pil)
    draw_fn(d, w * SS, h * SS)
    arr = np.array(pil, dtype=np.float32) / 255.0
    img_np[...] = np.maximum(img_np, arr)
    return img_np


def finalize(img_np, size, filename, add_glow=True, glow_radius=0.06):
    """Downsample supersampled buffer to final size and save PNG."""
    img = np.clip(img_np, 0, 1)
    # convert to uint8 via float -> 8bit with rounding
    img8 = (img * 255.0 + 0.5).astype(np.uint8)
    pil = Image.fromarray(img8, "RGBA")
    # Downsample with Lanczos -> clean edges
    pil = pil.resize((size, size), Image.LANCZOS)

    if add_glow:
        pil = add_neon_glow(pil, radius=max(2, int(size * glow_radius)))

    out = os.path.join(ASSETS, filename)
    pil.save(out, "PNG", optimize=True)
    print("  wrote %-24s %dx%d" % (filename, size, size))


def finalize_nonsquare(img_np, w, h, filename, add_glow=True):
    """Downsample a supersampled non-square buffer (width=w, height=h)."""
    img = np.clip(img_np, 0, 1)
    img8 = (img * 255.0 + 0.5).astype(np.uint8)
    pil = Image.fromarray(img8, "RGBA").resize((w, h), Image.LANCZOS)
    if add_glow:
        pil = add_neon_glow(pil, radius=max(2, int(h * 0.04)))
    out = os.path.join(ASSETS, filename)
    pil.save(out, "PNG", optimize=True)
    print("  wrote %-24s %dx%d" % (filename, w, h))


def add_neon_glow(pil, radius=4, intensity=1.0):
    """Expand the sprite's alpha outward and tint it, creating a neon halo."""
    alpha = pil.split()[3]
    rgb = pil.convert("RGB")

    # Blurred copy of the alpha -> soft halo
    halo_alpha = alpha.filter(ImageFilter.GaussianBlur(radius))
    # scale up halo brightness
    halo_alpha = halo_alpha.point(lambda v: min(255, int(v * intensity)))

    glow_rgb = rgb.filter(ImageFilter.GaussianBlur(radius))

    # Composite: halo behind the crisp sprite
    base = Image.new("RGBA", pil.size, (0, 0, 0, 0))
    base.paste(glow_rgb, (0, 0), halo_alpha)
    base = Image.alpha_composite(base, pil)
    return base


def lerp(a, b, t):
    return tuple(a[i] + (b[i] - a[i]) * t for i in range(3))


def with_alpha(c, a):
    return (int(c[0]), int(c[1]), int(c[2]), int(a * 255))


def shape_points(cx, cy, w, h, pts):
    """Normalized polygon points (x,y in 0..1) -> absolute pixel coords."""
    return [(cx + (px - 0.5) * w, cy + (py - 0.5) * h) for (px, py) in pts]


def draw_poly(d, points, fill, outline=None, width=2):
    d.polygon(points, fill=fill, outline=outline)
    if outline:
        d.line(points + [points[0]], fill=outline, width=width)


# ---------------------------------------------------------------------------
# Ship primitive (parametric) — draws a stylized top-down fighter
# ---------------------------------------------------------------------------
def draw_fighter(d, W, H, hull, accent, cockpit, engine, nose_angle=1.0,
                 wings=("swept",), wing_extent=0.42, body_width=0.30,
                 tail=True, cockpit_size=0.16):
    """Draw a top-down neon fighter facing UP.

    Parameters are normalized (0..1) within the square canvas of W x H px.
    """
    cx, cy = W / 2, H / 2
    w = W * 0.86  # usable drawing area

    # --- engine glow (bottom) -------------------------------------
    eg = cy + w * 0.40
    d.ellipse([cx - w * 0.10, eg - w * 0.02, cx + w * 0.10, eg + w * 0.20],
              fill=with_alpha(engine, 0.9))

    # --- wings -----------------------------------------------------
    if "swept" in wings:
        wing_tip_y = cy - w * 0.28          # swept back -> tips toward nose
        wing_base_y = cy + w * 0.30
        wing_tip_x = w * wing_extent
        # right wing (mirrored left)
        pts_r = [(cx, wing_base_y), (cx + w * 0.34, wing_base_y + w * 0.08),
                 (cx + wing_tip_x, wing_tip_y - w * 0.12), (cx + w * 0.16, wing_tip_y + w * 0.16)]
        pts_l = [(cx, wing_base_y), (cx - w * 0.34, wing_base_y + w * 0.08),
                 (cx - wing_tip_x, wing_tip_y - w * 0.12), (cx - w * 0.16, wing_tip_y + w * 0.16)]
        draw_poly(d, pts_r, fill=with_alpha(hull, 0.96), outline=with_alpha(accent, 0.9), width=int(W * 0.012))
        draw_poly(d, pts_l, fill=with_alpha(hull, 0.96), outline=with_alpha(accent, 0.9), width=int(W * 0.012))

    # --- main fuselage ---------------------------------------------
    nose = cy - w * 0.46 * nose_angle
    tail_y = cy + w * 0.42
    half_bw = w * body_width / 2
    fuse = [(cx - half_bw * 0.75, nose + w * 0.02),
            (cx + half_bw * 0.75, nose + w * 0.02),
            (cx + half_bw, tail_y - w * 0.06),
            (cx + half_bw * 0.4, tail_y + w * 0.05),
            (cx - half_bw * 0.4, tail_y + w * 0.05),
            (cx - half_bw, tail_y - w * 0.06)]
    draw_poly(d, fuse, fill=with_alpha(hull, 1.0), outline=with_alpha(accent, 1.0), width=int(W * 0.014))

    # center spine highlight
    spine = [(cx - half_bw * 0.22, nose + w * 0.10),
             (cx + half_bw * 0.22, nose + w * 0.10),
             (cx + half_bw * 0.10, tail_y - w * 0.10),
             (cx - half_bw * 0.10, tail_y - w * 0.10)]
    draw_poly(d, spine, fill=with_alpha(lerp(hull, (255, 255, 255), 0.35), 0.85))

    # --- cockpit ----------------------------------------------------
    cw = w * cockpit_size
    d.ellipse([cx - cw / 2, nose + w * 0.10, cx + cw / 2, nose + w * 0.10 + cw],
              fill=with_alpha(cockpit, 1.0), outline=with_alpha((255, 255, 255), 0.8), width=int(W * 0.008))
    # cockpit inner glow
    cw2 = cw * 0.5
    d.ellipse([cx - cw2 / 2, nose + w * 0.12, cx + cw2 / 2, nose + w * 0.12 + cw2],
              fill=with_alpha((255, 255, 255), 0.9))

    # --- wingtip accent lights -------------------------------------
    if "swept" in wings:
        for s in (1, -1):
            tipx = cx + s * w * wing_extent
            tipy = wing_tip_y - w * 0.10
            d.ellipse([tipx - w * 0.035, tipy - w * 0.035, tipx + w * 0.035, tipy + w * 0.035],
                      fill=with_alpha(accent, 1.0))
            d.ellipse([tipx - w * 0.012, tipy - w * 0.012, tipx + w * 0.012, tipy + w * 0.012],
                      fill=(255, 255, 255, 255))

    # --- tail fins --------------------------------------------------
    if tail:
        fin_r = [(cx + half_bw * 0.15, tail_y - w * 0.02),
                 (cx + half_bw * 0.6, tail_y - w * 0.02),
                 (cx + half_bw * 0.28, tail_y + w * 0.14)]
        fin_l = [(cx - half_bw * 0.15, tail_y - w * 0.02),
                 (cx - half_bw * 0.6, tail_y - w * 0.02),
                 (cx - half_bw * 0.28, tail_y + w * 0.14)]
        draw_poly(d, fin_r, fill=with_alpha(hull, 0.96), outline=with_alpha(accent, 0.7), width=int(W * 0.01))
        draw_poly(d, fin_l, fill=with_alpha(hull, 0.96), outline=with_alpha(accent, 0.7), width=int(W * 0.01))

    # engine exhaust point
    d.ellipse([cx - half_bw * 0.16, tail_y + w * 0.04, cx + half_bw * 0.16, tail_y + w * 0.16],
              fill=with_alpha(engine, 0.95))


def draw_wide_ship(d, W, H, hull, accent, cockpit, engine, horns=0):
    """A bulkier, wide-bodied ship (tank / dragoon style)."""
    cx, cy = W / 2, H / 2
    w = W * 0.88
    half_bw = w * 0.26

    # wide body
    body = [(cx - half_bw * 1.6, cy - w * 0.18),
            (cx - half_bw, cy - w * 0.40),
            (cx - half_bw * 0.3, cy - w * 0.42),
            (cx + half_bw * 0.3, cy - w * 0.42),
            (cx + half_bw, cy - w * 0.40),
            (cx + half_bw * 1.6, cy - w * 0.18),
            (cx + half_bw * 1.25, cy + w * 0.30),
            (cx + half_bw * 0.6, cy + w * 0.42),
            (cx - half_bw * 0.6, cy + w * 0.42),
            (cx - half_bw * 1.25, cy + w * 0.30)]
    draw_poly(d, body, fill=with_alpha(hull, 1.0), outline=with_alpha(accent, 1.0), width=int(W * 0.014))

    # nose spike
    nose = [(cx - half_bw * 0.22, cy - w * 0.42),
            (cx + half_bw * 0.22, cy - w * 0.42),
            (cx, cy - w * 0.52)]
    draw_poly(d, nose, fill=with_alpha(lerp(hull, (255, 255, 255), 0.2), 1.0),
              outline=with_alpha(accent, 1.0), width=int(W * 0.01))

    # cockpit
    cw = w * 0.20
    d.ellipse([cx - cw / 2, cy - w * 0.30, cx + cw / 2, cy - w * 0.30 + cw],
              fill=with_alpha(cockpit, 1.0), outline=with_alpha((255, 255, 255), 0.8), width=int(W * 0.008))

    # armor plating lines
    for side in (1, -1):
        d.line([cx + side * half_bw * 1.1, cy - w * 0.05,
                cx + side * half_bw * 0.9, cy + w * 0.22],
               fill=with_alpha(accent, 0.7), width=int(W * 0.012))
        # side engine pods
        podx = cx + side * half_bw * 1.35
        d.ellipse([podx - w * 0.06, cy + w * 0.24, podx + w * 0.06, cy + w * 0.34],
                  fill=with_alpha(engine, 1.0))

    # engine glow
    eg = cy + w * 0.42
    d.ellipse([cx - w * 0.09, eg, cx + w * 0.09, eg + w * 0.16], fill=with_alpha(engine, 0.9))

    # horns (tank cannons)
    if horns:
        for side in (1, -1):
            hx = cx + side * half_bw * 1.5
            d.rectangle([hx - w * 0.02, cy - w * 0.46, hx + w * 0.02, cy - w * 0.26],
                        fill=with_alpha(accent, 0.95))
            d.ellipse([hx - w * 0.03, cy - w * 0.50, hx + w * 0.03, cy - w * 0.44],
                      fill=with_alpha((255, 255, 255), 0.9))


def draw_sniper(d, W, H, hull, accent, cockpit, engine):
    """A thin needle-like ship (sniper)."""
    cx, cy = W / 2, H / 2
    w = W * 0.9
    half_bw = w * 0.085  # very thin

    # long needle body
    body = [(cx - half_bw, cy - w * 0.48),
            (cx + half_bw, cy - w * 0.48),
            (cx + half_bw * 1.4, cy + w * 0.42),
            (cx - half_bw * 1.4, cy + w * 0.42)]
    draw_poly(d, body, fill=with_alpha(hull, 1.0), outline=with_alpha(accent, 1.0), width=int(W * 0.012))

    # small stub wings
    for s in (1, -1):
        wing = [(cx + s * half_bw, cy - w * 0.10),
                (cx + s * half_bw * 3.2, cy - w * 0.02),
                (cx + s * half_bw * 3.2, cy + w * 0.04),
                (cx + s * half_bw, cy + w * 0.10)]
        draw_poly(d, wing, fill=with_alpha(hull, 0.95), outline=with_alpha(accent, 0.8), width=int(W * 0.008))
        tipx = cx + s * half_bw * 3.2
        d.ellipse([tipx - w * 0.015, cy - w * 0.02, tipx + w * 0.015, cy + w * 0.02],
                  fill=with_alpha(accent, 1.0))

    # cockpit
    cw = w * 0.10
    d.ellipse([cx - cw / 2, cy - w * 0.34, cx + cw / 2, cy - w * 0.34 + cw],
              fill=with_alpha(cockpit, 1.0), outline=with_alpha((255, 255, 255), 0.8), width=int(W * 0.006))

    # engine
    d.ellipse([cx - half_bw, cy + w * 0.38, cx + half_bw, cy + w * 0.46],
              fill=with_alpha(engine, 1.0))


# ---------------------------------------------------------------------------
# Item / projectile drawing
# ---------------------------------------------------------------------------
def draw_crystal(d, W, H, color):
    """A faceted crystal gem (currency)."""
    cx, cy = W / 2, H / 2
    w = W * 0.7
    pts = [(cx, cy - w * 0.5),
           (cx + w * 0.28, cy - w * 0.15),
           (cx + w * 0.16, cy + w * 0.42),
           (cx, cy + w * 0.55),
           (cx - w * 0.16, cy + w * 0.42),
           (cx - w * 0.28, cy - w * 0.15)]
    draw_poly(d, pts, fill=with_alpha(color, 1.0), outline=with_alpha((255, 255, 255), 0.9), width=int(W * 0.02))
    # facets
    d.line([cx, cy - w * 0.5, cx, cy + w * 0.55], fill=with_alpha((255, 255, 255), 0.7), width=int(W * 0.015))
    d.line([cx, cy - w * 0.5, cx - w * 0.28, cy - w * 0.15], fill=with_alpha((255, 255, 255), 0.4), width=int(W * 0.012))
    d.line([cx, cy - w * 0.5, cx + w * 0.28, cy - w * 0.15], fill=with_alpha((255, 255, 255), 0.4), width=int(W * 0.012))
    # bright top facet
    d.polygon([(cx, cy - w * 0.5), (cx + w * 0.28, cy - w * 0.15), (cx, cy - w * 0.15)],
              fill=with_alpha(lerp(color, (255, 255, 255), 0.5), 0.9))


def draw_med_cross(d, W, H, color):
    """Health pickup: rounded ring + cross."""
    cx, cy = W / 2, H / 2
    w = W * 0.62
    ro = w * 0.5
    d.ellipse([cx - ro, cy - ro, cx + ro, cy + ro], outline=with_alpha(color, 1.0), width=int(W * 0.06))
    # cross
    arm = w * 0.30
    thk = w * 0.16
    d.rectangle([cx - thk / 2, cy - arm, cx + thk / 2, cy + arm], fill=with_alpha(color, 1.0))
    d.rectangle([cx - arm, cy - thk / 2, cx + arm, cy + thk / 2], fill=with_alpha(color, 1.0))
    # white highlight
    d.rectangle([cx - thk / 6, cy - arm * 0.7, cx + thk / 6, cy + arm * 0.7], fill=(255, 255, 255, 200))


def draw_upgrade(d, W, H, color):
    """Weapon upgrade pickup: bolt / lightning in a ring."""
    cx, cy = W / 2, H / 2
    w = W * 0.62
    ro = w * 0.5
    d.ellipse([cx - ro, cy - ro, cx + ro, cy + ro], outline=with_alpha(color, 1.0), width=int(W * 0.055))
    # lightning bolt
    bw = w * 0.16
    bolt = [(cx + bw * 0.6, cy - w * 0.42),
            (cx - bw * 0.5, cy + w * 0.02),
            (cx - bw * 0.05, cy + w * 0.02),
            (cx - bw * 0.6, cy + w * 0.42),
            (cx + bw * 0.5, cy - w * 0.02),
            (cx + bw * 0.05, cy - w * 0.02)]
    draw_poly(d, bolt, fill=with_alpha(color, 1.0), outline=with_alpha((255, 255, 255), 0.8), width=int(W * 0.012))


def draw_beam(d, W, H, color, core_color):
    """Vertical laser beam (tall canvas, W=height ratio handled by caller)."""
    cx, cy = W / 2, H / 2
    bw = W * 0.30
    d.rectangle([cx - bw, 0, cx + bw, H], fill=with_alpha(color, 0.85))
    d.rectangle([cx - bw * 0.35, 0, cx + bw * 0.35, H], fill=with_alpha(core_color, 1.0))
    d.rectangle([cx - bw * 0.12, 0, cx + bw * 0.12, H], fill=(255, 255, 255, 255))


def draw_orb(d, W, H, color):
    """Glowing energy orb."""
    cx, cy = W / 2, H / 2
    r = W * 0.42
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=with_alpha(color, 1.0),
              outline=with_alpha((255, 255, 255), 0.8), width=int(W * 0.03))
    r2 = r * 0.45
    d.ellipse([cx - r2, cy - r2, cx + r2, cy + r2], fill=(255, 255, 255, 255))


def draw_boss(d, W, H):
    """Big menacing boss ship."""
    cx, cy = W / 2, H / 2
    w = W * 0.92
    half_bw = w * 0.30
    hull = (30, 6, 12)
    accent = (255, 30, 60)
    cockpit = (255, 90, 40)

    # broad angular hull
    body = [(cx - half_bw * 1.7, cy - w * 0.20),
            (cx - half_bw * 0.8, cy - w * 0.44),
            (cx - half_bw * 0.1, cy - w * 0.5),
            (cx + half_bw * 0.1, cy - w * 0.5),
            (cx + half_bw * 0.8, cy - w * 0.44),
            (cx + half_bw * 1.7, cy - w * 0.20),
            (cx + half_bw * 1.35, cy + w * 0.32),
            (cx + half_bw * 0.5, cy + w * 0.42),
            (cx - half_bw * 0.5, cy + w * 0.42),
            (cx - half_bw * 1.35, cy + w * 0.32)]
    draw_poly(d, body, fill=with_alpha(hull, 1.0), outline=with_alpha(accent, 1.0), width=int(W * 0.012))

    # center core (weak point glow)
    core_r = w * 0.13
    d.ellipse([cx - core_r, cy - w * 0.06, cx + core_r, cy - w * 0.06 + core_r * 2],
              fill=with_alpha(cockpit, 1.0), outline=with_alpha((255, 255, 255), 0.9), width=int(W * 0.01))
    core_r2 = core_r * 0.4
    d.ellipse([cx - core_r2, cy - w * 0.03, cx + core_r2, cy - w * 0.03 + core_r2 * 2],
              fill=(255, 255, 255, 255))

    # twin cannons
    for s in (1, -1):
        cax = cx + s * half_bw * 1.35
        d.rectangle([cax - w * 0.025, cy - w * 0.46, cax + w * 0.025, cy - w * 0.18],
                    fill=with_alpha((255, 255, 255), 0.9))
        d.ellipse([cax - w * 0.04, cy - w * 0.50, cax + w * 0.04, cy - w * 0.42],
                  fill=with_alpha(accent, 1.0))

    # armor line details
    for side in (1, -1):
        d.line([cx + side * half_bw * 1.3, cy - w * 0.16,
                cx + side * half_bw * 1.05, cy + w * 0.26],
               fill=with_alpha(accent, 0.6), width=int(W * 0.012))
        # side engine pods
        podx = cx + side * half_bw * 1.5
        d.ellipse([podx - w * 0.07, cy + w * 0.26, podx + w * 0.07, cy + w * 0.38],
                  fill=with_alpha((255, 60, 30), 1.0))

    # engine glow bank
    eg = cy + w * 0.42
    for i in range(-2, 3):
        ex = cx + i * w * 0.06
        d.ellipse([ex - w * 0.02, eg, ex + w * 0.02, eg + w * 0.10], fill=with_alpha((255, 140, 40), 0.9))


def draw_nebula(d, W, H):
    """Procedural nebula background tile (512px). Drawn on supersampled surface."""
    # draw several soft colored blobs; finalize() applies glow which blurs them
    rng = np.random.default_rng(7)
    for _ in range(14):
        bx = rng.uniform(0, 1)
        by = rng.uniform(0, 1)
        br = rng.uniform(0.12, 0.34)
        c = tuple(int(v) for v in rng.choice(
            [(40, 10, 80, 90), (10, 40, 90, 80), (80, 10, 50, 70),
             (8, 30, 60, 90), (60, 20, 80, 70)]))
        d.ellipse([bx * W - br * W, by * H - br * H,
                   bx * W + br * W, by * H + br * H], fill=c)
    # sprinkle faint stars
    for _ in range(120):
        sx = rng.uniform(0, 1) * W
        sy = rng.uniform(0, 1) * H
        s = int(rng.uniform(1, 3) * SS)
        a = rng.uniform(60, 220)
        d.ellipse([sx - s, sy - s, sx + s, sy + s], fill=(255, 255, 255, int(a)))


# ---------------------------------------------------------------------------
# Per-sprite builders
# ---------------------------------------------------------------------------
def build(name, size, draw_fn, glow=True):
    img = canvas(size)
    blit_pil(img, draw_fn, size, size)
    finalize(img, size, name, add_glow=glow)


def main():
    os.makedirs(ASSETS, exist_ok=True)
    print("Generating sprites into %s ..." % ASSETS)

    S = 128  # standard ship canvas
    # Player ship — sleek cyan/white fighter
    build("ship.png", S, lambda d, W, H: draw_fighter(
        d, W, H, hull=(8, 30, 46), accent=(0, 200, 255), cockpit=(120, 240, 255),
        engine=(0, 160, 255), wing_extent=0.52, body_width=0.30))

    # EnemyShipA — basic grunt (blue)
    build("enemy1.png", S, lambda d, W, H: draw_fighter(
        d, W, H, hull=(16, 22, 46), accent=(90, 190, 255), cockpit=(160, 220, 255),
        engine=(60, 140, 255), wing_extent=0.46, body_width=0.30))

    # EnemyShipB — elite gunner (magenta/rose)
    build("enemy2.png", S, lambda d, W, H: draw_wide_ship(
        d, W, H, hull=(46, 10, 40), accent=(255, 60, 200), cockpit=(255, 160, 220),
        engine=(255, 80, 200)))

    # EnemyShipC — sniper (hot pink, thin)
    build("enemy_c.png", S, lambda d, W, H: draw_sniper(
        d, W, H, hull=(40, 8, 40), accent=(255, 70, 200), cockpit=(255, 140, 220),
        engine=(255, 90, 180)))

    # EnemyShipD — tank (orange, wide)
    build("enemy_d.png", S, lambda d, W, H: draw_wide_ship(
        d, W, H, hull=(46, 22, 8), accent=(255, 140, 40), cockpit=(255, 200, 120),
        engine=(255, 120, 30), horns=2))

    # EnemyShipE — fast striker (purple, swept-forward)
    build("enemy_e.png", S, lambda d, W, H: draw_fighter(
        d, W, H, hull=(30, 8, 50), accent=(170, 60, 255), cockpit=(220, 160, 255),
        engine=(150, 50, 255), wing_extent=0.55, body_width=0.26))

    # EnemyShipF — heavy dragoon (gold, big wide)
    build("enemy_f.png", 160, lambda d, W, H: draw_wide_ship(
        d, W, H, hull=(40, 30, 8), accent=(255, 200, 40), cockpit=(255, 240, 160),
        engine=(255, 170, 30), horns=2))

    # Boss — menacing red
    build("enemy_boss.png", 256, draw_boss)

    # Projectiles (tall beams: logical 48x128)
    img = np.zeros((128 * SS, 48 * SS, 4), dtype=np.float32)
    blit_pil(img, lambda d, W, H: draw_beam(d, W, H, (0, 150, 255), (180, 240, 255)), 48, 128)
    finalize_nonsquare(img, 48, 128, "laser_blue.png")
    img = np.zeros((128 * SS, 48 * SS, 4), dtype=np.float32)
    blit_pil(img, lambda d, W, H: draw_beam(d, W, H, (255, 60, 60), (255, 200, 200)), 48, 128)
    finalize_nonsquare(img, 48, 128, "laser_red.png")
    build("plasma_orb.png", 64, lambda d, W, H: draw_orb(d, W, H, (255, 80, 200)), glow=True)

    # Items
    build("item_star.png", 64, lambda d, W, H: draw_crystal(d, W, H, (40, 220, 120)), glow=True)
    build("item_hp.png", 64, lambda d, W, H: draw_med_cross(d, W, H, (255, 60, 70)), glow=True)
    build("item_upgrade.png", 64, lambda d, W, H: draw_upgrade(d, W, H, (0, 160, 255)), glow=True)

    # Background nebula tile (512, no extra glow)
    build("nebula.png", 512, draw_nebula, glow=True)

    print("Done.")


if __name__ == "__main__":
    main()
