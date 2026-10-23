package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.utils.JsonValue;

public class DirectedSkill {
    public static final int KIND_BEAM = 0;
    public static final int KIND_STRIKE = 1;

    private final CampaignWorld world;
    private final CampaignWorld.Enemy enemy;

    public int kind = KIND_BEAM;
    public float chargeSeconds = 1.25f;
    public float trackingSeconds = 0.35f;
    public float activeSeconds = 0.55f;
    public float cooldownSeconds = 3.5f;
    public float beamWidth = 0.22f;
    public boolean sweep;
    public boolean alternateSweep;
    public float sweepLaneMin = 0.22f;
    public float sweepLaneMax = 0.78f;
    public float returnSeconds = 1.1f;

    public int attackCount;
    public boolean warning;
    public boolean active;
    public float fromX;
    public float fromY;
    public float toX;
    public float toY;
    public float width;

    private boolean busy;
    private float elapsed;
    private float cooldown;
    private float recovery;
    private float aimNormX;
    private float aimNormY;
    private float anchorX;
    private float anchorY;
    private float strikeTargetX;
    private float strikeTargetY;
    private float damageCooldown;

    public DirectedSkill(CampaignWorld world, CampaignWorld.Enemy enemy, JsonValue definition) {
        if (definition == null) throw new IllegalArgumentException("Missing directed skill definition");
        this.world = world;
        this.enemy = enemy;

        if (definition != null) {
            kind = definition.getInt("Kind");
            if (kind < 0 || kind > 1) throw new IllegalArgumentException("Unknown directed skill kind");
            chargeSeconds = definition.getFloat("ChargeSeconds", 1.25f);
            trackingSeconds = definition.getFloat("TrackingSeconds", 0.35f);
            activeSeconds = definition.getFloat("ActiveSeconds", 0.55f);
            cooldownSeconds = definition.getFloat("CooldownSeconds", 3.5f);
            beamWidth = definition.getFloat("BeamWidth", 0.22f);
            sweep = definition.getBoolean("Sweep", false);
            alternateSweep = definition.getBoolean("AlternateSweep", false);
            if (definition.has("SweepLanes")) {
                JsonValue lanes = definition.get("SweepLanes");
                if (lanes.isArray() && lanes.size >= 2) {
                    sweepLaneMin = lanes.get(0).asFloat();
                    sweepLaneMax = lanes.get(1).asFloat();
                } else {
                    sweepLaneMin = lanes.getFloat("x", 0.22f);
                    sweepLaneMax = lanes.getFloat("y", 0.78f);
                }
            }
            returnSeconds = definition.getFloat("ReturnSeconds", 1.1f);
        }

        boolean valid = Float.isFinite(chargeSeconds) && chargeSeconds > 0f
                && Float.isFinite(trackingSeconds) && trackingSeconds >= 0f && trackingSeconds < chargeSeconds
                && Float.isFinite(activeSeconds) && activeSeconds > 0f
                && Float.isFinite(cooldownSeconds) && cooldownSeconds > 0f
                && Float.isFinite(beamWidth) && beamWidth > 0f
                && Float.isFinite(returnSeconds) && returnSeconds > 0f
                && Float.isFinite(sweepLaneMin) && Float.isFinite(sweepLaneMax)
                && sweepLaneMin > 0f && sweepLaneMax < 1f && sweepLaneMax > sweepLaneMin;

        if (!valid) throw new IllegalArgumentException("Invalid directed attack definition");
        cooldown = Math.min(.35f, cooldownSeconds);

        width = beamWidth;
    }

    public int phase() {
        return busy ? (active ? 3 : elapsed > trackingSeconds ? 2 : 1) : recovery > 0 ? 4 : 0;
    }

    public boolean busy() {
        return busy;
    }

    public boolean request() {
        if (busy || recovery > 0f || cooldown > 0f) return false;
        if (enemy == null || enemy.hp <= 0f || !enemy.canShoot || !enemy.holding) return false;
        if (world == null || world.player == null || !world.player.canInput() || world.player.enemyFireGrace > 0 || world.stage != CampaignWorld.Stage.RUNNING) return false;

        busy = true;
        active = false;
        warning = true;
        elapsed = 0f;
        damageCooldown = 0f;

        float vw = world.width > 0f ? world.width : 1f;
        float vh = world.height > 0f ? world.height : 1f;
        aimNormX = world.playerX / vw;
        aimNormY = world.playerY / vh;
        anchorX = enemy.x;
        anchorY = enemy.y;
        strikeTargetX = world.playerX;
        strikeTargetY = world.playerY;

        world.sound(kind == KIND_STRIKE ? 11 : 13);
        updateTelegraph();
        return true;
    }

    public void tick(float dt) {
        if (cooldown > 0f) {
            cooldown = Math.max(0f, cooldown - dt);
        }
        if (recovery > 0f) {
            recovery = Math.max(0f, recovery - dt);
        }
        if (damageCooldown > 0f) {
            damageCooldown = Math.max(0f, damageCooldown - dt);
        }

        if (!busy) {
            return;
        }

        if (world == null || enemy == null || enemy.hp <= 0f || !enemy.canShoot || !enemy.holding
                || enemy.retreating || world.stage != CampaignWorld.Stage.RUNNING) {
            cancel();
            return;
        }

        elapsed += dt;

        float vw = world.width > 0f ? world.width : 1f;
        float vh = world.height > 0f ? world.height : 1f;

        if (!active) {
            if (elapsed <= trackingSeconds) {
                aimNormX = world.playerX / vw;
                aimNormY = world.playerY / vh;
            }

            if (elapsed >= chargeSeconds) {
                active = true;
                warning = false;
                elapsed = 0f;
                attackCount++;

                if (kind == KIND_STRIKE) {
                    world.sound(12);
                    enemy.motionPaused = true;
                    anchorX = enemy.x;
                    anchorY = enemy.y;
                    strikeTargetX = aimNormX * vw;
                    strikeTargetY = aimNormY * vh;
                } else {
                    world.sound(14);
                }
            }
        }

        if (kind == KIND_STRIKE) {
            if (active) {
                if (elapsed < activeSeconds) {
                    float t = activeSeconds > 0f ? elapsed / activeSeconds : 1f;
                    t = t * t * (3 - 2 * t);
                    enemy.x = anchorX + (strikeTargetX - anchorX) * t;
                    enemy.y = anchorY + (strikeTargetY - anchorY) * t;

                    float dx = Math.abs(enemy.x - world.playerX);
                    float dy = Math.abs(enemy.y - world.playerY);
                    float hw = (world.direction() == 0 ? enemy.spec.halfWidth : enemy.spec.halfHeight) * enemy.scale * enemy.spec.rootScale;
                    float hh = (world.direction() == 0 ? enemy.spec.halfHeight : enemy.spec.halfWidth) * enemy.scale * enemy.spec.rootScale;
                    if (dx <= hw + world.playerSpec.halfWidth && dy <= hh + world.playerSpec.halfHeight && damageCooldown <= 0f) {
                        world.hitPlayer();
                        damageCooldown = 0.25f;
                    }
                } else if (elapsed < activeSeconds + returnSeconds) {
                    float t = returnSeconds > 0f ? (elapsed - activeSeconds) / returnSeconds : 1f;
                    if (t > 1f) t = 1f;
                    t = t * t * (3 - 2 * t);
                    enemy.x = strikeTargetX + (anchorX - strikeTargetX) * t;
                    enemy.y = strikeTargetY + (anchorY - strikeTargetY) * t;
                } else {
                    enemy.x = anchorX;
                    enemy.y = anchorY;
                    recover();
                    return;
                }
            }
            updateTelegraph();
            return;
        }

        updateTelegraph();

        if (active) {
            float segDx = toX - fromX;
            float segDy = toY - fromY;
            float segLenSq = segDx * segDx + segDy * segDy;
            float t = 0f;
            if (segLenSq > 1e-6f) {
                t = ((world.playerX - fromX) * segDx + (world.playerY - fromY) * segDy) / segLenSq;
                if (t < 0f) t = 0f;
                else if (t > 1f) t = 1f;
            }

            float nx = fromX + t * segDx;
            float ny = fromY + t * segDy;
            float radius = world.playerSpec.halfWidth + width * 0.5f;
            if (com.badlogic.gdx.math.Vector2.dst2(world.playerX, world.playerY, nx, ny) <= radius * radius && damageCooldown <= 0f) {
                world.hitPlayer();
                damageCooldown = 0.25f;
            }

            if (elapsed >= activeSeconds) {
                recover();
            }
        }
    }

    private void updateTelegraph() {
        if (world == null || enemy == null) return;

        float vw = world.width > 0f ? world.width : 1f;
        float vh = world.height > 0f ? world.height : 1f;

        if (kind == KIND_STRIKE) {
            fromX = enemy.x;
            fromY = enemy.y;
            toX = aimNormX * vw;
            toY = aimNormY * vh;
            width = beamWidth;
            return;
        }

        float fx = enemy.x / vw;
        float fy = enemy.y / vh;
        float tx = aimNormX;
        float ty = aimNormY;

        if (sweep) {
            boolean reverse = alternateSweep && (attackCount % 2 == (active ? 0 : 1));
            float progress = active ? (activeSeconds > 0f ? elapsed / activeSeconds : 1f) : 0f;
            if (progress < 0f) progress = 0f;
            else if (progress > 1f) progress = 1f;

            float lane = sweepLaneMin + (sweepLaneMax - sweepLaneMin) * (reverse ? (1f - progress) : progress);
            if (world.direction() == 1) {
                fy = lane;
                tx = -0.05f;
                ty = lane;
            } else {
                fx = lane;
                tx = lane;
                ty = -0.05f;
            }
        } else {
            float dx = tx - fx;
            float dy = ty - fy;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len > 1e-5f) {
                dx /= len;
                dy /= len;
            } else {
                if (world.direction() == 1) {
                    dx = -1f;
                    dy = 0f;
                } else {
                    dx = 0f;
                    dy = -1f;
                }
            }
            tx = fx + dx * 2f;
            ty = fy + dy * 2f;
        }

        fromX = fx * vw;
        fromY = fy * vh;
        toX = tx * vw;
        toY = ty * vh;
        width = beamWidth;
    }

    private void recover() {
        if (kind == KIND_STRIKE) enemy.motionPaused = false;
        recovery = 0.18f;
        busy = false;
        active = false;
        warning = false;
        damageCooldown = 0f;
        cooldown = cooldownSeconds;
    }

    public void cancel() {
        if (kind == KIND_STRIKE) {
            if (busy && active) { enemy.x = anchorX; enemy.y = anchorY; }
            enemy.motionPaused = false;
        }
        busy = false;
        active = false;
        warning = false;
        recovery = 0f;
        damageCooldown = 0f;
        cooldown = attackCount == 0 ? Math.min(0.35f, cooldownSeconds) : cooldownSeconds;
    }
}
