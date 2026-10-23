package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

public final class BossCombat {
    public static final class Node {
        public String objectId;
        public float x, y, hp, maxHp, halfWidth, halfHeight;
        float offsetX, offsetY;
    }
    public static final class Beam {
        public float fromX, fromY, toX, toY, width;
        public boolean active, warning, field;
        final BeamContact contact = new BeamContact();
    }
    public static final class SkillCue {
        public JsonValue data;
        public CampaignCatalog.Pose pose;
        public String[][] frames;
        public int phase;
        public float age;
    }
    public final Array<SkillCue> cues = new Array<>();
    public final Array<Node> nodes = new Array<>();
    private final Array<float[]> coreShapes = new Array<>();
    public final Array<Beam> beams = new Array<>();
    public boolean phaseB;
    public final String shieldSprite;
    public final CampaignCatalog.Pose shieldPose;
    private final CampaignWorld world;
    private final CampaignWorld.Enemy enemy;
    private final JsonValue definition;
    private final Array<Module> modules = new Array<>();
    private final float collapseSeconds;
    private float collapseRemaining, remaining = 1.5f;
    private boolean collapsing;
    private int ordinal;
    private Module current;

    public BossCombat(CampaignWorld world, CampaignWorld.Enemy enemy) {
        this.world = world; this.enemy = enemy;
        JsonValue runner = world.catalog.rootComponent(enemy.spec.record, "BossPhaseRunner");
        definition = world.catalog.resolve(runner.get("definition"));
        for (JsonValue ref : runner.get("modules")) modules.add(new Module(CampaignWorld.local(enemy.spec.record, ref)));
        for (String field : new String[]{"PhaseAOrder", "PhaseBOrder"}) {
            for (JsonValue index : definition.get(field))
                if (index.asInt() < 0 || index.asInt() >= modules.size) throw new IllegalArgumentException("Invalid boss module order");
        }
        JsonValue network = world.catalog.rootComponent(enemy.spec.record, "BossShieldNetwork");
        collapseSeconds = network == null ? 0 : network.getFloat("collapseSeconds");
        collapseRemaining = collapseSeconds;
        JsonValue shield = network == null ? null : CampaignWorld.local(enemy.spec.record, network.get("shield"));
        shieldSprite = shield == null ? null : CampaignCatalog.spriteKey(shield.get("m_Sprite"));
        shieldPose = shield == null ? null : CampaignCatalog.pose(enemy.spec.record, shield.get("m_GameObject").getString("fileID"));
        if (network != null) for (JsonValue ref : network.get("nodes")) {
            JsonValue health = CampaignWorld.local(enemy.spec.record, ref);
            Node node = new Node();
            node.objectId = health.get("m_GameObject").getString("fileID");
            node.hp = node.maxHp = world.catalog.resolve(health.get("definition")).getInt("maxHp");
            CampaignCatalog.Pose pose = CampaignCatalog.pose(enemy.spec.record, node.objectId);
            node.offsetX = pose.x; node.offsetY = pose.y;
            JsonValue collider = null;
            for (JsonValue doc : enemy.spec.record.get("documents")) {
                JsonValue data = doc.get("data");
                if (doc.getString("type").equals("BoxCollider2D") && data.get("m_GameObject").getString("fileID").equals(node.objectId)) collider = data;
            }
            if (collider == null) throw new IllegalArgumentException("Missing boss node collider");
            node.halfWidth = collider.get("m_Size").getFloat("x") * Math.abs(pose.sx) * enemy.scale * .5f;
            node.halfHeight = collider.get("m_Size").getFloat("y") * Math.abs(pose.sy) * enemy.scale * .5f;
            if (world.direction() == 1) { float w = node.halfWidth; node.halfWidth = node.halfHeight; node.halfHeight = w; }
            nodes.add(node);
        }
        // Khung collisionShape của flight chỉ để tính biên, không nhận đạn.
        for (JsonValue doc : enemy.spec.record.get("documents")) {
            JsonValue box = doc.get("data");
            if (!doc.getString("type").equals("BoxCollider2D") || box.getInt("m_Enabled", 1) == 0) continue;
            String id = box.get("m_GameObject").getString("fileID");
            boolean nodeShape = false;
            for (Node node : nodes) if (node.objectId.equals(id)) nodeShape = true;
            if (nodeShape) continue;
            CampaignCatalog.Pose p = CampaignCatalog.pose(enemy.spec.record, id);
            coreShapes.add(new float[]{p.x + box.get("m_Offset").getFloat("x") * p.sx,
                    p.y + box.get("m_Offset").getFloat("y") * p.sy,
                    box.get("m_Size").getFloat("x") * Math.abs(p.sx) * .5f,
                    box.get("m_Size").getFloat("y") * Math.abs(p.sy) * .5f});
        }
        updateNodes();
    }

    public float coreHitFraction(CampaignWorld.Shot shot, float shotHW, float shotHH) {
        float nearest = Float.POSITIVE_INFINITY;
        for (float[] box : coreShapes) {
            boolean right = world.direction() == 1;
            float x = enemy.x + (right ? box[1] : box[0]) * enemy.scale;
            float y = enemy.y + (right ? -box[0] : box[1]) * enemy.scale;
            nearest = Math.min(nearest, CampaignWorld.segmentFraction(shot, x, y,
                    (right ? box[3] : box[2]) * enemy.scale + shotHW,
                    (right ? box[2] : box[3]) * enemy.scale + shotHH));
        }
        return nearest;
    }

    private void updateNodes() {
        for (Node node : nodes) {
            node.x = enemy.x + (world.direction() == 0 ? node.offsetX : node.offsetY) * enemy.scale;
            node.y = enemy.y + (world.direction() == 0 ? node.offsetY : -node.offsetX) * enemy.scale;
        }
    }

    public boolean busy() { return current != null && current.busy; }
    public String displayName() { return definition.getString("DisplayName"); }
    public boolean blocksCoreDamage() { return nodes.size > 0 && (!collapsing || collapseRemaining > 0); }

    public boolean hitNodes(CampaignWorld.Shot shot) {
        if (!enemy.holding || !world.player.canInput()) return false;
        for (Node node : nodes) {
            if (node.hp <= 0 || !CampaignWorld.segmentHit(shot, node.x, node.y, node.halfWidth + shot.radius, node.halfHeight + shot.radius)) continue;
            damageNode(node, shot.damage);
            return true;
        }
        return false;
    }

    public void damageNode(Node node, float amount) {
        if (node.hp <= 0 || !enemy.holding) return;
        node.hp = Math.max(0, node.hp - amount);
        enemy.flash = .08f;
        if (node.hp == 0) world.sound(23);
    }

    public void tick(float dt) {
        for (SkillCue cue : cues) {
            cue.age += dt;
            if (cue.phase == 4 && cue.age >= cue.data.getFloat("frameSeconds") * cue.frames[2].length) cue.phase = 0;
        }
        for (Module module : modules) if (module.directed != null) module.directed.tick(dt);
        updateNodes();
        int alive = 0;
        for (Node node : nodes) if (node.hp > 0) alive++;
        if (nodes.size > 0 && alive == 0) {
            if (!collapsing) { collapsing = true; world.reposition(enemy, 0, .035f, collapseSeconds, 1); }
            collapseRemaining = Math.max(0, collapseRemaining - dt);
        }
        if (!phaseB && (nodes.size > 0 ? !blocksCoreDamage() : enemy.hp <= enemy.maxHp * definition.getFloat("PhaseBThreshold"))) {
            phaseB = true; cancel(); ordinal = 0;
            remaining = definition.getFloat("TransitionSeconds");
            JsonValue offset = definition.get("PhaseBOffset");
            world.reposition(enemy, offset.getFloat("x"), offset.getFloat("y"), remaining, definition.getFloat("PhaseBMotionMultiplier"));
            world.sound(23);
        }
        if (enemy.hp <= 0 || enemy.retreating || !enemy.holding || world.stage != CampaignWorld.Stage.RUNNING) {
            cancel(); remaining = Math.max(remaining, 1); return;
        }
        if (current != null) {
            current.tick(dt);
            if (current.busy) return;
            current = null;
            remaining = definition.getFloat(phaseB ? "PhaseBRestSeconds" : "PhaseARestSeconds");
        }
        if (!world.player.canInput() || world.player.enemyFireGrace > 0) return;
        remaining -= dt;
        if (remaining > 0 || !world.canStartBossAttack()) return;
        JsonValue order = definition.get(phaseB ? "PhaseBOrder" : "PhaseAOrder");
        Module module = modules.get(order.getInt(ordinal++ % order.size));
        if (module.begin()) { current = module; world.recordBossAttack(); }
    }

    public void cancel() {
        for (Module module : modules) module.cancel();
        current = null;
    }

    private final class Module {
        final JsonValue data, fire, barrage;
        final DirectedSkill directed;
        final EscortController escorts;
        final Array<CampaignCatalog.Pose> muzzles = new Array<>();
        final Beam beam = new Beam(), lower = new Beam(), upper = new Beam();
        final boolean isBeam;
        final Array<SkillCue> moduleCues = new Array<>();
        boolean busy, active;
        float timer, safeLane, initialLane, originForward, beamX;
        int pulse;

        Module(JsonValue data) {
            this.data = data;
            fire = world.catalog.resolve(data.get("definition"));
            JsonValue barrageComponent = CampaignWorld.local(enemy.spec.record, data.get("barrage"));
            barrage = barrageComponent == null ? null : world.catalog.resolve(barrageComponent.get("definition"));
            JsonValue directedComponent = CampaignWorld.local(enemy.spec.record, data.get("directed"));
            directed = directedComponent == null ? null : new DirectedSkill(world, enemy, world.catalog.resolve(directedComponent.get("definition")));
            JsonValue spawner = CampaignWorld.local(enemy.spec.record, data.get("escorts"));
            EscortController found = null;
            for (EscortController controller : enemy.escorts) if (controller.component == spawner) found = controller;
            escorts = found;
            isBeam = !data.get("beam").getString("fileID").equals("0");
            for (JsonValue ref : data.get("muzzles")) {
                JsonValue transform = CampaignWorld.local(enemy.spec.record, ref);
                muzzles.add(CampaignCatalog.pose(enemy.spec.record, transform.get("m_GameObject").getString("fileID")));
            }
            if (muzzles.size == 0) throw new IllegalArgumentException("Boss module has no muzzle");
            lower.field = upper.field = true;
            beams.add(beam); beams.add(lower); beams.add(upper);
            for (JsonValue presentation : world.catalog.components(enemy.spec.record, "BossSkillPhasePresentation")) {
                if (CampaignWorld.local(enemy.spec.record, presentation.get("module")) != data
                        && (barrageComponent == null || CampaignWorld.local(enemy.spec.record, presentation.get("barrage")) != barrageComponent)) continue;
                JsonValue renderer = CampaignWorld.local(enemy.spec.record, presentation.get("cue"));
                SkillCue cue = new SkillCue(); cue.data = presentation;
                cue.pose = CampaignCatalog.pose(enemy.spec.record, renderer.get("m_GameObject").getString("fileID"));
                cue.frames = new String[][]{CampaignWorld.frames(presentation.get("prepareFrames")), CampaignWorld.frames(presentation.get("lockFrames")), CampaignWorld.frames(presentation.get("attackFrames"))};
                moduleCues.add(cue); cues.add(cue);
            }
        }

        float muzzleX(int i) {
            CampaignCatalog.Pose p = muzzles.get(i);
            return enemy.x + (world.direction() == 0 ? p.x : p.y) * enemy.scale;
        }
        float muzzleY(int i) {
            CampaignCatalog.Pose p = muzzles.get(i);
            return enemy.y + (world.direction() == 0 ? p.y : -p.x) * enemy.scale;
        }

        void cuePhase(int phase) {
            for (SkillCue cue : moduleCues) if (cue.phase != phase) { cue.phase = phase; cue.age = 0; }
        }

        boolean begin() {
            if (directed != null) { busy = directed.request(); if (busy) cuePhase(1); return busy; }
            cuePhase(1);
            busy = true; active = false; pulse = 0;
            timer = barrage != null ? barrage.getFloat("ChargeSeconds") : fire.getFloat("telegraphDuration");
            if (isBeam) { beamX = world.direction() == 0 ? muzzleX(0) / world.width : muzzleY(0) / world.height; enemy.motionPaused = true; }
            if (barrage != null) {
                float lateral = world.direction() == 0 ? world.playerX / world.width : 1 - world.playerY / world.height;
                safeLane = initialLane = lateral < .5f ? .75f : .25f;
                originForward = MathUtils.clamp((world.direction() == 0 ? enemy.y / world.height : enemy.x / world.width) - .1f, .6f, .86f);
                world.sound(barrage.getInt("Kind") == 0 ? 20 : 21);
            } else world.sound(isBeam ? 19 : escorts != null ? 22 : 18);
            return true;
        }

        void cancel() {
            if (busy) cuePhase(4);
            busy = active = false;
            beam.active = beam.warning = lower.active = lower.warning = upper.active = upper.warning = false;
            beam.contact.reset(); lower.contact.reset(); upper.contact.reset();
            if (directed != null) directed.cancel();
            if (isBeam) enemy.motionPaused = false;
        }

        void tick(float dt) {
            if (!busy) return;
            if (directed != null) {
                busy = directed.busy();
                cuePhase(directed.phase());
                beam.fromX = directed.fromX; beam.fromY = directed.fromY;
                beam.toX = directed.toX; beam.toY = directed.toY; beam.width = directed.width;
                beam.active = directed.active; beam.warning = directed.warning;
                return;
            }
            if (barrage != null) { barrage(dt); if (busy) cuePhase(active ? 3 : timer < barrage.getFloat("ChargeSeconds") * .45f ? 2 : 1); return; }
            timer -= dt;
            cuePhase(active ? 3 : timer < fire.getFloat("telegraphDuration") * .45f ? 2 : 1);
            if (isBeam) {
                if (active || timer > fire.getFloat("telegraphDuration") * .45f) {
                    float target = MathUtils.clamp(world.direction() == 0 ? world.playerX / world.width : world.playerY / world.height, .08f, .92f);
                    beamX += MathUtils.clamp(target - beamX, -data.getFloat("beamTrackingSpeed") * dt, data.getFloat("beamTrackingSpeed") * dt);
                }
                beam.fromX = muzzleX(0); beam.fromY = muzzleY(0);
                beam.toX = world.direction() == 0 ? beamX * world.width : -.02f * world.width;
                beam.toY = world.direction() == 0 ? -.02f * world.height : beamX * world.height;
                beam.width = data.getFloat("beamViewportWidth") * (world.direction() == 0 ? world.width : world.height);
                beam.active = active; beam.warning = !active;
                if (active) applyBeam(beam);
                if (timer <= 0) {
                    if (active) cancel();
                    else { active = true; timer = data.getFloat("beamSeconds"); world.sound(14); }
                }
                return;
            }
            if (timer > 0) return;
            if (escorts != null) {
                for (int i = 0; i < data.getInt("escortVolleyCount"); i++) escorts.spawn();
            } else for (int i = 0; i < muzzles.size; i++) {
                float x = muzzleX(i), y = muzzleY(i);
                world.launchPattern(fire, null, x, y, true, MathUtils.atan2(world.playerY - y, world.playerX - x) * MathUtils.radiansToDegrees);
                world.sound(16);
            }
            cancel();
        }

        void barrage(float dt) {
            boolean laser = barrage.getInt("Kind") == 1;
            float opening = barrage.getFloat("OpeningWidth");
            if (!laser && active && pulse < barrage.getInt("PulseCount") && timer <= barrage.getFloat("PulseInterval") - .28f) {
                active = false;
                safeLane = initialLane + (pulse % 2 == 1 ? (initialLane < .5f ? .28f : -.28f) : 0);
            }
            band(lower, 0, safeLane - opening * .5f);
            band(upper, safeLane + opening * .5f, 1);
            lower.warning = upper.warning = !active;
            lower.active = upper.active = laser && active;
            if (laser && active) { applyBeam(lower); applyBeam(upper); }
            timer -= dt;
            if (timer > 0) return;
            if (laser) {
                if (!active) { active = true; timer = barrage.getFloat("ActiveSeconds"); }
                else if (++pulse >= barrage.getInt("PulseCount")) cancel();
                else { active = false; safeLane = 1 - safeLane; timer = barrage.getFloat("ChargeSeconds") + barrage.getFloat("PulseInterval"); }
            } else if (pulse < barrage.getInt("PulseCount")) {
                active = true;
                JsonValue projectile = world.catalog.resolve(barrage.get("Projectile"));
                float edge = projectileHalfWidth(projectile) / (world.direction() == 0 ? world.width : world.height);
                int count = barrage.getInt("ProjectilesPerRow");
                for (int i = 0; i < count; i++) {
                    float lane = .02f + .96f * (i + (pulse % 2 == 0 ? 0 : .5f)) / (count - 1);
                    if (lane > .98f || Math.abs(lane - safeLane) <= opening * .5f + edge) continue;
                    float x = (world.direction() == 0 ? lane : originForward) * world.width;
                    float y = (world.direction() == 0 ? originForward : 1 - lane) * world.height;
                    world.launchPattern(projectile, null, x, y, true, world.direction() == 0 ? -90 : 180);
                }
                pulse++;
                timer = pulse == barrage.getInt("PulseCount") ?
                        (originForward + .05f) * (world.direction() == 0 ? world.height : world.width) / projectile.getFloat("projectileSpeed") : barrage.getFloat("PulseInterval");
            } else cancel();
        }

        void band(Beam b, float from, float to) {
            if (!active) b.contact.reset();
            float center = (from + to) * .5f;
            if (world.direction() == 0) {
                b.fromX = b.toX = center * world.width;
                b.fromY = -.02f * world.height; b.toY = originForward * world.height;
                b.width = (to - from) * world.width;
            } else {
                b.fromY = b.toY = (1 - center) * world.height;
                b.fromX = -.02f * world.width; b.toX = originForward * world.width;
                b.width = (to - from) * world.height;
            }
        }
    }

    private float projectileHalfWidth(JsonValue definition) {
        JsonValue record = world.catalog.referencedRecord(definition.get("projectilePrefab"));
        for (JsonValue doc : record.get("documents")) if (doc.getString("type").equals("BoxCollider2D")) return doc.get("data").get("m_Size").getFloat("x") * .5f;
        throw new IllegalArgumentException("Missing barrage projectile collider");
    }

    private void applyBeam(Beam beam) {
        if (beam.contact.hit(beam.fromX, beam.fromY, beam.toX, beam.toY, beam.width,
                world.playerX, world.playerY, world.playerSpec.halfWidth)) world.hitPlayer();
    }
}
