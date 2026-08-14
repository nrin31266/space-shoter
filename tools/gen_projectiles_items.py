#!/usr/bin/env python3
"""
SPACE SHOOTER — PROJECTILE / ITEM / STAR / NEBULA SPRITE GENERATOR
==================================================================
Generates ONLY the projectile, pickup-item, currency-star and background
textures. The ship sprites (ship.png, enemy*.png, enemy_boss.png) are
hand-authored by the project owner and are deliberately NOT regenerated here.

Run:
    python3 tools/gen_projectiles_items.py
"""

import os
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

from gen_sprites import (
    ASSETS, canvas, blit_pil, finalize, finalize_nonsquare,
    with_alpha, lerp, draw_poly, draw_beam, draw_med_cross, draw_orb,
)

SS = 4  # supersample factor (matches gen_sprites.py)


# ---------------------------------------------------------------------------
# Projectiles
# ---------------------------------------------------------------------------
def draw_plasma_orb(d, W, H):
    """Menacing red plasma orb with 8 energy spikes — the boss heavy shot."""
    cx, cy = W / 2, H / 2
    r = W * 0.40
    # energy spikes
    for i in range(8):
        a = i * math.pi / 4
        x1 = cx + r * math.cos(a)
        y1 = cy + r * math.sin(a)
        x2 = cx + (r * 1.4) * math.cos(a + 0.20)
        y2 = cy + (r * 1.4) * math.sin(a + 0.20)
        x3 = cx + (r * 1.4) * math.cos(a - 0.20)
        y3 = cy + (r * 1.4) * math.sin(a - 0.20)
        d.polygon([(x1, y1), (x2, y2), (x3, y3)], fill=with_alpha((255, 60, 40), 0.85))
    # main sphere
    d.ellipse([cx - r, cy - r, cx + r, cy + r],
              fill=with_alpha((255, 80, 40), 1.0),
              outline=with_alpha((255, 255, 255), 0.9), width=int(W * 0.02))
    # hot core
    r2 = r * 0.52
    d.ellipse([cx - r2, cy - r2, cx + r2, cy + r2], fill=with_alpha((255, 200, 120), 1.0))
    r3 = r2 * 0.42
    d.ellipse([cx - r3, cy - r3, cx + r3, cy + r3], fill=(255, 255, 255, 255))


def draw_neon_orb(d, W, H, color):
    """Player explosive orb — neon with a hot white core and strong rim."""
    cx, cy = W / 2, H / 2
    r = W * 0.44
    # soft outer glow ring
    d.ellipse([cx - r * 1.35, cy - r * 1.35, cx + r * 1.35, cy + r * 1.35],
              fill=with_alpha(lerp((0, 0, 0), color, 0.25), 0.35))
    # main sphere
    d.ellipse([cx - r, cy - r, cx + r, cy + r],
              fill=with_alpha(color, 1.0),
              outline=with_alpha(lerp(color, (255, 255, 255), 0.6), 1.0),
              width=int(W * 0.045))
    # mid hot layer
    r2 = r * 0.62
    d.ellipse([cx - r2, cy - r2, cx + r2, cy + r2],
              fill=with_alpha(lerp(color, (255, 255, 255), 0.45), 1.0))
    # white-hot core
    r3 = r * 0.30
    d.ellipse([cx - r3, cy - r3, cx + r3, cy + r3], fill=(255, 255, 255, 255))


def draw_dart(d, W, H, color):
    """Elongated homing dart pointing up (used by the player HOMING weapon)."""
    cx = W / 2
    tip = (cx, H * 0.06)
    base = (cx, H * 0.94)
    half = W * 0.30
    pts = [tip,
           (cx + half, base[1]),
           (cx + half * 0.45, base[1] - H * 0.26),
           (cx + half * 0.45, base[1]),
           (cx - half * 0.45, base[1]),
           (cx - half * 0.45, base[1] - H * 0.26),
           (cx - half, base[1])]
    draw_poly(d, pts, fill=with_alpha(color, 1.0),
              outline=with_alpha((255, 255, 255), 0.9), width=int(W * 0.03))
    # centre spine
    d.line([cx, H * 0.14, cx, base[1] - H * 0.12],
           fill=(255, 255, 255, 230), width=int(W * 0.05))
    # tail fins
    d.polygon([(cx + half * 0.45, base[1] - H * 0.26),
               (cx + half * 1.05, base[1]),
               (cx + half * 0.45, base[1])], fill=with_alpha(color, 0.95))
    d.polygon([(cx - half * 0.45, base[1] - H * 0.26),
               (cx - half * 1.05, base[1]),
               (cx - half * 0.45, base[1])], fill=with_alpha(color, 0.95))


# ---------------------------------------------------------------------------
# Items
# ---------------------------------------------------------------------------
def draw_star5(d, W, H, color):
    """Classic 5-pointed star (space currency)."""
    cx, cy = W / 2, H / 2
    w = W * 0.80
    R = w * 0.50
    r = w * 0.22
    pts = []
    for i in range(10):
        a = -math.pi / 2 + i * math.pi / 5
        rad = R if i % 2 == 0 else r
        pts.append((cx + rad * math.cos(a), cy + rad * math.sin(a)))
    draw_poly(d, pts, fill=with_alpha(color, 1.0),
              outline=with_alpha((255, 255, 255), 0.95), width=int(W * 0.02))
    # inner shine
    inner = []
    for i in range(10):
        a = -math.pi / 2 + i * math.pi / 5
        rad = (R * 0.45 if i % 2 == 0 else r * 0.45)
        inner.append((cx + rad * math.cos(a), cy + rad * math.sin(a)))
    d.polygon(inner, fill=with_alpha((255, 255, 255), 0.45))


def draw_burst_icon(d, W, H, color):
    """Explosion / burst icon inside a ring (EXPLOSIVE weapon upgrade)."""
    cx, cy = W / 2, H / 2
    w = W * 0.64
    ro = w * 0.5
    d.ellipse([cx - ro, cy - ro, cx + ro, cy + ro],
              outline=with_alpha(color, 1.0), width=int(W * 0.055))
    R = w * 0.34
    r = w * 0.15
    pts = []
    for i in range(12):
        a = i * math.pi / 6
        rad = R if i % 2 == 0 else r
        pts.append((cx + rad * math.cos(a), cy + rad * math.sin(a)))
    draw_poly(d, pts, fill=with_alpha(color, 1.0),
              outline=with_alpha((255, 255, 255), 0.9), width=int(W * 0.014))
    d.ellipse([cx - w * 0.08, cy - w * 0.08, cx + w * 0.08, cy + w * 0.08],
              fill=(255, 255, 255, 255))


def draw_target_icon(d, W, H, color):
    """Crosshair / target icon inside a ring (HOMING weapon upgrade)."""
    cx, cy = W / 2, H / 2
    w = W * 0.64
    ro = w * 0.5
    d.ellipse([cx - ro, cy - ro, cx + ro, cy + ro],
              outline=with_alpha(color, 1.0), width=int(W * 0.055))
    r1 = w * 0.26
    r2 = w * 0.13
    d.ellipse([cx - r1, cy - r1, cx + r1, cy + r1],
              outline=with_alpha(color, 1.0), width=int(W * 0.02))
    d.ellipse([cx - r2, cy - r2, cx + r2, cy + r2],
              outline=with_alpha(color, 1.0), width=int(W * 0.02))
    d.ellipse([cx - w * 0.055, cy - w * 0.055, cx + w * 0.055, cy + w * 0.055],
              fill=with_alpha(color, 1.0))
    for s in (1, -1):
        d.line([cx + s * w * 0.36, cy - w * 0.035, cx + s * w * 0.36, cy + w * 0.035],
               fill=(255, 255, 255, 230), width=int(W * 0.02))
        d.line([cx - w * 0.035, cy + s * w * 0.36, cx + w * 0.035, cy + s * w * 0.36],
               fill=(255, 255, 255, 230), width=int(W * 0.02))


def draw_energy_icon(d, W, H, color):
    """Pure energy / power-up icon: glowing hex core (green)."""
    cx, cy = W / 2, H / 2
    w = W * 0.66
    ro = w * 0.5
    # energy ring (double, hex-ish)
    d.ellipse([cx - ro, cy - ro, cx + ro, cy + ro],
              outline=with_alpha(color, 1.0), width=int(W * 0.055))
    d.ellipse([cx - ro * 0.78, cy - ro * 0.78, cx + ro * 0.78, cy + ro * 0.78],
              outline=with_alpha((255, 255, 255), 0.4), width=int(W * 0.012))
    # hexagon core
    r = w * 0.34
    pts = []
    for i in range(6):
        a = math.pi / 180.0 * (60 * i + 30)
        pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    draw_poly(d, pts, fill=with_alpha(color, 1.0),
              outline=with_alpha((255, 255, 255), 0.9), width=int(W * 0.016))
    # white-hot inner spark (up arrow)
    s = w * 0.16
    d.polygon([(cx, cy - s * 1.1), (cx + s * 0.7, cy + s * 0.5), (cx, cy + s * 0.1),
               (cx - s * 0.7, cy + s * 0.5)], fill=(255, 255, 255, 255))


# ---------------------------------------------------------------------------
# Background (dark nebula with faint halo rings — no falling streaks)
# ---------------------------------------------------------------------------
def draw_nebula_v2(d, W, H):
    """Very dark space tile: faint colour blobs + subtle concentric rings."""
    rng = np.random.default_rng(11)
    # faint colour blobs (dark & subordinate)
    for _ in range(10):
        bx, by, br = rng.uniform(0, 1), rng.uniform(0, 1), rng.uniform(0.14, 0.32)
        c = tuple(int(v) for v in rng.choice(
            [(26, 7, 54, 60), (7, 26, 62, 52), (52, 7, 34, 46), (5, 20, 44, 58)]))
        d.ellipse([bx * W - br * W, by * H - br * H,
                   bx * W + br * W, by * H + br * H], fill=c)
    # faint concentric halo rings ("vòng nhạt" outside)
    rings = [(0.30, 40, (110, 150, 255)), (0.44, 26, (255, 120, 170)),
             (0.58, 16, (120, 200, 255))]
    for rr, alpha, col in rings:
        d.ellipse([W * 0.5 - rr * W, H * 0.5 - rr * H * 1.25,
                   W * 0.5 + rr * W, H * 0.5 + rr * H * 1.25],
                  outline=(col[0], col[1], col[2], alpha), width=int(W * 0.006))
    # tiny sparse stars
    for _ in range(70):
        sx, sy = rng.uniform(0, 1) * W, rng.uniform(0, 1) * H
        s = int(rng.uniform(1, 2.2) * SS)
        d.ellipse([sx - s, sy - s, sx + s, sy + s],
                  fill=(255, 255, 255, int(rng.uniform(36, 120))))


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
def main():
    os.makedirs(ASSETS, exist_ok=True)
    print("Generating projectile/item/star/nebula sprites into %s ..." % ASSETS)

    # --- Projectiles ------------------------------------------------
    img = np.zeros((128 * SS, 48 * SS, 4), dtype=np.float32)
    blit_pil(img, lambda d, W, H: draw_beam(d, W, H, (0, 170, 255), (190, 245, 255)), 48, 128)
    finalize_nonsquare(img, 48, 128, "laser_blue.png")
    img = np.zeros((128 * SS, 48 * SS, 4), dtype=np.float32)
    blit_pil(img, lambda d, W, H: draw_beam(d, W, H, (255, 70, 70), (255, 205, 205)), 48, 128)
    finalize_nonsquare(img, 48, 128, "laser_red.png")

    build("plasma_orb.png", 64, draw_plasma_orb)
    build("orb_red.png", 48, lambda d, W, H: draw_orb(d, W, H, (255, 80, 80)))
    build("orb_green.png", 48, lambda d, W, H: draw_orb(d, W, H, (70, 255, 120)))
    build("orb_gold.png", 48, lambda d, W, H: draw_orb(d, W, H, (255, 205, 60)))
    build("orb_purple.png", 48, lambda d, W, H: draw_orb(d, W, H, (205, 90, 255)))
    build("orb_pink.png", 48, lambda d, W, H: draw_orb(d, W, H, (255, 80, 200)))

    # Player weapons — neon, distinct, with a white-hot core for pop.
    build("shot_orb.png", 48, lambda d, W, H: draw_neon_orb(d, W, H, (255, 130, 40)))
    img = np.zeros((64 * SS, 40 * SS, 4), dtype=np.float32)
    blit_pil(img, lambda d, W, H: draw_dart(d, W, H, (200, 80, 255)), 40, 64)
    finalize_nonsquare(img, 40, 64, "shot_dart.png")

    # --- Items -------------------------------------------------------
    build("item_star.png", 64, lambda d, W, H: draw_star5(d, W, H, (255, 205, 60)))
    build("item_hp.png", 64, lambda d, W, H: draw_med_cross(d, W, H, (255, 70, 85)))
    build("item_upgrade.png", 64, lambda d, W, H: draw_upgrade_bolt(d, W, H, (0, 170, 255)))
    build("item_upgrade_explosive.png", 64, lambda d, W, H: draw_burst_icon(d, W, H, (255, 120, 30)))
    build("item_upgrade_homing.png", 64, lambda d, W, H: draw_target_icon(d, W, H, (205, 90, 255)))
    build("item_energy.png", 64, lambda d, W, H: draw_energy_icon(d, W, H, (80, 255, 140)))

    # --- Background --------------------------------------------------
    build("nebula.png", 512, draw_nebula_v2)

    print("Done.")


def build(name, size, draw_fn, glow=True):
    img = canvas(size)
    blit_pil(img, draw_fn, size, size)
    finalize(img, size, name, add_glow=glow)


def draw_upgrade_bolt(d, W, H, color):
    """Blue lightning bolt inside a ring (PLASMA weapon upgrade)."""
    cx, cy = W / 2, H / 2
    w = W * 0.64
    ro = w * 0.5
    d.ellipse([cx - ro, cy - ro, cx + ro, cy + ro],
              outline=with_alpha(color, 1.0), width=int(W * 0.055))
    bw = w * 0.17
    bolt = [(cx + bw * 0.6, cy - w * 0.42),
            (cx - bw * 0.5, cy + w * 0.02),
            (cx - bw * 0.05, cy + w * 0.02),
            (cx - bw * 0.6, cy + w * 0.42),
            (cx + bw * 0.5, cy - w * 0.02),
            (cx + bw * 0.05, cy - w * 0.02)]
    draw_poly(d, bolt, fill=with_alpha(color, 1.0),
              outline=with_alpha((255, 255, 255), 0.85), width=int(W * 0.012))


if __name__ == "__main__":
    main()
