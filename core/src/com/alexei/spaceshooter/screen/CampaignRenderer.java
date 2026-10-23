package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.effect.PrefabVisual;
import com.alexei.spaceshooter.manager.CampaignAssets;
import com.alexei.spaceshooter.manager.CampaignWorld;
import com.alexei.spaceshooter.manager.CombatState;
import com.alexei.spaceshooter.manager.BossCombat;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class CampaignRenderer implements Disposable {
    private final CampaignAssets assets;
    private final CampaignWorld world;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;

    private final ObjectMap<JsonValue, PrefabVisual> visuals = new ObjectMap<JsonValue, PrefabVisual>();
    private final ObjectMap<JsonValue, String[]> pickupAnimationCache = new ObjectMap<JsonValue, String[]>();

    private JsonValue chassis, trail;
    private String chassisKey;
    private String[] engineFrames, trailFrames;
    private final float[] trailX = new float[24], trailY = new float[24], trailAge = new float[24];
    private int trailCursor;
    private float lastTrailClock = -1, lastPlayerX, lastPlayerY;

    private float backgroundX, backgroundY, previousBackgroundClock, starDistance;
    private com.badlogic.gdx.graphics.g2d.TextureRegion[] streaks;
    private JsonValue introPresentation;
    private String bgBaseKey;
    private String bgAltKey;
    private String planetAstraKey;
    private String planetCrimsonKey;
    private String debrisKey;
    private String shieldKey;
    private String beamKey;

    public CampaignRenderer(CampaignAssets assets, CampaignWorld world) {
        this.assets = assets;
        this.world = world;
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        resolveKnownSpriteKeys();
        JsonValue scene = assets.catalog.record("Scenes/02_Gameplay.unity");
        introPresentation = assets.catalog.component(scene, "MissionIntroPresentation");
        JsonValue stars = local(scene, introPresentation.get("stars"));
        streaks = new com.badlogic.gdx.graphics.g2d.TextureRegion[stars.get("m_Children").size];
        int index = 0;
        for (JsonValue child : stars.get("m_Children")) {
            JsonValue transform = local(scene, child);
            JsonValue go = local(scene, transform.get("m_GameObject"));
            for (JsonValue component : go.get("m_Component")) {
                JsonValue data = local(scene, component.get("component"));
                if (data.has("m_Sprite")) streaks[index++] = assets.sprite(data.get("m_Sprite")).region;
            }
        }
        JsonValue cosmetics = assets.catalog.asset("Data/Cosmetics/ShipCosmetics.asset");
        for (JsonValue option : cosmetics.get("Options")) {
            if (option.getString("Id").equals(world.save.equippedCosmetics[0])) chassis = option;
            if (option.getString("Id").equals(world.save.equippedCosmetics[2])) trail = option;
        }
        if (chassis == null || trail == null) throw new IllegalArgumentException("Unknown equipped cosmetic");
        chassisKey = CampaignCatalog.spriteKey(chassis.get(world.save.equippedCosmetics[1].equals("skin-amber") ? "AmberSprite" : "Sprite"));
        engineFrames = CampaignWorld.frames(cosmetics.get("EngineFrames"));
        trailFrames = CampaignWorld.frames(trail.get("AnimationFrames"));
        java.util.Arrays.fill(trailAge, -100);
        lastPlayerX = world.playerX; lastPlayerY = world.playerY;
    }

    private void resolveKnownSpriteKeys() {
        if (assets == null || assets.catalog == null || assets.catalog.root == null) {
            return;
        }
        JsonValue sprites = assets.catalog.root.get("sprites");
        if (sprites == null) {
            return;
        }
        for (JsonValue entry = sprites.child; entry != null; entry = entry.next) {
            String path = entry.getString("path", "");
            if (path.endsWith("Environments/background.png")) {
                bgBaseKey = entry.name;
            } else if (path.endsWith("Environments/crimson-background.png")) {
                bgAltKey = entry.name;
            } else if (path.endsWith("Environments/astra-planet.png")) {
                planetAstraKey = entry.name;
            } else if (path.endsWith("Environments/crimson-planet.png")) {
                planetCrimsonKey = entry.name;
            } else if (path.endsWith("Environments/parallax-debris.png")) {
                debrisKey = entry.name;
            } else if (path.endsWith("VFX/shield-activate-break.png") && entry.name.endsWith(":21300000")) {
                shieldKey = entry.name;
            } else if (path.endsWith("VFX/beam-energy.png") && entry.name.endsWith(":21300000")) {
                beamKey = entry.name;
            }
        }
    }

    public void resize(int width, int height) {
        if (height <= 0) height = 1;
        float aspect = (float) width / (float) height;
        world.resize(aspect);
        camera.setToOrtho(false, world.width, world.height);
        camera.update();
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(.01f, .015f, .035f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float shakeX = 0f;
        float shakeY = 0f;
        boolean reducedMotion = (world.save != null && world.save.reducedMotion);
        if (!reducedMotion && world.shake > 0.001f) {
            float intensity = Math.min(world.shake, 1.5f);
            shakeX = MathUtils.sin(world.clock * 45f) * intensity * 0.35f;
            shakeY = MathUtils.cos(world.clock * 53f) * intensity * 0.35f;
        }

        camera.viewportWidth = world.width;
        camera.viewportHeight = world.height;
        camera.position.set(world.width * 0.5f + shakeX, world.height * 0.5f + shakeY, 0f);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        renderBackground();
        renderPickups();
        renderEnemies();
        renderCorpses();
        renderBeams();
        renderPlayer();
        renderProjectiles();
        renderBursts();

        batch.end();
    }

    private static JsonValue local(JsonValue record, JsonValue reference) {
        String id = reference.getString("fileID");
        for (JsonValue doc : record.get("documents")) if (doc.getString("fileID").equals(id)) return doc.get("data");
        throw new IllegalArgumentException("Missing scene object " + id);
    }

    private void renderBackground() {
        float delta = Math.max(0, Math.min(.1f, world.clock - previousBackgroundClock));
        previousBackgroundClock = world.clock;
        float speed = .035f, t = world.cinematicProgress;
        t = t * t * (3 - 2 * t);
        boolean journey = world.stage == CampaignWorld.Stage.REGION_TRAVEL;
        boolean handoff = world.stage == CampaignWorld.Stage.HANDOFF || world.stage == CampaignWorld.Stage.MISSION_TRAVEL;
        if (world.stage == CampaignWorld.Stage.BRIEFING) speed = .015f;
        else if (world.stage == CampaignWorld.Stage.APPROACH) speed = MathUtils.lerp(.015f, .22f, t);
        else if (world.stage == CampaignWorld.Stage.TRAVEL) speed = MathUtils.lerp(.22f, 2.8f, t);
        else if (world.stage == CampaignWorld.Stage.BRAKE) speed = 2.8f * (1 - t);
        else if (journey) speed = world.journeySpeed;
        else if (handoff) speed = world.stage == CampaignWorld.Stage.HANDOFF ? .08f + .95f * MathUtils.sin(world.cinematicProgress * MathUtils.PI) : .25f + 1.5f * MathUtils.sin(world.cinematicProgress * MathUtils.PI);
        float heading = world.presentationRotation();
        boolean warp = journey || world.stage == CampaignWorld.Stage.TRAVEL || world.stage == CampaignWorld.Stage.BRAKE;
        float multiplier = 3 * (warp ? introPresentation.getFloat("warpBackdropMultiplier") : 1);
        backgroundX += MathUtils.sinDeg(heading) * speed * delta * multiplier;
        backgroundY -= MathUtils.cosDeg(heading) * speed * delta * multiplier;
        float palette = journey ? world.journeyPalette : world.direction() == 1 ? 1 : 0;
        drawBackgroundTile(bgBaseKey, 1);
        if (palette > 0) drawBackgroundTile(bgAltKey, palette);
        boolean planetVisible = world.stage == CampaignWorld.Stage.BRIEFING || world.stage == CampaignWorld.Stage.APPROACH || world.stage == CampaignWorld.Stage.TRAVEL;
        float start = introPresentation.getFloat("planetStartScale"), end = introPresentation.getFloat("planetEndScale");
        if (journey && world.journeyReveal > 0) {
            float reveal = world.journeyReveal, eased = reveal * reveal * (3 - 2 * reveal);
            batch.setColor(1, 1, 1, Math.min(1, reveal * 4));
            assets.draw(batch, planetCrimsonKey, MathUtils.lerp(1.1f, .76f, eased) * world.width,
                    MathUtils.lerp(.3f, .3f, eased) * world.height, MathUtils.lerp(.25f, start, reveal), -world.clock * 10);
        } else if (planetVisible) {
            String planet = world.direction() == 1 ? planetCrimsonKey : planetAstraKey;
            float approach = world.stage == CampaignWorld.Stage.BRIEFING ? 0 : world.stage == CampaignWorld.Stage.APPROACH ? t : 1;
            float x = MathUtils.lerp(.7f, .67f, approach), y = MathUtils.lerp(.76f, .8f, approach);
            if (world.direction() == 1) { float old = x; x = y; y = 1 - old; }
            float fade = MathUtils.clamp(world.cinematicProgress / .3f, 0, 1);
            float alpha = world.stage == CampaignWorld.Stage.TRAVEL ? 1 - fade * fade * (3 - 2 * fade) : 1;
            batch.setColor(1, 1, 1, alpha);
            assets.draw(batch, planet, x * world.width, y * world.height, MathUtils.lerp(start, end, approach), -world.clock * 10);
        }
        if (world.cinematic() || handoff) {
            starDistance += speed * delta;
            float opacity = handoff ? MathUtils.sin(world.cinematicProgress * MathUtils.PI) : world.stage == CampaignWorld.Stage.BRAKE ? 1 - t : 1;
            for (int i = 0; i < streaks.length; i++) {
                float depth = .45f + repeat(i * .371f, .55f);
                float x = repeat(i * .618034f, 1), y = repeat(i * .437f - starDistance * depth, 1.4f) - .2f;
                if (journey) {
                    x = repeat(repeat(i * .618034f, 1) + MathUtils.sinDeg(heading) * starDistance * depth + .15f, 1.3f) - .15f;
                    y = repeat(repeat(i * .437f, 1) - MathUtils.cosDeg(heading) * starDistance * depth + .15f, 1.3f) - .15f;
                } else if (world.direction() == 1) { float old = x; x = y; y = 1 - old; }
                float unit = world.height / 900;
                float w = introPresentation.getFloat("streakWidthAt900") * MathUtils.lerp(.55f, 1, depth) * unit;
                float h = w + speed * introPresentation.getFloat("streakLengthPerSpeedAt900") * depth * unit;
                batch.setColor(MathUtils.lerp(.72f, 1, palette), MathUtils.lerp(.91f, .68f, palette), MathUtils.lerp(1, .76f, palette), opacity * MathUtils.lerp(.45f, .95f, depth));
                batch.draw(streaks[i], x * world.width - w / 2, y * world.height - h / 2, w / 2, h / 2, w, h, 1, 1, heading);
            }
        } else starDistance = 0;
        batch.setColor(1, 1, 1, 1);
    }

    private static float repeat(float value, float length) { return value - (float)Math.floor(value / length) * length; }

    private void drawBackgroundTile(String key, float alpha) {
        CampaignAssets.Sprite sprite = assets.sprite(key);
        float ox = backgroundX % sprite.width, oy = backgroundY % sprite.height;
        batch.setColor(1, 1, 1, alpha);
        for (int y = -2; y <= 2; y++) for (int x = -2; x <= 2; x++)
            assets.draw(batch, key, world.width * .5f + x * sprite.width + ox,
                    world.height * .5f + y * sprite.height + oy, 1, 0);
    }

    private void renderPlayer() {
        CombatState player = world.player;
        if (player == null || player.deathRemaining > 0f) {
            return;
        }

        float alpha = 1.0f;
        if (player.protection > 0f) {
            alpha = (MathUtils.sin(world.clock * 28f) > 0f) ? 0.35f : 1.0f;
        }

        float drawX = world.playerX;
        float drawY = world.playerY;
        if (world.stage == CampaignWorld.Stage.BRIEFING) {
            drawX = (world.direction() == 0 ? .5f : .17f) * world.width;
            drawY = (world.direction() == 0 ? .17f : .5f) * world.height;
            batch.setColor(1, 1, 1, 1);
            assets.draw(batch, chassisKey, drawX, drawY, 1, world.rotation());
            return;
        }

        if (world.playerSpec != null && world.playerSpec.record != null) {
            renderCosmetics(drawX, drawY, alpha);
        }

        if (shieldKey != null && player.shield) {
            batch.setColor(1f, 1f, 1f, 0.8f);
            assets.draw(batch, shieldKey, drawX, drawY, 1.2f, world.clock * 60f);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private void renderCosmetics(float x, float y, float alpha) {
        float rotation = world.presentationRotation(), cos = MathUtils.cosDeg(rotation), sin = MathUtils.sinDeg(rotation);
        float lifetime = trail.getFloat("TrailSeconds");
        if (world.clock != lastTrailClock) {
            if (trailFrames.length > 0 && lifetime > 0 && world.player.canInput()
                    && com.badlogic.gdx.math.Vector2.dst2(x, y, lastPlayerX, lastPlayerY) > .0001f) {
                JsonValue socket = chassis.get("MovementTrailSocket");
                float sx = socket.getFloat("x"), sy = socket.getFloat("y");
                trailX[trailCursor] = x + sx * cos - sy * sin;
                trailY[trailCursor] = y + sx * sin + sy * cos;
                trailAge[trailCursor] = world.clock;
                trailCursor = (trailCursor + 1) % trailX.length;
            }
            lastTrailClock = world.clock; lastPlayerX = x; lastPlayerY = y;
        }
        if (trailFrames.length > 0 && lifetime > 0) for (int i = 0; i < trailX.length; i++) {
            float progress = (world.clock - trailAge[i]) / lifetime;
            if (progress < 0 || progress >= 1) continue;
            batch.setColor(1, 1, 1, alpha * (1 - progress) * .55f);
            assets.draw(batch, trailFrames[Math.min(trailFrames.length - 1, (int)(progress * trailFrames.length))],
                    trailX[i], trailY[i], .6f, rotation);
        }
        batch.setColor(1, 1, 1, alpha);
        if (engineFrames.length > 0) for (JsonValue socket : chassis.get("EngineSockets")) {
            float sx = socket.getFloat("x"), sy = socket.getFloat("y") - .35555f * chassis.getFloat("PlumeScale");
            assets.draw(batch, engineFrames[(int)(world.clock / .075f) % engineFrames.length],
                    x + sx * cos - sy * sin, y + sx * sin + sy * cos, chassis.getFloat("PlumeScale"), rotation);
        }
        assets.draw(batch, chassisKey, x, y, 1, rotation);
        batch.setColor(1, 1, 1, 1);
    }

    private void renderEnemies() {
        if (world.enemies == null) return;
        for (int i = 0; i < world.enemies.size; i++) {
            CampaignWorld.Enemy enemy = world.enemies.get(i);
            if (enemy == null || enemy.spec == null || enemy.spec.record == null) {
                continue;
            }
            PrefabVisual visual = getVisual(enemy.spec.record);
            if (visual == null) {
                continue;
            }

            visual.resetObjectHealth();
            float hpFraction = 1.0f;
            if (enemy.maxHp > 0.0001f) {
                hpFraction = MathUtils.clamp(enemy.hp / enemy.maxHp, 0f, 1f);
            }

            float alpha = (enemy.flash > 0.001f) ? 0.6f : 1.0f;
            if (enemy.boss != null) for (BossCombat.Node node : enemy.boss.nodes) visual.setObjectHealth(node.objectId, node.hp / node.maxHp);
            visual.draw(batch, enemy.x, enemy.y, enemy.scale,
                    world.rotation() + (enemy.spec.asteroid ? enemy.age * enemy.group.getFloat("VisualSpin", 0) : 0), world.clock, hpFraction, alpha);
            if (enemy.charge > 0 && enemy.spec.chargeFrames.length > 0) {
                String frame = enemy.spec.chargeFrames[(int)(world.clock / .07f) % enemy.spec.chargeFrames.length];
                CampaignAssets.Sprite sprite = assets.sprite(frame);
                float cueScale = world.height * 44 / 900 / sprite.width;
                assets.draw(batch, frame, enemy.x, enemy.y, cueScale, world.rotation());
            }
            if (enemy.directed != null && enemy.directed.warning && enemy.spec.warningFrames.length > 0) {
                String frame = enemy.spec.warningFrames[(int)(world.clock / .07f) % enemy.spec.warningFrames.length];
                CampaignAssets.Sprite sprite = assets.sprite(frame);
                boolean strike = enemy.directed.kind == com.alexei.spaceshooter.manager.DirectedSkill.KIND_STRIKE;
                assets.draw(batch, frame, strike ? enemy.directed.toX : enemy.directed.fromX,
                        strike ? enemy.directed.toY : enemy.directed.fromY, world.height * (strike ? 96 : 48) / 900 / sprite.width, world.rotation());
            }
            if (enemy.boss != null) renderBossCues(enemy);
            if (enemy.boss != null && enemy.boss.blocksCoreDamage()) {
                CampaignCatalog.Pose p = enemy.boss.shieldPose;
                CampaignAssets.Sprite sprite = assets.sprite(enemy.boss.shieldSprite);
                float x = enemy.x + (world.direction() == 0 ? p.x : p.y) * enemy.scale;
                float y = enemy.y + (world.direction() == 0 ? p.y : -p.x) * enemy.scale;
                batch.setColor(.15f, .8f, 1, enemy.flash > 0 ? .7f : .32f);
                batch.draw(sprite.region, x - sprite.pivotX, y - sprite.pivotY, sprite.pivotX, sprite.pivotY,
                        sprite.width, sprite.height, p.sx * enemy.scale, p.sy * enemy.scale, p.rotation + world.rotation());
                batch.setColor(1, 1, 1, 1);
            }
        }
    }

    private void renderBossCues(CampaignWorld.Enemy enemy) {
        for (BossCombat.SkillCue cue : enemy.boss.cues) {
            if (cue.phase == 0) continue;
            String[] frames = cue.frames[cue.phase == 1 ? 0 : cue.phase == 2 ? 1 : 2];
            if (frames.length == 0) continue;
            String frame = frames[Math.min(frames.length - 1, (int)(cue.age / cue.data.getFloat("frameSeconds")))];
            JsonValue tint = cue.data.get(cue.phase == 1 ? "prepareTint" : cue.phase == 2 ? "lockTint" : "attackTint");
            float alpha = tint.getFloat("a");
            if (cue.phase == 4) alpha *= Math.max(0, 1 - cue.age / (cue.data.getFloat("frameSeconds") * frames.length));
            float size = world.height / 900 * cue.data.getFloat("cueSizeAt900") * cue.data.getFloat("cueScale") * (cue.phase == 3 ? 1.15f : cue.phase == 2 ? 1 : .8f);
            float x = enemy.x + (world.direction() == 0 ? cue.pose.x : cue.pose.y) * enemy.scale;
            float y = enemy.y + (world.direction() == 0 ? cue.pose.y : -cue.pose.x) * enemy.scale;
            batch.setColor(tint.getFloat("r"), tint.getFloat("g"), tint.getFloat("b"), alpha);
            batch.draw(assets.sprite(frame).region, x - size * .5f, y - size * .5f, size * .5f, size * .5f,
                    size, size, 1, 1, world.rotation());
        }
        batch.setColor(1, 1, 1, 1);
    }

    private void renderCorpses() {
        for (CampaignWorld.Corpse corpse : world.corpses) {
            CampaignWorld.Enemy enemy = corpse.enemy;
            PrefabVisual visual = getVisual(enemy.spec.record); visual.resetObjectHealth();
            if (enemy.boss != null) for (BossCombat.Node node : enemy.boss.nodes) visual.setObjectHealth(node.objectId, node.hp / node.maxHp);
            if (enemy.spec.deathStages != null) for (JsonValue stage : enemy.spec.deathStages) {
                if (corpse.age < stage.getFloat("delay")) continue;
                JsonValue module = CampaignWorld.local(enemy.spec.record, stage.get("module"));
                if (module != null) visual.setObjectHealth(module.get("m_GameObject").getString("fileID"), 0);
            }
            visual.draw(batch, enemy.x, enemy.y, enemy.scale, world.rotation(), world.clock, 0, 1);
        }
    }

    private void renderBeams() {
        for (CampaignWorld.Enemy enemy : world.enemies) {
            if (enemy.directed != null && enemy.directed.kind == com.alexei.spaceshooter.manager.DirectedSkill.KIND_BEAM && (enemy.directed.active || enemy.directed.warning))
                beam(enemy.directed.fromX, enemy.directed.fromY, enemy.directed.toX, enemy.directed.toY, enemy.directed.width, enemy.directed.active);
            if (enemy.boss != null) for (BossCombat.Beam beam : enemy.boss.beams) {
                if (beam.active || beam.warning) beam(beam.fromX, beam.fromY, beam.toX, beam.toY, beam.width, beam.active);
            }
        }
    }

    private void beam(float ax, float ay, float bx, float by, float width, boolean active) {
        if (beamKey == null) throw new IllegalArgumentException("Missing beam artwork");
        float dx = bx - ax, dy = by - ay;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        batch.setColor(active ? .9f : 1, active ? .4f : .65f, active ? .7f : .2f, active ? .8f : .3f);
        batch.draw(assets.sprite(beamKey).region, ax, ay - width * .5f, 0, width * .5f,
                length, width, 1, 1, MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
        batch.setColor(1, 1, 1, 1);
    }

    private void renderProjectiles() {
        if (world.shots == null) return;
        for (int i = 0; i < world.shots.size; i++) {
            CampaignWorld.Shot shot = world.shots.get(i);
            if (shot == null || shot.sprite == null) {
                continue;
            }
            float angleDeg = MathUtils.atan2(shot.vy, shot.vx) * MathUtils.radiansToDegrees - 90f;
            batch.setColor(shot.r, shot.g, shot.b, shot.a);
            CampaignAssets.Sprite sprite = assets.sprite(shot.sprite);
            batch.draw(sprite.region, shot.x - sprite.pivotX, shot.y - sprite.pivotY,
                    sprite.pivotX, sprite.pivotY, sprite.width, sprite.height, shot.scaleX, shot.scaleY, angleDeg);
        }
        batch.setColor(1, 1, 1, 1);
    }

    private void renderPickups() {
        if (world.pickups == null) return;
        for (int i = 0; i < world.pickups.size; i++) {
            CampaignWorld.Pickup pickup = world.pickups.get(i);
            if (pickup == null) {
                continue;
            }

            String activeSprite = pickup.sprite;
            float scale = 1.0f;

            if (pickup.definition != null) {
                scale = pickup.definition.getFloat("VisualScale", 1.0f);
                String[] frames = getPickupFrames(pickup.definition);
                if (frames != null && frames.length > 0) {
                    int frameIndex = (int) (pickup.age * 10f) % frames.length;
                    activeSprite = frames[frameIndex];
                }
            }

            if (activeSprite != null) {
                assets.draw(batch, activeSprite, pickup.x, pickup.y, scale, 0f);
            }
        }
    }

    private void renderBursts() {
        if (world.bursts == null) return;
        for (int i = 0; i < world.bursts.size; i++) {
            CampaignWorld.Burst burst = world.bursts.get(i);
            if (burst == null || burst.frames == null || burst.frames.length == 0 || burst.duration <= 0.0001f) {
                continue;
            }
            if (burst.age < 0) continue;
            float progress = burst.age / burst.duration;
            int frameIndex = (int) (progress * burst.frames.length);
            if (frameIndex >= 0 && frameIndex < burst.frames.length) {
                String frame = burst.frames[frameIndex];
                if (frame != null) {
                    float scale = Float.isNaN(burst.growTo) ? burst.scale : MathUtils.lerp(burst.scale, burst.growTo, progress);
                    batch.setColor(1, 1, 1, burst.fade ? Math.min(1, (1 - progress) / (1 - burst.fadeFrom)) : 1);
                    assets.draw(batch, frame, burst.x, burst.y, scale, burst.rotation + burst.spin * burst.age);
                    batch.setColor(1, 1, 1, 1);
                }
            }
        }
    }

    private PrefabVisual getVisual(JsonValue record) {
        PrefabVisual visual = visuals.get(record);
        if (visual == null) {
            visual = new PrefabVisual(assets, record);
            visuals.put(record, visual);
        }
        return visual;
    }

    private String[] getPickupFrames(JsonValue definition) {
        String[] cached = pickupAnimationCache.get(definition);
        if (cached != null) {
            return cached;
        }
        JsonValue framesVal = definition.get("AnimationFrames");
        if (framesVal == null || !framesVal.isArray()) {
            return null;
        }
        Array<String> list = new Array<String>(String.class);
        for (JsonValue f = framesVal.child; f != null; f = f.next) {
            list.add(CampaignCatalog.spriteKey(f));
        }
        cached = list.toArray();
        pickupAnimationCache.put(definition, cached);
        return cached;
    }

    @Override
    public void dispose() {
        batch.dispose();
        visuals.clear();
        pickupAnimationCache.clear();
    }
}
