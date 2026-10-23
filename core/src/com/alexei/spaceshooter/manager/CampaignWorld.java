package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.Random;

public final class CampaignWorld {
    public enum Stage { BRIEFING, APPROACH, TRAVEL, BRAKE, REGION_TRAVEL, MISSION_TRAVEL, INTRO, RUNNING, DRAIN, PROJECTILE_DRAIN, BOSS_POST_DEATH, REWARD_GRACE, CLEAR, HANDOFF, GAME_OVER, RESULT }
    public interface Listener {
        void sound(int cue);
        void checkpoint(CampaignStore.State state);
        default int creditShards(int cumulative, int balance) { return balance; }
    }
    public static final class Spec {
        public JsonValue record, definition, fire, damage, pickupTable, firePresentation, telegraph;
        public String[] smokeFrames;
        public String[][] asteroidFrames;

        public String asteroidSprite(float scale, float hpFraction) {
            if (asteroidFrames == null || asteroidFrames[0].length != 4) return null;
            int index = scale < .875f ? 0 : scale < 1.275f ? 1 : scale < 1.8f ? 2 : 3;
            int stage = hpFraction <= damage.getFloat("criticalHealthFraction", .33f) ? 2 : hpFraction <= damage.getFloat("damagedHealthFraction", .66f) ? 1 : 0;
            return asteroidFrames[stage][index];
        }
        public String hull, accent;
        public CampaignCatalog.Pose accentPose;
        public String[] deathFrames, hitFrames;
        public String[] chargeFrames, warningFrames, launchFrames;
        public float deathSeconds = .6f;
        public final Array<Fragment> fragments = new Array<>();
        public JsonValue deathStages, armor;
        public String[] armorFrames;
        public String[][] armorFragments;
        public float halfWidth, halfHeight, colliderX, colliderY, muzzleX, muzzleY, rootScale = 1;
        public boolean circle;
        public int missileTier;
        public boolean bodyDamages, fires, asteroid;
    }
    public static final class Fragment {
        public String[] frames;
        public float scale;
    }
    public static final class Corpse {
        public Enemy enemy;
        public float age, duration;
    }
    public static final class Enemy {
        public Spec spec;
        public DirectedSkill directed;
        public BossCombat boss;
        public final Array<EscortController> escorts = new Array<>();
        public float combatX, combatY, combatFromX, combatFromY, combatTargetX, combatTargetY, repositionClock, repositionDuration, motionMultiplier = 1;
        public float entryOriginX = Float.NaN, entryOriginY, retreatX, retreatY, retreatClock;
        public boolean retreating, motionPaused;
        public JsonValue group, sequence, elite, rewards, definition;
        public float spawnedAt;
        public float x, y, age, visibleSeconds, offsetX, offsetY, scale, hp, maxHp, cooldown, charge, flash;
        public boolean required, optional, grantsRewards, canShoot, holding, expired;
        public int burstRemaining, armorEmitted;
        public float aimX, aimY;
    }
    public static final class Shot implements Pool.Poolable {
        public float x, y, previousX, previousY, vx, vy, life, age, damage, radius, scaleX, scaleY, homing, splash, trackingDelay, trackingRange;
        public boolean hostile;
        public int branch;
        public Enemy target;
        public BossCombat.Node targetNode;
        public float launchTurnRate, halfWidth, halfHeight, frameSeconds;
        public String[] visualFrames;
        public float r = 1, g = 1, b = 1, a = 1;
        public String sprite;
        @Override public void reset() { sprite = null; target = null; targetNode = null; age = homing = splash = launchTurnRate = 0; }
    }
    public static final class Pickup implements Pool.Poolable {
        public JsonValue definition;
        public String sprite;
        public float x, y, age, vx, vy, remaining, opacity = 1;
        private float ejectionForward;
        public int bounceCount;
        private int direction;
        private float previousPlayerX, previousPlayerY, previousX, previousY, ejectionRemaining, graceRemaining = -1;
        private SupplyCapture supply;
        @Override public void reset() {
            definition = null; sprite = null; supply = null; age = vx = vy = ejectionRemaining = ejectionForward = 0;
            bounceCount = 0; graceRemaining = -1; opacity = 1;
        }
    }
    private static final class SupplyCapture {
        boolean collected;
    }
    public static final class Burst implements Pool.Poolable {
        public String[] frames;
        public float x, y, age, duration, scale, rotation, vx, vy, ax, ay, spin, fadeFrom, growTo = Float.NaN;
        public boolean fade, followPlayer;
        public int soundOnStart = -1;
        @Override public void reset() { frames = null; age = vx = vy = ax = ay = spin = fadeFrom = 0; growTo = Float.NaN; fade = followPlayer = false; soundOnStart = -1; }
    }
    private static final class Spawn {
        JsonValue group;
        int index;
        float at;
        boolean optional, spawned;
    }
    public final CampaignCatalog catalog;
    public final CampaignStore.State save;
    public final CombatState player;
    public final Array<Enemy> enemies = new Array<>();
    public final Array<Shot> shots = new Array<>(false, 128);
    public final Array<Pickup> pickups = new Array<>(false, 32);
    public final Array<Burst> bursts = new Array<>(false, 64);
    public final Array<Corpse> corpses = new Array<>(false, 2);
    public final Spec playerSpec;
    public Stage stage;
    public JsonValue mission, encounter;
    public float width = 24, height = 12, playerX, playerY, clock, encounterClock, stageClock;
    public float targetOrtho, ortho, shake, missileFlight, missileCooldown;
    public float missileStartX, missileStartY;
    public float missileFlightDuration = .32f;
    public float missileDetonationRemaining;
    public float missileDetonationDuration = .45f;
    public float missileDetonateX, missileDetonateY;
    public float boundaryPressureX, boundaryPressureY;
    public boolean devGodMode, devFakeShots;
    private int devBossRoute;
    public String reserveMissileKey;
    public boolean held, replay;
    public int revision;
    public String announcement = "", pickupAnnouncement = "";
    public float pickupCueRemaining;
    public float cinematicProgress;
    private float travelSeconds, entryLeadRemaining, cleanupOverlap, drainDuration, titleRemaining;
    private boolean regionTravel;
    public int journeyPhase;
    public float journeySpeed, journeyPalette, journeyReveal, journeyHeading;
    private float journeyDepartureX, journeyDepartureY;
    private final float[] journeyDurations = new float[5];
    private float respawnTargetX, respawnTargetY;
    private static final float[] SAFE_X = {.5f, .25f, .75f, .5f, .25f, .75f, .5f, .25f, .75f};
    private static final float[] SAFE_Y = {.18f, .18f, .18f, .35f, .35f, .35f, .5f, .5f, .5f};
    private float viewElapsed, viewDuration, viewFromSize;
    public float playableX, playableY, playableWidth = 1, playableHeight = 1;
    private float fromPlayableX, fromPlayableY, fromPlayableWidth = 1, fromPlayableHeight = 1;
    private float targetPlayableX, targetPlayableY, targetPlayableWidth = 1, targetPlayableHeight = 1;
    private float footprintWidth, footprintHeight;
    private final Listener listener;
    private final RewardRules rewards;
    private final JsonValue defaultDrops;
    private final int activePickupLimit;
    private final float collectionRadius, expiryFadeSeconds;
    private boolean hasKilledCandidate, bossVictoryPlayed;
    private float lastKillX, lastKillY;
    public final String[][] muzzleFrames = new String[3][];
    public final String[] coinFrames, upgradeFrames, shieldFrames;
    public final String[] hitPulseFrames, hitPlasmaFrames, hitHomingFrames, hitRockFrames, hitShieldFrames;
    public final String[] missileSplashFrames, rockShatterFrames, heavyBurstFrames, smokeDebrisFrames;
    public float muzzleRemaining;
    public float boundaryPressedSeconds;
    private boolean boundaryPressed;
    private final JsonValue[] weapons = new JsonValue[3];
    private float playerMuzzleX, playerMuzzleY;
    private final ObjectMap<String, Spec> specs = new ObjectMap<>();
    private static final class ProjectileShape {
        float hw, hh, frameSeconds;
        String[] frames = new String[0];
    }
    private final ObjectMap<JsonValue, ProjectileShape> projectileShapes = new ObjectMap<>();
    private final Array<Spawn> schedule = new Array<>();
    private final Pool<Shot> shotPool = new Pool<Shot>(128) { protected Shot newObject() { return new Shot(); } };
    private final Pool<Pickup> pickupPool = new Pool<Pickup>(32) { protected Pickup newObject() { return new Pickup(); } };
    private final Pool<Burst> burstPool = new Pool<Burst>(64) { protected Burst newObject() { return new Burst(); } };
    private final Vector2 scratch = new Vector2();
    private final Vector2 entryScratch = new Vector2();
    private final Random random = new Random(521);
    private float shotCooldown, droneCooldown, fireSpacing;
    private int shooterCursor, volleyShots, encounterShards, requiredKilled, requiredTotal;
    private float[] beatStarts;
    private float optionalResolvedAt = -1;
    private boolean[] optionalFired;

    public CampaignWorld(CampaignCatalog catalog, CampaignStore.State state, boolean replay, Listener listener) {
        CampaignStore.validate(state);
        this.catalog = catalog;
        this.save = state;
        this.replay = replay;
        this.listener = listener;
        player = new CombatState(state);
        rewards = new RewardRules(catalog, player, 410);
        JsonValue rewardDirector = catalog.component(catalog.record("Scenes/02_Gameplay.unity"), "RewardDirector");
        defaultDrops = catalog.resolve(rewardDirector.get("defaultDrops"));
        activePickupLimit = rewardDirector.getInt("activePickupLimit");
        JsonValue pickupConfig = catalog.component(catalog.referencedRecord(rewardDirector.get("pickupPrefab")), "RewardPickup");
        collectionRadius = pickupConfig.getFloat("collectionRadius");
        expiryFadeSeconds = pickupConfig.getFloat("expiryFadeSeconds");
        playerSpec = spec(catalog.record("Prefabs/Player/Player.prefab"));
        JsonValue footprint = catalog.component(playerSpec.record, "PlayerMovementBounds").get("footprintSize");
        footprintWidth = footprint.getFloat("x"); footprintHeight = footprint.getFloat("y");
        weapons[0] = catalog.asset("Data/Weapons/PulseLaser.asset");
        weapons[1] = catalog.asset("Data/Weapons/PlasmaSpread.asset");
        weapons[2] = catalog.asset("Data/Weapons/HomingMissile.asset");
        JsonValue feedback = catalog.component(playerSpec.record, "PlayerCombatPresentation");
        muzzleFrames[0] = frames(feedback.get("pulseMuzzleFrames"));
        muzzleFrames[1] = frames(feedback.get("plasmaMuzzleFrames"));
        muzzleFrames[2] = frames(feedback.get("missileMuzzleFrames"));
        coinFrames = frames(feedback.get("coinRewardFrames"));
        upgradeFrames = frames(feedback.get("upgradeRewardFrames"));
        shieldFrames = frames(feedback.get("shieldRewardFrames"));
        hitPulseFrames = framesByName(catalog, "hit-pulse_");
        hitPlasmaFrames = framesByName(catalog, "hit-plasma_");
        hitHomingFrames = framesByName(catalog, "hit-homing_");
        hitRockFrames = framesByName(catalog, "hit-rock_");
        hitShieldFrames = framesByName(catalog, "hit-shield_");
        missileSplashFrames = framesByName(catalog, "missile-splash_");
        rockShatterFrames = framesByName(catalog, "rock-shatter_");
        heavyBurstFrames = framesByName(catalog, "heavy-burst_");
        smokeDebrisFrames = framesByName(catalog, "smoke-debris_");
        if (catalog.root.has("sprites")) {
            for (JsonValue entry = catalog.root.get("sprites").child; entry != null; entry = entry.next) {
                String path = entry.getString("path", "");
                if (path.endsWith("Projectiles/reserve-missile.png")) reserveMissileKey = entry.name;
            }
        }
        for (JsonValue option : catalog.asset("Data/Cosmetics/ShipCosmetics.asset").get("Options"))
            if (option.getString("Id").equals(state.equippedCosmetics[0])) {
                playerMuzzleX = option.get("MuzzleSocket").getFloat("x");
                playerMuzzleY = option.get("MuzzleSocket").getFloat("y");
            }
        beginMission();
    }

    public void introduceMission() {
        if (stage == Stage.RESULT || save.encounter != 0 || !mission.get("Intro").getBoolean("Enabled")) return;
        stage = Stage.BRIEFING; stageClock = 0;
        announcement = mission.getString("LocationName", "HÀNH TINH PHÍA TRƯỚC");
        requestView(mission.get("Intro").get("ApproachView"), 0);
        resize(width / height); checkpointPosition();
        held = false; revision++;
    }

    public boolean acceptBriefing() {
        if (!awaitingContinue() || (stage == Stage.BRIEFING && stageClock < mission.get("Intro").getFloat("ContinueDelaySeconds"))) return false;
        if (stage == Stage.BRIEFING) {
            playerX = (direction() == 0 ? .5f : .17f) * width;
            playerY = (direction() == 0 ? .17f : .5f) * height;
        }
        stage = Stage.APPROACH; stageClock = 0; announcement = "";
        requestView(mission.get("Intro").get("TravelView"), regionTravel ? .7f : mission.get("Intro").getFloat("ApproachSeconds"));
        if (!regionTravel) sound(35); sound(40); revision++; return true;
    }

    public boolean readyToContinue() {
        return awaitingContinue() && (stage != Stage.BRIEFING || stageClock >= mission.get("Intro").getFloat("ContinueDelaySeconds"));
    }

    public boolean awaitingContinue() { return stage == Stage.BRIEFING || stage == Stage.REGION_TRAVEL && journeyPhase == 5; }

    public boolean cinematic() {
        return stage == Stage.BRIEFING || stage == Stage.APPROACH || stage == Stage.TRAVEL || stage == Stage.BRAKE || stage == Stage.REGION_TRAVEL;
    }

    public boolean gameplayInput() {
        return stage != Stage.BRIEFING && stage != Stage.REGION_TRAVEL && stage != Stage.GAME_OVER && stage != Stage.RESULT;
    }

    public float presentationRotation() {
        if (stage == Stage.REGION_TRAVEL) return journeyHeading;
        return rotation();
    }

    public void sound(int cue) { listener.sound(cue); }

    boolean canStartBossAttack() {
        if (stage != Stage.RUNNING || !player.canInput() || player.enemyFireGrace > 0 || fireSpacing > 0 || volleyShots >= encounter.getInt("ShotsPerVolley")) return false;
        int busy = 0;
        for (Enemy e : enemies) if (e.charge > 0 || (e.directed != null && e.directed.busy()) || (e.boss != null && e.boss.busy())) busy++;
        return busy < encounter.getInt("ActiveShooterLimit");
    }

    void recordBossAttack() {
        fireSpacing = ++volleyShots >= encounter.getInt("ShotsPerVolley") ? encounter.getFloat("VolleyPause") : encounter.getFloat("ShotSpacing");
    }

    void reposition(Enemy e, float x, float y, float seconds, float motionMultiplier) {
        e.combatFromX = e.combatX; e.combatFromY = e.combatY;
        e.combatTargetX = x; e.combatTargetY = y;
        e.repositionClock = 0; e.repositionDuration = seconds;
        e.motionMultiplier = motionMultiplier;
    }

    private static JsonValue copyJson(JsonValue value) {
        return new JsonReader().parse(value.toJson(JsonWriter.OutputType.json));
    }

    private static void replace(JsonValue object, String field, JsonValue value) {
        object.remove(field); object.addChild(field, value);
    }

    Enemy spawnEscort(JsonValue prefab, JsonValue sequence, float x, float y, float slotOffset) {
        if (stage != Stage.RUNNING) return null;
        JsonValue group = copyJson(encounter.get("Groups").get(0));
        replace(group, "EnemyPrefab", copyJson(prefab)); replace(group, "Sequence", copyJson(sequence));
        for (String key : new String[]{"Elite", "Plan", "RewardOverride", "EnemyDefinition"}) replace(group, key, new JsonReader().parse("{fileID:0}"));
        replace(group, "Count", new JsonValue(1)); group.get("Required").set(false); group.get("GrantsRewards").set(false);
        replace(group, "SpawnScale", new JsonValue(1)); replace(group, "HpOverride", new JsonValue(0)); replace(group, "FirstSlot", new JsonValue(0));
        replace(group.get("Anchor"), "x", new JsonValue(slotOffset)); replace(group.get("Anchor"), "y", new JsonValue(0));
        Spawn spawn = new Spawn(); spawn.group = group;
        Enemy escort = spawn(spawn);
        escort.entryOriginX = x / width; escort.entryOriginY = y / height;
        flight(escort);
        return escort;
    }

    void retreat(Enemy enemy) {
        if (enemy.retreating) return;
        enemy.retreating = true; enemy.retreatX = enemy.x; enemy.retreatY = enemy.y;
        enemy.retreatClock = 0; enemy.canShoot = enemy.holding = enemy.motionPaused = false;
    }

    public int direction() { return regionTravel && stage != Stage.REGION_TRAVEL ? 1 : mission.getInt("Direction"); }
    public float rotation() { return direction() == 1 ? -90 : 0; }
    public JsonValue weapon() { return weapons[save.activeWeapon]; }
    public JsonValue thermal() { return catalog.resolve(weapon().get("thermalProfile")); }

    public Spec spec(JsonValue record) {
        String path = record.getString("path");
        Spec cached = specs.get(path);
        if (cached != null) return cached;
        Spec s = new Spec();
        s.record = record;
        JsonValue asteroid = catalog.component(record, "AsteroidVisualMotion");
        s.asteroid = asteroid != null;
        if (asteroid != null && asteroid.get("sizeSprites").size == 4)
            s.asteroidFrames = new String[][]{frames(asteroid.get("sizeSprites")), frames(asteroid.get("damagedSizeSprites")), frames(asteroid.get("criticalSizeSprites"))};
        JsonValue health = catalog.rootComponent(record, "EnemyHealth");
        if (health != null) {
            s.definition = catalog.resolve(health.get("definition"));
            s.missileTier = s.definition.getInt("missileTier", 0);
            s.bodyDamages = s.definition.getBoolean("bodyDamagesPlayer", true);
        }
        JsonValue attack = catalog.rootComponent(record, "SingleFireAttack");
        if (attack != null) {
            s.fire = catalog.resolve(attack.get("definition"));
            s.fires = attack.getInt("m_Enabled", 1) != 0 && s.fire != null;
            JsonValue muzzle = local(record, attack.get("muzzle"));
            if (muzzle != null) {
                CampaignCatalog.Pose pose = CampaignCatalog.pose(record, muzzle.get("m_GameObject").getString("fileID"));
                s.muzzleX = pose.x; s.muzzleY = pose.y;
            }
        }
        JsonValue flight = catalog.rootComponent(record, "EntryHoldFlight");
        JsonValue collider = flight == null ? null : local(record, flight.get("collisionShape"));
        if (collider == null) for (JsonValue doc : record.get("documents"))
            if (doc.getString("type").equals("BoxCollider2D") || doc.getString("type").equals("CircleCollider2D")) { collider = doc.get("data"); break; }
        if (collider == null) throw new IllegalArgumentException("Missing collision shape: " + path);
        s.circle = collider.has("m_Radius");
        s.halfWidth = s.circle ? collider.getFloat("m_Radius") : collider.get("m_Size").getFloat("x") * .5f;
        s.halfHeight = s.circle ? s.halfWidth : collider.get("m_Size").getFloat("y") * .5f;
        s.colliderX = collider.get("m_Offset").getFloat("x");
        s.colliderY = collider.get("m_Offset").getFloat("y");
        for (JsonValue doc : record.get("documents")) {
            JsonValue d = doc.get("data");
            if (doc.getString("type").equals("Transform") && d.get("m_Father").getString("fileID").equals("0"))
                s.rootScale = d.get("m_LocalScale").getFloat("x");
        }
        s.damage = catalog.rootComponent(record, health == null ? "PlayerDamagePresentation" : "EnemyDamagePresentation");
        if (s.damage != null && s.damage.has("hull")) {
            JsonValue hull = local(record, s.damage.get("hull"));
            if (hull != null) s.hull = CampaignCatalog.spriteKey(hull.get("m_Sprite"));
        }
        JsonValue modifierPresentation = catalog.component(record, "EnemyModifierPresentation");
        if (modifierPresentation != null) {
            JsonValue accent = local(record, modifierPresentation.get("accent"));
            s.accent = CampaignCatalog.spriteKey(accent.get("m_Sprite"));
            s.accentPose = CampaignCatalog.pose(record, accent.get("m_GameObject").getString("fileID"));
        }
        JsonValue fireFeedback = s.firePresentation = catalog.component(record, "EnemyFirePresentation");
        s.chargeFrames = frames(fireFeedback == null ? null : fireFeedback.get("chargeFrames"));
        JsonValue hits = catalog.component(record, "EnemyHitPresentation");
        s.hitFrames = frames(hits == null ? null : hits.get("hitFrames"));
        s.smokeFrames = frames(hits == null ? null : hits.get("smokeFrames"));
        JsonValue telegraph = s.telegraph = catalog.component(record, "DirectedAttackTelegraph");
        s.warningFrames = frames(telegraph == null ? null : telegraph.get("warningFrames"));
        s.launchFrames = frames(telegraph == null ? null : telegraph.get("launchFrames"));
        JsonValue death = catalog.component(record, health == null ? "PlayerDeathPresentation" : "BossDeathSequence");
        s.deathFrames = frames(death == null ? null : death.get("explosionFrames"));
        if (death != null) s.deathSeconds = death.getFloat(health == null ? "seconds" : "duration");
        if (death != null && death.has("fragments")) for (JsonValue ref : death.get("fragments")) {
            JsonValue renderer = local(record, ref);
            if (renderer == null || !CampaignCatalog.hasSpriteReference(renderer.get("m_Sprite"))) continue;
            Fragment fragment = new Fragment(); fragment.frames = new String[]{CampaignCatalog.spriteKey(renderer.get("m_Sprite"))};
            fragment.scale = CampaignCatalog.pose(record, renderer.get("m_GameObject").getString("fileID")).sx;
            s.fragments.add(fragment);
        }
        JsonValue staged = catalog.component(record, "StagedDestructionPresentation");
        s.deathStages = staged == null ? null : staged.get("stages");
        s.armor = catalog.component(record, "BossArmorBreakPresentation");
        if (s.armor != null) {
            s.armorFrames = frames(s.armor.get("burstFrames"));
            s.armorFragments = new String[s.armor.get("fragments").size][];
            for (int i = 0; i < s.armorFragments.length; i++) {
                JsonValue fragment = local(record, s.armor.get("fragments").get(i));
                s.armorFragments[i] = new String[]{CampaignCatalog.spriteKey(fragment.get("m_Sprite"))};
            }
        }
        JsonValue reward = catalog.component(record, "EnemyRewardSource");
        if (reward != null && reward.has("table")) s.pickupTable = catalog.resolve(reward.get("table"));
        if (death == null && health != null) {
            JsonValue destruction = catalog.component(catalog.record("Scenes/02_Gameplay.unity"), "AsteroidDestructionPresentation");
            boolean supply = !s.bodyDamages && s.pickupTable != null && s.pickupTable.getInt("Source") == 2;
            s.deathFrames = frames(destruction.get(supply ? "supplyFrames" : s.asteroid ? "frames" : s.missileTier == 1 ? "heavyFrames" : "enemyFrames"));
            s.deathSeconds = supply || s.asteroid ? destruction.getFloat("seconds") : .5f;
        }
        specs.put(path, s);
        return s;
    }

    public static JsonValue local(JsonValue record, JsonValue ref) {
        if (ref == null) return null;
        String id = ref.getString("fileID");
        if (id.equals("0")) return null;
        for (JsonValue doc : record.get("documents")) if (doc.getString("fileID").equals(id)) return doc.get("data");
        throw new IllegalArgumentException("Missing local object " + id + " in " + record.getString("path"));
    }

    public static String[] frames(JsonValue refs) {
        if (refs == null) return new String[0];
        String[] frames = new String[refs.size];
        for (int i = 0; i < refs.size; i++) frames[i] = CampaignCatalog.spriteKey(refs.get(i));
        return frames;
    }

    public static String[] framesByName(CampaignCatalog catalog, String prefix) {
        Array<JsonValue> matches = new Array<>();
        if (catalog != null && catalog.root != null && catalog.root.has("sprites")) {
            for (JsonValue entry = catalog.root.get("sprites").child; entry != null; entry = entry.next) {
                if (entry.getString("name", "").startsWith(prefix)) matches.add(entry);
            }
            matches.sort((a, b) -> a.getString("name", "").compareTo(b.getString("name", "")));
        }
        String[] keys = new String[matches.size];
        for (int i = 0; i < matches.size; i++) keys[i] = matches.get(i).name;
        return keys;
    }

    public void resize(float aspect) {
        float oldWidth = width, oldHeight = height;
        height = ortho * 2; width = height * aspect;
        float dx = (width - oldWidth) * .5f, dy = (height - oldHeight) * .5f;
        playerX += dx; playerY += dy;
        for (Shot shot : shots) { shot.x += dx; shot.previousX += dx; shot.y += dy; shot.previousY += dy; }
        for (Pickup pickup : pickups) {
            pickup.x += dx; pickup.y += dy;
            pickup.previousX += dx; pickup.previousY += dy;
            pickup.previousPlayerX += dx; pickup.previousPlayerY += dy;
        }
        for (Burst burst : bursts) { burst.x += dx; burst.y += dy; }
        clampPlayer();
    }

    private void checkpointPosition() {
        FlightMath.orient(.5f, .18f, direction(), scratch);
        playerX = (playableX + playableWidth * scratch.x) * width;
        playerY = (playableY + playableHeight * scratch.y) * height;
        clampPlayer();
    }

    private boolean chooseRespawnTarget() {
        for (int anchor = 0; anchor < SAFE_X.length; anchor++) {
            FlightMath.orient(SAFE_X[anchor], SAFE_Y[anchor], direction(), scratch);
            float x = (playableX + playableWidth * scratch.x) * width;
            float y = (playableY + playableHeight * scratch.y) * height;
            boolean clear = true;
            for (Enemy enemy : enemies) if (Math.abs(enemy.x - x) < halfWidth(enemy) + playerSpec.halfWidth
                    && Math.abs(enemy.y - y) < halfHeight(enemy) + playerSpec.halfHeight) { clear = false; break; }
            if (anchor == 0 || clear) { respawnTargetX = x; respawnTargetY = y; }
            if (clear) return true;
        }
        return false;
    }

    private void clampPlayer() {
        float hw = (direction() == 0 ? footprintWidth : footprintHeight) * playerSpec.rootScale * .5f;
        float hh = (direction() == 0 ? footprintHeight : footprintWidth) * playerSpec.rootScale * .5f;
        float minX = playableX * width + hw, maxX = (playableX + playableWidth) * width - hw;
        float minY = playableY * height + hh, maxY = (playableY + playableHeight) * height - hh;
        if (!held || playerY < maxY - .01f) boundaryPressureY = 0;
        if (!held || playerX < maxX - .01f) boundaryPressureX = 0;
        playerX = minX > maxX ? (minX + maxX) * .5f : MathUtils.clamp(playerX, minX, maxX);
        playerY = minY > maxY ? (minY + maxY) * .5f : MathUtils.clamp(playerY, minY, maxY);
    }

    public float boundaryCeilingY() {
        return (playableY + playableHeight) * height - ((direction() == 0 ? footprintHeight : footprintWidth) * playerSpec.rootScale * .5f);
    }

    public float boundaryRightX() {
        return (playableX + playableWidth) * width - ((direction() == 0 ? footprintWidth : footprintHeight) * playerSpec.rootScale * .5f);
    }

    private void requestView(JsonValue view, float seconds) {
        viewFromSize = ortho; viewElapsed = 0; viewDuration = seconds;
        fromPlayableX = playableX; fromPlayableY = playableY;
        fromPlayableWidth = playableWidth; fromPlayableHeight = playableHeight;
        targetOrtho = view.getFloat("orthographicSize");
        JsonValue rect = view.get("playableViewport");
        targetPlayableX = rect.getFloat("x"); targetPlayableY = rect.getFloat("y");
        targetPlayableWidth = rect.getFloat("width"); targetPlayableHeight = rect.getFloat("height");
    }

    private void beginMission() {
        mission = catalog.missions[save.mission - 1];
        JsonValue firstEncounter = catalog.resolve(mission.get("Encounters").get(save.encounter));
        JsonValue view = catalog.resolve(firstEncounter.get("CameraOverride"));
        if (view == null) view = catalog.resolve(mission.get("Camera"));
        ortho = targetOrtho = view.get("View").getFloat("orthographicSize");
        requestView(view.get("View"), 0);
        resize(width / height);
        checkpointPosition();
        if (save.missionCleared) {
            encounter = catalog.resolve(mission.get("Encounters").get(save.encounter));
            stage = Stage.RESULT;
            announcement = save.completed ? "HOÀN THÀNH CHIẾN DỊCH" : "HOÀN THÀNH NHIỆM VỤ";
            revision++;
            if (!save.completed && !replay) {
                boolean ready = save.regionReady;
                nextMission();
                if (ready && regionTravel) {
                    journeyPhase = 5; journeyHeading = -90; journeySpeed = .015f; journeyPalette = journeyReveal = 1;
                    announcement = "HÀNH TINH TIẾP THEO";
                    requestView(mission.get("Intro").get("ApproachView"), 0);
                    resize(width / height);
                    playerX = width * .18f; playerY = height * .5f;
                }
            }
        } else beginEncounter();
    }

    private void beginEncounter() {
        clearActors();
        encounter = catalog.resolve(mission.get("Encounters").get(save.encounter));
        JsonValue view = catalog.resolve(encounter.get("CameraOverride"));
        if (view == null) view = catalog.resolve(mission.get("Camera"));
        requestView(view.get("View"), save.encounter == 0 ? 0 : view.getFloat("TransitionSeconds"));
        schedule.clear(); requiredKilled = requiredTotal = 0;
        for (JsonValue group : encounter.get("Groups")) if (group.getBoolean("Required")) requiredTotal += group.getInt("Count");
        beatStarts = new float[encounter.get("EntryBeats").size + 1];
        java.util.Arrays.fill(beatStarts, -1);
        beatStarts[0] = 0;
        optionalResolvedAt = -1;
        encounterShards = 0;
        for (JsonValue group : encounter.get("Groups")) addGroup(group, 0);
        JsonValue hazards = catalog.resolve(encounter.get("HazardStream"));
        if (hazards != null) for (JsonValue group : HazardSchedule.build(hazards, encounter.getFloat("Duration"))) addGroup(group, 0);
        schedule.sort((a, b) -> Float.compare(a.at, b.at));
        int candidates = 0;
        for (JsonValue group : encounter.get("Groups")) {
            JsonValue reference = group.get("EnemyPrefab");
            if (!reference.has("guid")) reference = encounter.get("ScoutPrefab");
            Spec spec = spec(catalog.referencedRecord(reference));
            if (group.getBoolean("GrantsRewards") && spec.bodyDamages && spec.missileTier != 2 && !spec.asteroid) candidates += group.getInt("Count");
        }
        rewards.beginEncounter(mission.getString("Id"), encounter.getString("Id"), candidates, encounter.getInt("GuaranteedMajorCount"), encounter.getInt("GuaranteedMajorByKill", 0));
        hasKilledCandidate = bossVictoryPlayed = false;
        optionalFired = new boolean[encounter.get("OptionalEvents").size];
        shooterCursor = volleyShots = 0;
        stageClock = encounterClock = fireSpacing = shotCooldown = droneCooldown = 0;
        stage = Stage.RUNNING;
        entryLeadRemaining = encounter.getFloat("EntryLeadSeconds");
        titleRemaining = encounter.getFloat("TitleSeconds");
        cleanupOverlap = 0;
        announcement = encounter.getString("TransitionTitle", mission.getString("DisplayName"));
        if (!replay && save.lives > 0) listener.checkpoint(save);
        revision++;
    }

    private void addGroup(JsonValue group, float offset) {
        for (int i = 0; i < group.getInt("Count"); i++) {
            Spawn spawn = new Spawn();
            spawn.group = group; spawn.index = i;
            spawn.at = group.getInt("EntryBeat", 0) == 0 ? offset + group.getFloat("StartTime") + i * group.getFloat("Stagger") : Float.POSITIVE_INFINITY;
            schedule.add(spawn);
        }
    }

    public void move(float dx, float dy) {
        if (!player.canInput() || !gameplayInput()) return;
        float hw = (direction() == 0 ? footprintWidth : footprintHeight) * playerSpec.rootScale * .5f;
        float hh = (direction() == 0 ? footprintHeight : footprintWidth) * playerSpec.rootScale * .5f;
        float maxX = (playableX + playableWidth) * width - hw;
        float maxY = (playableY + playableHeight) * height - hh;
        boundaryPressureX = (playerX + dx >= maxX - .05f && dx > 0) ? 1f : 0f;
        boundaryPressureY = (playerY + dy >= maxY - .05f && dy > 0) ? 1f : 0f;
        playerX += dx; playerY += dy;
        clampPlayer();
    }

    public void update(float dt) {
        if (!Float.isFinite(dt) || dt < 0 || dt > .1f) throw new IllegalArgumentException("Invalid simulation step");
        if (stage == Stage.GAME_OVER || stage == Stage.RESULT) return;
        tickDevBossRoute();
        clock += dt; pickupCueRemaining = Math.max(0, pickupCueRemaining - dt);
        stageClock += dt;
        shake = Math.max(0, shake - dt * 2);
        muzzleRemaining = Math.max(0, muzzleRemaining - dt);
        updateBursts(dt);
        if (save.lives == 0) {
            player.tick(dt, false, thermal());
            updateEnemies(dt); updateShots(dt); updateMissile(dt);
            if (player.deathRemaining <= 0) {
                stage = Stage.GAME_OVER; stageClock = 0; held = false;
                announcement = "THẤT BẠI"; sound(39); revision++;
            }
            return;
        }
        viewElapsed += dt;
        float viewProgress = viewDuration <= 0 ? 1 : Math.min(1, viewElapsed / viewDuration);
        viewProgress = viewProgress * viewProgress * (3 - 2 * viewProgress);
        ortho = MathUtils.lerp(viewFromSize, targetOrtho, viewProgress);
        playableX = MathUtils.lerp(fromPlayableX, targetPlayableX, viewProgress);
        playableY = MathUtils.lerp(fromPlayableY, targetPlayableY, viewProgress);
        playableWidth = MathUtils.lerp(fromPlayableWidth, targetPlayableWidth, viewProgress);
        playableHeight = MathUtils.lerp(fromPlayableHeight, targetPlayableHeight, viewProgress);
        float aspect = width / height;
        resize(aspect);
        clampPlayer();
        if (titleRemaining > 0) {
            titleRemaining -= dt;
            if (titleRemaining <= 0 && !cinematic()) { announcement = ""; revision++; }
        }
        if (cinematic()) {
            JsonValue intro = mission.get("Intro");
            if (stage == Stage.REGION_TRAVEL) {
                tickJourney();
            } else if (stage != Stage.BRIEFING) {
                float duration = regionTravel ? (stage == Stage.APPROACH ? .7f : stage == Stage.TRAVEL ? 1.8f : .8f)
                        : intro.getFloat(stage == Stage.APPROACH ? "ApproachSeconds" : stage == Stage.TRAVEL ? "TravelSeconds" : "BrakeSeconds");
                cinematicProgress = Math.min(1, stageClock / duration);
                if (stageClock >= duration) {
                    stageClock = cinematicProgress = 0;
                    if (stage == Stage.APPROACH) { stage = Stage.TRAVEL; announcement = ""; sound(36); }
                    else if (stage == Stage.TRAVEL) {
                        stage = Stage.BRAKE;
                        JsonValue destination = regionTravel ? catalog.missions[save.mission] : mission;
                        JsonValue first = regionTravel ? catalog.resolve(destination.get("Encounters").get(0)) : encounter;
                        JsonValue override = catalog.resolve(first.get("CameraOverride"));
                        requestView((override == null ? catalog.resolve(destination.get("Camera")) : override).get("View"), regionTravel ? .8f : intro.getFloat("BrakeSeconds"));
                        announcement = ""; sound(37);
                    } else {
                        if (regionTravel) { advanceMission(); intro = mission.get("Intro"); }
                        stage = Stage.INTRO; announcement = intro.getString("ArrivalTitle");
                        titleRemaining = intro.getFloat("MissionTitleSeconds");
                    }
                    revision++;
                }
            }
            if (!gameplayInput()) return;
        }
        boolean missionLeadFinished = stage == Stage.INTRO && stageClock >= mission.get("Intro").getFloat("MissionEntryLeadSeconds");
        if (missionLeadFinished) {
            stage = Stage.RUNNING; stageClock = 0; entryLeadRemaining = 0; revision++;
        }
        if (stage == Stage.MISSION_TRAVEL) {
            cinematicProgress = MathUtils.clamp((stageClock - .3f) / (travelSeconds - .3f), 0, 1);
            if (stageClock >= 1.95f && announcement.isEmpty()) {
                announcement = catalog.missions[save.mission].get("Intro").getString("ArrivalTitle"); revision++;
            }
            if (stageClock >= travelSeconds) {
                advanceMission(); entryLeadRemaining = 0; sound(37);
                announcement = mission.get("Intro").getString("ArrivalTitle");
                titleRemaining = mission.get("Intro").getFloat("MissionTitleSeconds") - (travelSeconds - 1.95f);
            }
        }
        save.missionSeconds += dt;
        boolean scheduling = stage == Stage.RUNNING && entryLeadRemaining <= 0 && !missionLeadFinished;
        if (entryLeadRemaining > 0) entryLeadRemaining -= dt;
        if (scheduling) encounterClock += dt;
        boolean restrictedEdge = (playableX + playableWidth < .999f && boundaryPressureX > .1f)
                || (playableY + playableHeight < .999f && boundaryPressureY > .1f);
        boundaryPressedSeconds = held && restrictedEdge && viewElapsed >= viewDuration ? boundaryPressedSeconds + dt : 0;
        boolean entering = player.respawnRemaining > 0;
        boolean safe = entering && chooseRespawnTarget();
        if (entering && !safe && player.respawnRemaining <= dt) player.respawnRemaining = dt * 2;
        boolean wasOverheated = player.overheated;
        player.tick(dt, held && gameplayInput(), thermal());
        if (!wasOverheated && player.overheated) {
            listener.sound(7);
        } else if (wasOverheated && !player.overheated) {
            listener.sound(8);
        }
        if (player.respawnRemaining > 0) {
            chooseRespawnTarget();
            float t = MathUtils.clamp(1 - player.respawnRemaining / .55f, 0, 1);
            t = t * t * (3 - 2 * t);
            playerX = direction() == 0 ? respawnTargetX : MathUtils.lerp(-.12f * width, respawnTargetX, t);
            playerY = direction() == 0 ? MathUtils.lerp(-.12f * height, respawnTargetY, t) : respawnTargetY;
        } else if (entering) { playerX = respawnTargetX; playerY = respawnTargetY; }
        if (save.lives == 0 && player.deathRemaining <= 0) {
            stage = Stage.GAME_OVER; stageClock = 0; held = false;
            announcement = "KẾT THÚC LƯỢT"; sound(39); revision++; return;
        }
        boolean timelineEnded = (encounter.getInt("Completion") == 1 || encounter.getInt("Completion") == 2)
                && encounterClock >= encounter.getFloat("Duration");
        if (scheduling && !timelineEnded) {
            updateOptionalEvents();
            activateEntryBeats();
            for (Spawn pending : schedule) if (!pending.spawned && pending.at <= encounterClock) {
                pending.spawned = true;
                spawn(pending);
            }
        }
        if (gameplayInput()) firePlayer(dt);
        updateEnemies(dt);
        updateShots(dt);
        updatePickups(dt);
        updateMissile(dt);
        if (save.lives == 0) return;
        if (stage == Stage.RUNNING && scheduling && isEncounterFinished()) {
            if (encounter.getInt("Completion") == 3) {
                closeProducers(); clearHostileShots();
                stage = Stage.BOSS_POST_DEATH; stageClock = 0; revision++;
            } else if (enemies.size > 0) {
                stage = Stage.DRAIN; stageClock = 0;
                for (Enemy enemy : enemies) if (!enemy.spec.asteroid) retreat(enemy);
                revision++;
            } else beginProjectileDrain(dt);
        } else if (stage == Stage.DRAIN) {
            if (enemies.size == 0) beginProjectileDrain(dt);
            else if (stageClock >= 8) for (Enemy enemy : enemies) retreat(enemy);
        } else if (stage == Stage.PROJECTILE_DRAIN) {
            cleanupOverlap += dt;
            if (visibleProjectileTime() <= 0 || stageClock >= drainDuration) completeEncounter();
        } else if (stage == Stage.BOSS_POST_DEATH) {
            if (corpses.size == 0) completeEncounter();
        } else if (stage == Stage.REWARD_GRACE) {
            if (pickups.size == 0 || stageClock >= encounter.getFloat("RewardGraceSeconds")) advanceEncounter();
        } else if (stage == Stage.CLEAR) {
            float pause = encounter.getFloat("ClearPauseSeconds");
            if (pause <= 0) pause = encounter.getFloat("HandoffDelay");
            if (pause <= 0) pause = 1;
            if (stageClock >= Math.max(0, pause - cleanupOverlap)) {
                stage = Stage.HANDOFF; stageClock = cinematicProgress = 0; sound(40); revision++;
            }
        } else if (stage == Stage.HANDOFF) {
            float duration = encounter.getFloat("HandoffTravelSeconds");
            if (duration <= 0) duration = 1.5f;
            cinematicProgress = Math.min(1, stageClock / duration);
            if (stageClock >= duration) advanceEncounter();
        }
    }

    private void closeProducers() {
        for (Enemy enemy : enemies) {
            if (enemy.directed != null) enemy.directed.cancel();
            if (enemy.boss != null) enemy.boss.cancel();
            for (EscortController escort : enemy.escorts) escort.cancel();
        }
        enemies.clear();
    }

    private float visibleProjectileTime() {
        float maximum = 0;
        for (Shot shot : shots) if (shot.hostile && shot.x >= -.03f * width && shot.x <= 1.03f * width
                && shot.y >= -.03f * height && shot.y <= 1.03f * height && (shot.vx != 0 || shot.vy != 0)) {
            float x = Math.abs(shot.vx) < .000001f ? Float.POSITIVE_INFINITY : ((shot.vx > 0 ? 1.03f : -.03f) * width - shot.x) / shot.vx;
            float y = Math.abs(shot.vy) < .000001f ? Float.POSITIVE_INFINITY : ((shot.vy > 0 ? 1.03f : -.03f) * height - shot.y) / shot.vy;
            maximum = Math.max(maximum, Math.max(0, Math.min(x, y)));
        }
        return maximum;
    }

    private void beginProjectileDrain(float dt) {
        closeProducers(); cleanupOverlap = 0;
        drainDuration = visibleProjectileTime() + dt * 2;
        if (drainDuration <= dt * 2) completeEncounter();
        else { stage = Stage.PROJECTILE_DRAIN; stageClock = 0; revision++; }
    }

    private void completeEncounter() {
        closeProducers(); clearHostileShots();
        if (hasKilledCandidate) while (rewards.resolveFallback())
            drop(rewards.selectMajor(catalog.resolve(encounter.get("GuaranteeTable")), false, null), lastKillX, lastKillY, true);
        stageClock = 0;
        if (save.encounter + 1 < mission.get("Encounters").size) {
            stage = Stage.CLEAR; announcement = "";
        } else {
            stage = Stage.REWARD_GRACE;
            for (Pickup pickup : pickups) pickup.remaining = pickup.graceRemaining = encounter.getFloat("RewardGraceSeconds");
            announcement = encounter.getInt("Completion") == 3 && pickups.size > 0 ? "CHIẾN LỢI PHẨM" : "";
        }
        revision++;
    }

    private void updateOptionalEvents() {
        JsonValue events = encounter.get("OptionalEvents");
        for (int i = 0; i < events.size; i++) {
            JsonValue event = events.get(i);
            if (optionalFired[i] || encounterClock < event.getFloat("TimeOffset")) continue;
            if (event.getBoolean("UsePressureWindow") && enemies.size > event.getInt("MaximumActiveEnemies") && encounterClock < event.getFloat("FallbackTime")) continue;
            int condition = event.getInt("Condition");
            if ((condition == 1 && save.lives <= 0) || (condition == 2 && enemies.size > 0)) continue;
            optionalFired[i] = true;
            int first = schedule.size;
            addGroup(event.get("Group"), encounterClock);
            for (int n = first; n < schedule.size; n++) {
                Spawn added = schedule.get(n);
                added.optional = true;
                added.at = encounterClock + added.group.getFloat("StartTime") + added.index * added.group.getFloat("Stagger");
            }
            schedule.sort((a, b) -> Float.compare(a.at, b.at));
        }
    }

    private void activateEntryBeats() {
        for (int beat = 1; beat < beatStarts.length; beat++) {
            if (beatStarts[beat] >= 0 || beatStarts[beat - 1] < 0) continue;
            boolean exhausted = true;
            for (Spawn pending : schedule) if (!pending.optional && pending.group.getInt("EntryBeat", 0) == beat - 1 && !pending.spawned) exhausted = false;
            if (!exhausted) continue;
            JsonValue rule = encounter.get("EntryBeats").get(beat - 1);
            if (rule.getBoolean("WaitForOptionalResolution")) {
                boolean pending = false;
                for (boolean fired : optionalFired) if (!fired) pending = true;
                for (Enemy enemy : enemies) if (enemy.optional) pending = true;
                if (pending) { optionalResolvedAt = -1; continue; }
                if (optionalResolvedAt < 0) optionalResolvedAt = encounterClock;
            }
            float gap = encounterClock - (rule.getBoolean("WaitForOptionalResolution") ? optionalResolvedAt : beatStarts[beat - 1]);
            int live = 0;
            for (Enemy enemy : enemies) if (!enemy.optional && enemy.group.getInt("EntryBeat", 0) == beat - 1) live++;
            if (gap < rule.getFloat("MinimumGap") || (live > rule.getInt("RemainingThreshold") && gap < rule.getFloat("FallbackDelay"))) continue;
            beatStarts[beat] = encounterClock;
            for (Spawn pending : schedule) if (!pending.optional && pending.group.getInt("EntryBeat", 0) == beat)
                pending.at = encounterClock + pending.group.getFloat("StartTime") + pending.index * pending.group.getFloat("Stagger");
            schedule.sort((left, right) -> Float.compare(left.at, right.at));
        }
    }

    private Enemy spawn(Spawn spawn) {
        JsonValue group = spawn.group;
        JsonValue prefabRef = group.get("EnemyPrefab");
        if (!prefabRef.has("guid")) prefabRef = encounter.get("ScoutPrefab");
        Enemy e = new Enemy();
        e.spec = spec(catalog.referencedRecord(prefabRef));
        e.group = group;
        e.spawnedAt = encounterClock;
        e.definition = catalog.resolve(group.get("EnemyDefinition"));
        if (e.definition == null) e.definition = e.spec.definition;
        e.sequence = catalog.resolve(group.get("Sequence"));
        e.elite = catalog.resolve(group.get("Elite"));
        e.rewards = catalog.resolve(group.get("RewardOverride"));
        if (e.rewards == null) e.rewards = e.spec.pickupTable;
        e.optional = spawn.optional;
        e.required = group.getBoolean("Required") && !spawn.optional;
        e.grantsRewards = group.getBoolean("GrantsRewards");
        e.scale = group.getFloat("SpawnScale") * (e.elite == null ? 1 : e.elite.getFloat("Scale"));
        e.hp = group.getInt("HpOverride");
        if (e.hp == 0) e.hp = e.definition.getInt("maxHp");
        if (e.spec.bodyDamages && e.spec.missileTier != 2 && !e.spec.asteroid)
            e.hp = (float)Math.ceil(e.hp * mission.getFloat("EnemyHealthMultiplier") * mission.getFloat("RegionEnemyHealthMultiplier"));
        e.maxHp = e.hp = (float)Math.ceil(e.hp * (e.elite == null ? 1 : e.elite.getFloat("HpMultiplier")));
        FlightMath.slot(group, catalog.resolve(group.get("Plan")), spawn.index, scratch);
        e.offsetX = scratch.x + group.get("Anchor").getFloat("x");
        e.offsetY = scratch.y + group.get("Anchor").getFloat("y");
        e.cooldown = e.spec.fire == null ? 0 : e.spec.fire.getFloat("initialDelay");
        flight(e);
        JsonValue directed = catalog.rootComponent(e.spec.record, "DirectedAttack");
        if (directed != null) e.directed = new DirectedSkill(this, e, catalog.resolve(directed.get("definition")));
        for (JsonValue component : catalog.components(e.spec.record, "EscortSpawner")) e.escorts.add(new EscortController(this, e, component));
        if (catalog.rootComponent(e.spec.record, "BossPhaseRunner") != null) e.boss = new BossCombat(this, e);
        enemies.add(e);
        if (e.spec.missileTier == 2) listener.sound(17);
        return e;
    }

    private void flight(Enemy e) {
        if (e.retreating) {
            float t = MathUtils.clamp(e.retreatClock / 1.1f, 0, 1);
            e.x = MathUtils.lerp(e.retreatX, direction() == 1 ? width * 1.3f : e.retreatX, t);
            e.y = MathUtils.lerp(e.retreatY, direction() == 0 ? height * 1.3f : e.retreatY, t);
            e.canShoot = e.holding = false;
            e.expired = t >= 1;
            return;
        }
        if (e.sequence != null) {
            JsonValue phases = e.sequence.get("Phases");
            float time = e.age;
            int index = 0;
            while (index < phases.size - 1 && time >= phases.get(index).getFloat("Duration"))
                time -= phases.get(index++).getFloat("Duration");
            JsonValue phase = phases.get(index);
            float duration = phase.getFloat("Duration"), period = phase.getFloat("MotionPeriod", 0);
            if (period <= 0) period = duration;
            float progress = phase.getBoolean("RepeatMotion", false) ? ((time + (phase.getFloat("MotionPeriod", 0) > 0 ? e.spawnedAt : 0)) / period) % 1 : time / duration;
            JsonValue from = phase.get("From"), to = phase.get("To");
            FlightMath.evaluate(phase, progress, from.getFloat("x"), from.getFloat("y"), to.getFloat("x"), to.getFloat("y"), scratch);
            float t = MathUtils.clamp(progress, 0, 1);
            if (phase.getBoolean("Smooth")) t = t * t * (3 - 2 * t);
            float linearX = MathUtils.lerp(from.getFloat("x"), to.getFloat("x"), t);
            float linearY = MathUtils.lerp(from.getFloat("y"), to.getFloat("y"), t);
            boolean fromClamp = phase.getBoolean("ClampToView") || (index > 0 && phases.get(index - 1).getBoolean("ClampToView"));
            boolean toClamp = phase.getBoolean("ClampToView") || (index + 1 < phases.size && phases.get(index + 1).getBoolean("ClampToView"));
            float curveX = scratch.x - linearX, curveY = scratch.y - linearY;
            float blend = phase.getFloat("MotionPeriod", 0) > 0 ? Math.min(1, time / .6f) : 1;
            blend = blend * blend * (3 - 2 * blend);
            projectFlight(e, from.getFloat("x"), from.getFloat("y"), fromClamp, scratch);
            float fromX = scratch.x, fromY = scratch.y;
            if (index == 0 && !Float.isNaN(e.entryOriginX)) { fromX = e.entryOriginX; fromY = e.entryOriginY; }
            projectFlight(e, to.getFloat("x"), to.getFloat("y"), toClamp, scratch);
            float curveScreenX = direction() == 0 ? curveX : curveY;
            float curveScreenY = direction() == 0 ? curveY : -curveX;
            e.x = (MathUtils.lerp(fromX, scratch.x, t) + curveScreenX * e.motionMultiplier * blend) * width;
            e.y = (MathUtils.lerp(fromY, scratch.y, t) + curveScreenY * e.motionMultiplier * blend) * height;
            e.canShoot = phase.getBoolean("CanShoot");
            e.holding = phase.getInt("Kind") == 2 || phase.getInt("Kind") == 3;
            e.expired = index == phases.size - 1 && !phase.getBoolean("HoldUntilResolved") && time >= duration;
        } else {
            float entry = e.group.getFloat("EntryDuration"), hold = e.group.getFloat("HoldDuration"), exit = e.group.getFloat("ExitDuration");
            JsonValue from = e.group.get("EntryAnchor"), to = e.group.get("HoldAnchor");
            float p = MathUtils.clamp(e.age / entry, 0, 1);
            if (e.age > entry + hold) { from = to; to = e.group.get("ExitAnchor"); p = MathUtils.clamp((e.age - entry - hold) / exit, 0, 1); }
            if (e.group.getBoolean("ContinuousTransit")) { from = e.group.get("EntryAnchor"); to = e.group.get("ExitAnchor"); p = MathUtils.clamp(e.age / (entry + hold + exit), 0, 1); }
            else p = p * p * (3 - 2 * p);
            float x = MathUtils.lerp(from.getFloat("x"), to.getFloat("x"), p) + e.offsetX;
            float y = MathUtils.lerp(from.getFloat("y"), to.getFloat("y"), p) + e.offsetY;
            FlightMath.orient(x, y, direction(), scratch);
            e.x = scratch.x * width; e.y = scratch.y * height;
            e.canShoot = e.holding = e.age >= entry && e.age < entry + hold;
            e.expired = e.age >= entry + hold + exit;
        }
    }

    private void projectFlight(Enemy e, float x, float y, boolean clamp, Vector2 out) {
        FlightMath.orient(x + e.offsetX + e.combatX, y + e.offsetY + e.combatY, direction(), out);
        if (!clamp) return;
        float hw = halfWidth(e) / width, hh = halfHeight(e) / height;
        float ox = (direction() == 0 ? e.spec.colliderX : e.spec.colliderY) * e.scale * e.spec.rootScale / width;
        float oy = (direction() == 0 ? e.spec.colliderY : -e.spec.colliderX) * e.scale * e.spec.rootScale / height;
        out.x = MathUtils.clamp(out.x, hw - ox, 1 - hw - ox);
        out.y = MathUtils.clamp(out.y, hh - oy, 1 - hh - oy);
    }

    private float halfWidth(Enemy e) { return (direction() == 0 ? e.spec.halfWidth : e.spec.halfHeight) * e.scale * e.spec.rootScale; }
    private float halfHeight(Enemy e) { return (direction() == 0 ? e.spec.halfHeight : e.spec.halfWidth) * e.scale * e.spec.rootScale; }

    private void firePlayer(float dt) {
        shotCooldown = Math.max(0, shotCooldown - dt);
        droneCooldown = Math.max(0, droneCooldown - dt);
        if (held && player.canFire() && shotCooldown <= 0) {
            JsonValue weapon = weapon(), pattern = weapon.get("levels").get(save.weaponLevels[save.activeWeapon] - 1);
            float muzzleX = playerX + (direction() == 0 ? playerMuzzleX : playerMuzzleY);
            float muzzleY = playerY + (direction() == 0 ? playerMuzzleY : -playerMuzzleX);
            launchPattern(weapon, pattern, muzzleX, muzzleY, false, 90 + rotation());
            muzzleRemaining = .1f;
            shotCooldown = pattern.getFloat("FireInterval") / player.fireRateMultiplier;
            listener.sound(save.activeWeapon);
        }
        if (player.droneRemaining > 0 && player.canInput() && droneCooldown <= 0) {
            launchPattern(weapons[0], weapons[0].get("levels").get(0), playerX - .7f, playerY - .3f, false, 90 + rotation());
            droneCooldown = .4f;
        }
    }

    void launchPattern(JsonValue definition, JsonValue pattern, float x, float y, boolean hostile, float heading) {
        ProjectileShape shape = projectileShapes.get(definition);
        if (shape == null) {
            shape = new ProjectileShape();
            JsonValue prefab = catalog.referencedRecord(definition.get("projectilePrefab"));
            for (JsonValue doc : prefab.get("documents")) if (doc.getString("type").equals("BoxCollider2D")) {
                JsonValue collider = doc.get("data");
                CampaignCatalog.Pose pose = CampaignCatalog.pose(prefab, collider.get("m_GameObject").getString("fileID"));
                shape.hw = collider.get("m_Size").getFloat("x") * Math.abs(pose.sx) * .5f;
                shape.hh = collider.get("m_Size").getFloat("y") * Math.abs(pose.sy) * .5f;
            }
            JsonValue loop = catalog.component(prefab, "SpriteFrameLoop");
            if (loop != null) { shape.frames = frames(loop.get("frames")); shape.frameSeconds = loop.getFloat("frameSeconds"); }
            if (shape.hw <= 0 || shape.hh <= 0) throw new IllegalArgumentException("Missing projectile box collider");
            projectileShapes.put(definition, shape);
        }
        int count = pattern == null ? definition.getInt("projectileCount") : pattern.getInt("ProjectileCount");
        float spread = pattern == null ? definition.getFloat("spreadDegrees") : pattern.getFloat("SpreadDegrees");
        float spacing = pattern == null ? 0 : pattern.getFloat("LateralSpacing");
        for (int i = 0; i < count; i++) {
            float offset = i - (count - 1) * .5f;
            float angle = heading + (count == 1 ? 0 : offset * spread / (count - 1));
            Shot shot = shotPool.obtain();
            shot.x = shot.previousX = x + MathUtils.cosDeg(heading - 90) * offset * spacing;
            shot.y = shot.previousY = y + MathUtils.sinDeg(heading - 90) * offset * spacing;
            float speed = definition.getFloat("projectileSpeed");
            shot.vx = MathUtils.cosDeg(angle) * speed; shot.vy = MathUtils.sinDeg(angle) * speed;
            shot.life = definition.getFloat("projectileLifetime");
            shot.damage = pattern == null ? 1 : pattern.getInt("Damage") + (Math.abs(offset) < .01f ? pattern.getInt("CenterBonus") : 0);
            shot.splash = pattern == null ? 0 : pattern.getFloat("SplashRadius");
            shot.hostile = hostile;
            shot.branch = hostile ? -1 : definition == weapons[1] ? 1 : definition == weapons[2] ? 2 : 0;
            shot.sprite = CampaignCatalog.spriteKey(definition.get("projectileSprite"));
            shot.homing = hostile ? 0 : definition.getFloat("trackingDegreesPerSecond");
            shot.trackingDelay = hostile ? 0 : definition.getFloat("trackingLaunchSeconds");
            shot.trackingRange = hostile ? 0 : definition.getFloat("trackingRange");
            shot.launchTurnRate = shot.homing > 0 && shot.trackingDelay > 0 && count > 1
                    ? definition.getFloat("trackingLaunchArcDegrees") * (i * 2f / (count - 1) - 1) / shot.trackingDelay : 0;
            JsonValue scale = definition.get("projectileScale");
            shot.scaleX = scale.getFloat("x"); shot.scaleY = scale.getFloat("y");
            JsonValue color = definition.get("projectileColor");
            shot.r = color.getFloat("r"); shot.g = color.getFloat("g"); shot.b = color.getFloat("b"); shot.a = color.getFloat("a");
            shot.radius = 0; shot.halfWidth = shape.hw; shot.halfHeight = shape.hh;
            shot.visualFrames = shape.frames; shot.frameSeconds = shape.frameSeconds;
            shots.add(shot);
        }
    }

    private void updateEnemies(float dt) {
        int busy = 0;
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            if (!e.motionPaused) e.age += dt;
            if (e.retreating) e.retreatClock += dt;
            if (e.repositionClock < e.repositionDuration) {
                e.repositionClock = Math.min(e.repositionDuration, e.repositionClock + dt);
                float t = e.repositionClock / e.repositionDuration;
                t = t * t * (3 - 2 * t);
                e.combatX = MathUtils.lerp(e.combatFromX, e.combatTargetX, t);
                e.combatY = MathUtils.lerp(e.combatFromY, e.combatTargetY, t);
            }
            e.flash = Math.max(0, e.flash - dt);
            if (!e.motionPaused) flight(e);
            if (e.x + halfWidth(e) >= 0 && e.x - halfWidth(e) <= width && e.y + halfHeight(e) >= 0 && e.y - halfHeight(e) <= height) e.visibleSeconds += dt;
            e.cooldown -= dt;
            if (e.directed != null) { e.directed.tick(dt); if (e.directed.busy()) busy++; }
            for (EscortController controller : e.escorts) controller.tick(dt);
            if (e.boss != null) { e.boss.tick(dt); if (e.boss.busy()) busy++; }
            if (e.expired && (stage == Stage.DRAIN || !e.required || (encounter.getInt("Completion") != 0 && encounter.getInt("Completion") != 3))) {
                if (eligible(e) && e.grantsRewards) rewards.resolveEscape();
                if (e.directed != null) e.directed.cancel();
                if (e.boss != null) e.boss.cancel();
                for (EscortController controller : e.escorts) controller.cancel();
                enemies.removeIndex(i); continue;
            }
            if (e.spec.bodyDamages && Math.abs(e.x - playerX) < halfWidth(e) + playerSpec.halfWidth && Math.abs(e.y - playerY) < halfHeight(e) + playerSpec.halfHeight) hitPlayer();
            if (e.charge > 0) {
                busy++;
                if (stage != Stage.RUNNING || e.retreating) { e.charge = 0; e.burstRemaining = 0; continue; }
                e.charge -= dt;
                if (e.charge <= 0 && e.canShoot) {
                    float angle = MathUtils.atan2(playerY - e.y, playerX - e.x) * MathUtils.radiansToDegrees;
                    float muzzleX = e.x + (direction() == 0 ? e.spec.muzzleX : e.spec.muzzleY) * e.scale;
                    float muzzleY = e.y + (direction() == 0 ? e.spec.muzzleY : -e.spec.muzzleX) * e.scale;
                    launchPattern(e.spec.fire, null, muzzleX, muzzleY, true, angle);
                    listener.sound(e.spec.missileTier == 1 ? 16 : 10);
                    if (--e.burstRemaining > 0) e.charge = e.spec.fire.getFloat("burstInterval");
                    else e.cooldown = e.spec.fire.getFloat("cooldown") * (e.elite == null ? 1 : e.elite.getFloat("FireIntervalMultiplier"));
                }
            }
        }
        if (stage != Stage.RUNNING || !player.canInput() || player.enemyFireGrace > 0) return;
        if (volleyShots >= encounter.getInt("ShotsPerVolley") && busy > 0) return;
        fireSpacing -= dt;
        if (fireSpacing > 0) return;
        if (volleyShots >= encounter.getInt("ShotsPerVolley")) volleyShots = 0;
        if (busy >= encounter.getInt("ActiveShooterLimit")) return;
        for (int priority = 0; priority < 2; priority++) for (int i = 0; i < enemies.size; i++) {
            int index = (shooterCursor + i) % enemies.size;
            Enemy e = enemies.get(index);
            if (priority == 0 && (e.directed == null || e.directed.attackCount > 0)) continue;
            if ((!e.spec.fires && e.directed == null) || !e.canShoot || e.cooldown > 0 || e.charge > 0 || e.x < 0 || e.x > width || e.y < 0 || e.y > height) continue;
            if (e.directed != null) {
                if (!e.directed.request()) continue;
                fireSpacing = ++volleyShots >= encounter.getInt("ShotsPerVolley") ? encounter.getFloat("VolleyPause") : encounter.getFloat("ShotSpacing");
                shooterCursor = (index + 1) % enemies.size;
                return;
            }
            e.charge = Math.max(.001f, e.spec.fire.getFloat("telegraphDuration"));
            if (e.spec.missileTier == 1) sound(15);
            e.burstRemaining = e.spec.fire.getInt("burstCount");
            fireSpacing = ++volleyShots >= encounter.getInt("ShotsPerVolley") ? encounter.getFloat("VolleyPause") : encounter.getFloat("ShotSpacing");
            shooterCursor = (index + 1) % enemies.size;
            return;
        }
    }

    private void updateShots(float dt) {
        for (int i = shots.size - 1; i >= 0; i--) {
            Shot s = shots.get(i);
            s.previousX = s.x; s.previousY = s.y;
            s.life -= dt; s.age += dt;
            if (s.homing > 0) {
                if (s.target == null || s.target.hp <= 0 || (s.targetNode != null && s.targetNode.hp <= 0)
                        || (s.targetNode == null && !canDamage(s.target))) {
                    s.target = null; s.targetNode = null;
                    float best = s.trackingRange * s.trackingRange;
                    for (Enemy e : enemies) {
                        if (e.boss != null && e.holding) for (BossCombat.Node node : e.boss.nodes) {
                            float dx = node.x - s.x, dy = node.y - s.y, distance = dx * dx + dy * dy;
                            if (node.hp > 0 && distance < best && s.vx * dx + s.vy * dy > 0) {
                                best = distance; s.target = e; s.targetNode = node;
                            }
                        }
                        if (!canDamage(e)) continue;
                        float dx = e.x - s.x, dy = e.y - s.y, distance = dx * dx + dy * dy;
                        if (distance < best && s.vx * dx + s.vy * dy > 0) { best = distance; s.target = e; s.targetNode = null; }
                    }
                }
                float speed = (float)Math.sqrt(s.vx * s.vx + s.vy * s.vy);
                float angle = MathUtils.atan2(s.vy, s.vx) * MathUtils.radiansToDegrees;
                if (s.age <= s.trackingDelay) angle += s.launchTurnRate * dt;
                else if (s.target != null) {
                    float x = s.targetNode == null ? s.target.x : s.targetNode.x, y = s.targetNode == null ? s.target.y : s.targetNode.y;
                    float desired = MathUtils.atan2(y - s.y, x - s.x) * MathUtils.radiansToDegrees;
                    float turn = ((desired - angle + 540) % 360) - 180;
                    angle += MathUtils.clamp(turn, -s.homing * dt, s.homing * dt);
                }
                s.vx = MathUtils.cosDeg(angle) * speed; s.vy = MathUtils.sinDeg(angle) * speed;
            }
            s.x += s.vx * dt; s.y += s.vy * dt;
            if (s.hostile) {
                float px = playerX + (direction() == 0 ? playerSpec.colliderX : playerSpec.colliderY);
                float py = playerY + (direction() == 0 ? playerSpec.colliderY : -playerSpec.colliderX);
                if (shotHitsPlayer(s, px, py, playerSpec.halfWidth) && player.canInput()) {
                    hitPlayer(); s.life = 0;
                }
            } else {
                float angle = MathUtils.atan2(s.vy, s.vx) - MathUtils.PI * .5f;
                float shotHW = Math.abs(MathUtils.cos(angle)) * s.halfWidth + Math.abs(MathUtils.sin(angle)) * s.halfHeight + s.radius;
                float shotHH = Math.abs(MathUtils.sin(angle)) * s.halfWidth + Math.abs(MathUtils.cos(angle)) * s.halfHeight + s.radius;
                Enemy nearest = null;
                BossCombat.Node nearestNode = null;
                float fraction = Float.POSITIVE_INFINITY;
                for (int j = enemies.size - 1; j >= 0; j--) {
                    Enemy e = enemies.get(j);
                    if (!canDamage(e) && !(e.boss != null && e.holding && e.boss.blocksCoreDamage())) continue;
                    if (e.boss != null && e.holding) for (BossCombat.Node node : e.boss.nodes) {
                        if (node.hp <= 0) continue;
                        float entry = segmentFraction(s, node.x, node.y, node.halfWidth + shotHW, node.halfHeight + shotHH);
                        if (entry < fraction) { fraction = entry; nearest = e; nearestNode = node; }
                    }
                    float ox = (direction() == 0 ? e.spec.colliderX : e.spec.colliderY) * e.scale * e.spec.rootScale;
                    float oy = (direction() == 0 ? e.spec.colliderY : -e.spec.colliderX) * e.scale * e.spec.rootScale;
                    float entry = e.boss == null ? segmentFraction(s, e.x + ox, e.y + oy, halfWidth(e) + shotHW, halfHeight(e) + shotHH)
                            : e.boss.coreHitFraction(s, shotHW, shotHH);
                    if (entry < fraction) { fraction = entry; nearest = e; nearestNode = null; }
                }
                if (nearest != null) {
                    float hitX = MathUtils.lerp(s.previousX, s.x, fraction);
                    float hitY = MathUtils.lerp(s.previousY, s.y, fraction);
                    if (nearestNode != null || canDamage(nearest)) {
                        impact(nearest, s.branch, hitX, hitY);
                        if (s.branch == 2) sound(3);
                    } else if (nearest.boss != null && nearest.boss.blocksCoreDamage()) {
                        burst(hitShieldFrames, hitX, hitY, .5f, .22f);
                    }
                    if (!replay || !devFakeShots) {
                        if (nearestNode != null) nearest.boss.damageNode(nearestNode, s.damage);
                        else damage(nearest, s.damage);
                    }
                    if (s.splash > 0 && (!replay || !devFakeShots)) for (int k = enemies.size - 1; k >= 0; k--) {
                        Enemy other = enemies.get(k);
                        if (other != nearest && Vector2.dst2(nearest.x, nearest.y, other.x, other.y) <= s.splash * s.splash) damage(other, s.damage);
                    }
                    s.life = 0;
                }
            }
            if (s.life <= 0 || s.x < -3 || s.x > width + 3 || s.y < -3 || s.y > height + 3) shotPool.free(shots.removeIndex(i));
        }
    }

    public static boolean segmentHit(Shot s, float x, float y, float hw, float hh) {
        return segmentFraction(s, x, y, hw, hh) != Float.POSITIVE_INFINITY;
    }

    public static boolean shotHitsPlayer(Shot s, float x, float y, float radius) {
        if (s.halfWidth <= 0) return segmentCircleHit(s, x, y, radius + s.radius);
        float angle = MathUtils.atan2(s.vy, s.vx) - MathUtils.PI * .5f;
        float cos = MathUtils.cos(angle), sin = MathUtils.sin(angle);
        float ax = s.previousX - x, ay = s.previousY - y, bx = s.x - x, by = s.y - y;
        return segmentFraction(ax * cos + ay * sin, -ax * sin + ay * cos, bx * cos + by * sin, -bx * sin + by * cos,
                0, 0, s.halfWidth + radius, s.halfHeight + radius) != Float.POSITIVE_INFINITY;
    }

    public static float segmentFraction(Shot s, float x, float y, float hw, float hh) {
        return segmentFraction(s.previousX, s.previousY, s.x, s.y, x, y, hw, hh);
    }

    private static float segmentFraction(float ax, float ay, float bx, float by, float x, float y, float hw, float hh) {
        float dx = bx - ax, dy = by - ay;
        float enter = 0, exit = 1;
        if (Math.abs(dx) < .000001f) { if (Math.abs(bx - x) > hw) return Float.POSITIVE_INFINITY; }
        else {
            float a = (x - hw - ax) / dx, b = (x + hw - ax) / dx;
            enter = Math.max(enter, Math.min(a, b)); exit = Math.min(exit, Math.max(a, b));
        }
        if (Math.abs(dy) < .000001f) { if (Math.abs(by - y) > hh) return Float.POSITIVE_INFINITY; }
        else {
            float a = (y - hh - ay) / dy, b = (y + hh - ay) / dy;
            enter = Math.max(enter, Math.min(a, b)); exit = Math.min(exit, Math.max(a, b));
        }
        return enter <= exit ? enter : Float.POSITIVE_INFINITY;
    }

    public static boolean segmentCircleHit(Shot s, float x, float y, float radius) {
        float dx = s.x - s.previousX, dy = s.y - s.previousY;
        float length2 = dx * dx + dy * dy;
        float t = length2 == 0 ? 0 : MathUtils.clamp(((x - s.previousX) * dx + (y - s.previousY) * dy) / length2, 0, 1);
        return Vector2.dst2(x, y, s.previousX + t * dx, s.previousY + t * dy) <= radius * radius;
    }

    public void hitPlayer() {
        if (devGodMode) return;
        int result = player.hit();
        if (result == 0) return;
        revision++;
        shake = .5f;
        if (result == 1) {
            listener.sound(5);
            listener.sound(6);
            if (hitShieldFrames != null && hitShieldFrames.length > 0) {
                burst(hitShieldFrames, playerX, playerY, .5f, .22f);
            }
        } else if (result >= 2) {
            listener.sound(4);
            listener.sound(9);
            save.livesLost++;
            burst(playerSpec.deathFrames, playerX, playerY, 1, playerSpec.deathSeconds);
            for (int i = 0; i < playerSpec.fragments.size; i++) {
                Fragment fragment = playerSpec.fragments.get(i);
                float angle = 45 + i * 360f / playerSpec.fragments.size + rotation();
                Burst piece = burst(fragment.frames, playerX + MathUtils.cosDeg(angle) * .12f, playerY + MathUtils.sinDeg(angle) * .12f, fragment.scale, playerSpec.deathSeconds);
                if (piece != null) {
                    piece.vx = MathUtils.cosDeg(angle) * .8f / playerSpec.deathSeconds; piece.vy = MathUtils.sinDeg(angle) * .8f / playerSpec.deathSeconds;
                    piece.rotation += i * 90; piece.spin = (i % 2 == 0 ? 160 : -160) / playerSpec.deathSeconds; piece.fade = true;
                }
            }
            checkpointPosition();
        }
    }

    private boolean eligible(Enemy e) {
        return e.spec.bodyDamages && e.spec.missileTier != 2 && !e.spec.asteroid;
    }

    private boolean canDamage(Enemy e) {
        if (e.hp <= 0 || (e.boss != null && e.boss.blocksCoreDamage())) return false;
        if (e.x + halfWidth(e) < 0 || e.x - halfWidth(e) > width || e.y + halfHeight(e) < 0 || e.y - halfHeight(e) > height) return false;
        return !e.spec.bodyDamages || e.spec.asteroid || (e.visibleSeconds >= .2f && (e.spec.missileTier != 2 || e.holding));
    }

    private void impact(Enemy enemy, int branch, float x, float y) {
        String[] frames = enemy.spec.asteroid ? hitRockFrames : branch == 1 ? hitPlasmaFrames : branch == 2 ? hitHomingFrames : hitPulseFrames;
        float scale = enemy.spec.asteroid ? .65f : branch == 1 ? .8f : branch == 2 ? 1.05f : .55f;
        burst(frames, x, y, scale, .22f);
    }

    private void damage(Enemy e, float amount) {
        if (!canDamage(e)) return;
        e.hp -= amount;
        emitArmorBreak(e);
        e.flash = .08f;
        if (e.hp > 0) return;
        enemies.removeValue(e, true);
        if (e.required) requiredKilled++;
        if (e.directed != null) e.directed.cancel();
        if (e.boss != null) e.boss.cancel();
        for (EscortController controller : e.escorts) controller.cancel();
        if (e.boss != null) {
            Corpse corpse = new Corpse(); corpse.enemy = e; corpse.duration = e.spec.deathSeconds; corpses.add(corpse);
            JsonValue death = catalog.component(e.spec.record, "BossDeathSequence");
            int index = 0;
            for (JsonValue ref : death.get("explosions")) {
                JsonValue visual = local(e.spec.record, ref);
                CampaignCatalog.Pose pose = CampaignCatalog.pose(e.spec.record, visual.get("m_GameObject").getString("fileID"));
                boolean last = index == death.get("explosions").size - 1;
                float seconds = last ? .5f : .35f;
                Burst explosion = burst(e.spec.deathFrames, e.x + (direction() == 0 ? pose.x : pose.y) * e.scale,
                        e.y + (direction() == 0 ? pose.y : -pose.x) * e.scale, .3f, seconds);
                if (explosion != null) { explosion.soundOnStart = last ? 28 : 27; explosion.age = -(last ? e.spec.deathSeconds - seconds : index * .35f); explosion.growTo = last ? 9.6f : 1.1f; }
                if (index == 0) sound(27);
                index++;
            }
            if (e.spec.deathStages != null) for (JsonValue stage : e.spec.deathStages) {
                JsonValue visual = local(e.spec.record, stage.get("blast"));
                CampaignCatalog.Pose pose = CampaignCatalog.pose(e.spec.record, visual.get("m_GameObject").getString("fileID"));
                Burst blast = burst(frames(stage.get("blastFrames")), e.x + (direction() == 0 ? pose.x : pose.y) * e.scale,
                        e.y + (direction() == 0 ? pose.y : -pose.x) * e.scale, .4f, stage.getFloat("duration"));
                if (blast != null) { blast.age = -stage.getFloat("delay"); blast.growTo = 1.6f; }
            }
        } else burst(e.spec.deathFrames, e.x, e.y,
                e.spec.asteroid ? e.scale : !e.spec.bodyDamages ? .9f : MathUtils.clamp(e.scale * e.spec.rootScale * 1.3f, .9f, 2), e.spec.deathSeconds);
        if (e.spec.missileTier != 2) listener.sound(e.spec.asteroid ? 26 : e.spec.missileTier == 1 ? 25 : 24);
        shake = Math.max(shake, e.spec.missileTier == 2 ? .8f : .12f);
        if (e.boss == null) awardEnemy(e);
        revision++;
    }

    private void awardEnemy(Enemy e) {
        if (e.grantsRewards) {
            int score = Math.multiplyExact(e.definition.getInt("score", 100), e.elite == null ? 1 : 2);
            save.score = Math.addExact(save.score, score);
            save.missionScore = Math.addExact(save.missionScore, score);
            if (e.spec.bodyDamages && !e.spec.asteroid) save.enemiesDestroyed++;
            JsonValue table = e.rewards == null ? defaultDrops : e.rewards;
            if (table.getInt("Source") == 2) {
                JsonValue core = rewards.selectMajor(table, false, true), support = rewards.selectMajor(table, true, false);
                if (core == null) core = rewards.selectMajor(table, false, false);
                float x = direction() == 0 ? MathUtils.clamp(e.x, width * .12f, width * .88f) : e.x;
                float y = direction() == 0 ? e.y : MathUtils.clamp(e.y, height * .12f, height * .88f);
                SupplyCapture capture = new SupplyCapture();
                Pickup first = drop(core, x - (direction() == 0 ? .35f : 0), y + (direction() == 0 ? 0 : .35f), true);
                Pickup second = drop(support, x + (direction() == 0 ? .35f : 0), y - (direction() == 0 ? 0 : .35f), true);
                if (first != null) { eject(first, -1.5f, 1); first.supply = capture; }
                if (second != null) { eject(second, 1.5f, 1); second.supply = capture; }
                if (support != null) rewards.recordCrate(support, save.lives);
                if (e.spec.missileTier == 2) {
                    JsonValue shard = catalog.resolve(table.get("Shard"));
                    for (int i = 0; i < table.getInt("BossShardCount"); i++) {
                        FlightMath.orient(.07f + .86f * ((i * .618034f) % 1), .58f + .27f * ((i * .414214f) % 1), direction(), scratch);
                        Pickup gold = drop(shard, scratch.x * width, scratch.y * height, true);
                        if (gold != null) eject(gold, MathUtils.sin(i * 2.4f) * .8f, .6f + ((i * .37f) % 1));
                    }
                }
            } else {
                JsonValue item = rewards.direct(table);
                boolean floor = false;
                if (eligible(e)) {
                    hasKilledCandidate = true; lastKillX = e.x; lastKillY = e.y;
                    JsonValue guarantee = catalog.resolve(encounter.get("GuaranteeTable"));
                    JsonValue preferred = guarantee == null ? null : catalog.resolve(guarantee.get("GuaranteedPickup"));
                    boolean satisfies = item != null && item.getInt("Kind") != 0 &&
                            (preferred == null || !player.canUse(preferred) || preferred == item);
                    floor = satisfies && rewards.remaining() > 0;
                    if (rewards.resolveKill(satisfies)) { floor = true; item = rewards.selectMajor(guarantee, false, null); }
                }
                drop(item, e.x, e.y, floor);
            }
        }
        revision++;
    }

    private void emitArmorBreak(Enemy enemy) {
        JsonValue armor = enemy.spec.armor;
        if (armor == null) return;
        float seconds = armor.getFloat("seconds");
        float cos = MathUtils.cosDeg(rotation()), sin = MathUtils.sinDeg(rotation());
        for (int i = 0; i < armor.get("healthFractions").size; i++) {
            if ((enemy.armorEmitted & (1 << i)) != 0 || enemy.hp > enemy.maxHp * armor.get("healthFractions").getFloat(i)) continue;
            enemy.armorEmitted |= 1 << i;
            JsonValue anchor = armor.get("anchors").get(i);
            float x = anchor.getFloat("x") * enemy.scale * enemy.spec.rootScale, y = anchor.getFloat("y") * enemy.scale * enemy.spec.rootScale;
            float px = enemy.x + x * cos - y * sin, py = enemy.y + x * sin + y * cos;
            burst(enemy.spec.armorFrames, px, py, 1, seconds * .5f);
            Burst piece = burst(enemy.spec.armorFragments[i], px, py, 1, seconds);
            if (piece != null) {
                float sign = i == 0 ? -1 : 1;
                piece.vx = cos * sign * 2 / seconds; piece.vy = sin * sign * 2 / seconds;
                piece.ax = sin * 2 / (seconds * seconds); piece.ay = -cos * 2 / (seconds * seconds);
                piece.spin = sign * 200 / seconds; piece.fade = true; piece.fadeFrom = .5f;
            }
            sound(23);
        }
    }

    Pickup drop(JsonValue item, float x, float y, boolean guaranteed) {
        if (item == null || (!guaranteed && pickups.size >= activePickupLimit)) return null;
        Pickup pickup = pickupPool.obtain();
        pickup.definition = item;
        pickup.sprite = CampaignCatalog.spriteKey(item.get("Sprite"));
        pickup.direction = direction();
        if (pickup.direction == 0) {
            pickup.x = MathUtils.clamp(x, width * .05f, width * .95f);
            pickup.y = MathUtils.clamp(y, height * .15f, height * .88f);
        } else {
            pickup.x = MathUtils.clamp(x, width * .15f, width * .88f);
            pickup.y = MathUtils.clamp(y, height * .05f, height * .95f);
        }
        pickup.previousX = pickup.x; pickup.previousY = pickup.y;
        pickup.previousPlayerX = playerX; pickup.previousPlayerY = playerY;
        pickup.vy = -item.getFloat("DriftSpeed");
        pickup.remaining = item.getFloat("Lifetime");
        pickups.add(pickup);
        return pickup;
    }

    void eject(Pickup pickup, float side, float forward) {
        forward *= pickup.direction == 1 ? width / height : 1;
        pickup.vx = pickup.direction == 0 ? side : forward;
        pickup.ejectionForward = pickup.direction == 0 ? forward : -side;
        pickup.ejectionRemaining = .45f;
    }

    private void updatePickups(float dt) {
        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup p = pickups.get(i);
            p.age += dt; p.remaining -= dt;
            if (p.graceRemaining >= 0) p.graceRemaining = Math.max(0, p.graceRemaining - dt);
            p.opacity = MathUtils.clamp((p.graceRemaining >= 0 ? Math.min(p.remaining, p.graceRemaining) : p.remaining) / expiryFadeSeconds, 0, 1);
            if (p.remaining <= 0) { pickupPool.free(pickups.removeIndex(i)); continue; }
            if (p.ejectionRemaining > 0) {
                float factor = p.ejectionRemaining / .45f * dt;
                p.x += p.vx * factor; p.y += p.ejectionForward * factor;
                p.ejectionRemaining = Math.max(0, p.ejectionRemaining - dt);
            }
            boolean shard = p.definition.getInt("Kind") == 0;
            float driftScale = p.direction == 1 ? width / height : 1;
            if (shard) p.vy = Math.max(-p.definition.getFloat("MaximumFallSpeed"), p.vy - p.definition.getFloat("FallAcceleration") * dt);
            float depth = (p.direction == 0 ? p.y : p.x) + p.vy * driftScale * dt;
            float floor = .06f * (p.direction == 0 ? height : width);
            if (shard && depth <= floor && p.vy < 0 && p.bounceCount < 2) {
                depth = floor;
                float bounceHeight = p.definition.getFloat("ShardBounceHeight") * height * (p.bounceCount == 0 ? 1 : .35f);
                p.vy = (float)Math.sqrt(2 * p.definition.getFloat("FallAcceleration") * bounceHeight);
                p.bounceCount++;
            }
            if (p.direction == 0) p.y = depth; else p.x = depth;
            if (p.graceRemaining >= 0) {
                if (p.direction == 0) { p.x = MathUtils.clamp(p.x, width * .05f, width * .95f); if (!shard) p.y = Math.max(height * .09f, p.y); }
                else { p.y = MathUtils.clamp(p.y, height * .05f, height * .95f); if (!shard) p.x = Math.max(width * .09f, p.x); }
            }
            float fromX = p.previousPlayerX - p.previousX, fromY = p.previousPlayerY - p.previousY;
            float toX = playerX - p.x, toY = playerY - p.y;
            boolean collected = gameplayInput() && pickupSweepDistanceSquared(fromX, fromY, toX, toY) <= collectionRadius * collectionRadius && player.pickup(p.definition);
            p.previousPlayerX = playerX; p.previousPlayerY = playerY; p.previousX = p.x; p.previousY = p.y;
            if (collected && p.supply != null && !p.supply.collected) { p.supply.collected = true; save.supplyCaptured++; }
            if (collected && p.definition.getInt("Kind") == 1) save.coresCollected++;
            if (collected && p.definition.getInt("Kind") == 0 && !replay) {
                encounterShards = Math.addExact(encounterShards, p.definition.getInt("Amount"));
                save.shard = listener.creditShards(encounterShards, save.shard);
            }
            if (collected) {
                int kind = p.definition.getInt("Kind");
                pickupAnnouncement = kind == 0 ? "+" + p.definition.getInt("Amount") + " SHARD"
                        : kind == 1 ? new String[]{"Pulse", "Plasma", "Homing"}[p.definition.getInt("Branch")] + " · CẤP " + save.weaponLevels[p.definition.getInt("Branch")]
                        : kind == 2 ? "KHIÊN" : kind == 3 ? "CƯỜNG HÓA" : kind == 4 ? "TÀU HỖ TRỢ" : kind == 5 ? "+1 TÊN LỬA" : "+1 MẠNG";
                pickupCueRemaining = 1.2f;
                Burst reward = burst(kind == 0 ? coinFrames : kind == 2 ? shieldFrames : upgradeFrames,
                        playerX, playerY, 1.5f, .36f);
                if (reward != null) reward.followPlayer = true;
            }
            if (collected) { listener.sound(p.definition.getInt("Kind") == 0 ? 29 : p.definition.getInt("Kind") == 6 ? 31 : 30); revision++; }
            if (collected || (p.direction == 0 ? p.y / height : p.x / width) < -.06f) pickupPool.free(pickups.removeIndex(i));
        }
    }

    private static float pickupSweepDistanceSquared(float fromX, float fromY, float toX, float toY) {
        float dx = toX - fromX, dy = toY - fromY;
        float length = dx * dx + dy * dy;
        float t = length > .000001f ? MathUtils.clamp(-(fromX * dx + fromY * dy) / length, 0, 1) : 0;
        float x = fromX + dx * t, y = fromY + dy * t;
        return x * x + y * y;
    }

    public boolean canUseMissile() {
        return (stage == Stage.RUNNING || stage == Stage.DRAIN) && player.canInput() && save.missiles > 0 && missileCooldown <= 0 && missileFlight <= 0;
    }

    public boolean missile() {
        if (!canUseMissile()) return false;
        save.missiles--;
        missileCooldown = .8f;
        missileFlightDuration = .32f;
        missileFlight = missileFlightDuration;
        missileStartX = playerX;
        missileStartY = playerY;
        player.protect(.7f);
        revision++;
        listener.sound(2);
        return true;
    }

    private void updateMissile(float dt) {
        missileCooldown = Math.max(0, missileCooldown - dt);
        if (missileDetonationRemaining > 0) {
            missileDetonationRemaining = Math.max(0, missileDetonationRemaining - dt);
        }
        if (missileFlight <= 0) return;
        missileFlight -= dt;
        if (missileFlight > 0) return;
        missileDetonateX = width * .5f;
        missileDetonateY = height * .5f;
        missileDetonationRemaining = missileDetonationDuration;
        clearHostileShots();
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            if (!e.spec.bodyDamages || e.x + halfWidth(e) < 0 || e.x - halfWidth(e) > width || e.y + halfHeight(e) < 0 || e.y - halfHeight(e) > height) continue;
            damage(e, e.spec.missileTier == 2 ? (int)Math.rint(e.maxHp * .1f) : e.spec.missileTier == 1 ? (int)Math.rint(e.maxHp * .6f) : e.hp);
        }
        shake = 1.2f;
        listener.sound(3);
    }

    private boolean isEncounterFinished() {
        int completion = encounter.getInt("Completion");
        if ((completion == 1 || completion == 2) && encounterClock >= encounter.getFloat("Duration")) return true;
        for (Spawn pending : schedule) if (!pending.spawned && !pending.optional) return false;
        if (completion == 1 || completion == 2) {
            for (boolean fired : optionalFired) if (!fired) return false;
            for (Spawn pending : schedule) if (!pending.spawned) return false;
            if (completion == 1 && encounter.getBoolean("CompleteWhenScheduleExhausted") && enemies.size == 0) return true;
            return encounterClock >= encounter.getFloat("Duration");
        }
        return requiredKilled == requiredTotal && (completion != 3 || requiredTotal > 0);
    }

    private void advanceEncounter() {
        if (save.encounter + 1 < mission.get("Encounters").size) {
            save.encounter++;
            save.encounterShardHighWater = 0;
            beginEncounter();
        } else {
            for (Pickup pickup : pickups) pickupPool.free(pickup);
            pickups.clear();
            stage = Stage.RESULT; stageClock = 0; held = false;
            announcement = save.mission == 8 ? "HOÀN THÀNH CHIẾN DỊCH" : "HOÀN THÀNH NHIỆM VỤ";
            if (!replay) {
                save.missionCleared = true;
                save.completed = save.mission == 8;
                save.unlockedMission = Math.max(save.unlockedMission, Math.min(8, save.mission + 1));
                save.bestScores[save.mission - 1] = Math.max(save.bestScores[save.mission - 1], save.missionScore);
                if (save.missionSeconds > 0 && (save.bestTimes[save.mission - 1] == 0 || save.missionSeconds < save.bestTimes[save.mission - 1]))
                    save.bestTimes[save.mission - 1] = save.missionSeconds;
                save.rewardedMissions[save.mission - 1] = true;
                boolean sameRegionNext = save.mission < 8 && mission.getString("RegionId").equals(catalog.missions[save.mission].getString("RegionId"));
                if (!sameRegionNext) listener.checkpoint(save);
            }
            if (!bossVictoryPlayed) listener.sound(38); revision++;
            if (!replay && save.mission < 8) nextMission();
        }
    }

    public void nextMission() { startNextMission(false); }

    private void startNextMission(boolean dev) {
        if (stage != Stage.RESULT || save.mission >= 8 || replay && !dev) return;
        clearActors();
        regionTravel = !mission.getString("RegionId").equals(catalog.missions[save.mission].getString("RegionId"));
        stage = regionTravel ? Stage.REGION_TRAVEL : Stage.MISSION_TRAVEL;
        stageClock = cinematicProgress = 0;
        announcement = "";
        if (regionTravel) {
            held = false; journeyPhase = 0;
            journeyDepartureX = playerX - width / 2; journeyDepartureY = playerY - height / 2;
            JsonValue journey = catalog.component(catalog.record("Scenes/02_Gameplay.unity"), "PlanetJourney");
            String[] names = {"glideSeconds", "accelerationSeconds", "turnSeconds", "decelerationSeconds", "revealSeconds"};
            for (int i = 0; i < names.length; i++) journeyDurations[i] = journey.getFloat(names[i]);
        } else {
            if (!replay) {
                com.badlogic.gdx.utils.Json json = new com.badlogic.gdx.utils.Json();
                CampaignStore.State incoming = json.fromJson(CampaignStore.State.class, json.toJson(save));
                incoming.mission++; incoming.encounter = 0;
                incoming.missionCleared = incoming.completed = incoming.regionReady = false;
                CampaignStore.resetMissionStats(incoming);
                listener.checkpoint(incoming);
            }
            travelSeconds = catalog.component(catalog.record("Scenes/02_Gameplay.unity"), "RegionCampaignFlow").getFloat("missionClearSeconds");
            JsonValue destination = catalog.missions[save.mission];
            JsonValue first = catalog.resolve(destination.get("Encounters").get(0));
            JsonValue view = catalog.resolve(first.get("CameraOverride"));
            requestView((view == null ? catalog.resolve(destination.get("Camera")) : view).get("View"), travelSeconds);
        }
        sound(35); sound(40); revision++;
    }

    private void tickJourney() {
        float t = journeyPhase == 5 ? 1 : Math.min(1, stageClock / journeyDurations[journeyPhase]);
        float smooth = t * t * (3 - 2 * t);
        journeyHeading = journeyPhase == 0 ? 0 : journeyPhase == 1 ? MathUtils.lerp(0, -20, t) : journeyPhase == 2 ? MathUtils.lerp(-20, -90, smooth) : -90;
        journeySpeed = journeyPhase == 0 ? .045f : journeyPhase == 1 ? MathUtils.lerp(.045f, 2.8f, t) : journeyPhase == 2 ? MathUtils.lerp(2.8f, 3.6f, t) : journeyPhase == 3 ? MathUtils.lerp(3.6f, .04f, smooth) : .015f;
        journeyPalette = journeyPhase == 2 ? t : journeyPhase >= 3 ? 1 : 0;
        journeyReveal = journeyPhase == 4 ? .08f + .92f * t : journeyPhase == 5 ? 1 : journeyPhase == 3 ? .08f * t : 0;
        float progress = journeyPhase == 0 ? .025f * t : journeyPhase == 1 ? .025f + .2f * t : journeyPhase == 2 ? .225f + .55f * t : journeyPhase == 3 ? .775f + .225f * t : 1;
        float eased = progress * progress * (3 - 2 * progress);
        playerX = MathUtils.lerp(width / 2 + journeyDepartureX, width * .18f, eased);
        playerY = MathUtils.lerp(height / 2 + journeyDepartureY, height * .5f, eased) + MathUtils.sin(progress * MathUtils.PI) * 1.3f;
        if (journeyPhase == 5 || t < 1) return;
        journeyPhase++; stageClock = 0;
        if (journeyPhase == 1 || journeyPhase == 2) sound(36);
        if (journeyPhase == 3) sound(37);
        if (journeyPhase == 1) requestView(mission.get("Intro").get("TravelView"), journeyDurations[1]);
        if (journeyPhase == 3) requestView(mission.get("Intro").get("ApproachView"), journeyDurations[3]);
        if (journeyPhase == 5) {
            announcement = "HÀNH TINH TIẾP THEO";
            save.regionReady = true;
            if (!replay) listener.checkpoint(save);
        }
        revision++;
    }

    private void advanceMission() {
        float x = playerX - width / 2, y = playerY - height / 2;
        save.mission++; save.encounter = 0; save.missionCleared = save.regionReady = false;
        CampaignStore.resetMissionStats(save);
        regionTravel = false;
        beginMission();
        playerX = width / 2 + x; playerY = height / 2 + y;
    }

    private Burst burst(String[] frames, float x, float y, float scale, float duration) {
        if (frames.length == 0 || bursts.size >= 64) return null;
        Burst b = burstPool.obtain(); b.frames = frames; b.x = x; b.y = y; b.scale = scale; b.duration = duration; b.rotation = rotation();
        bursts.add(b);
        return b;
    }

    private void updateBursts(float dt) {
        for (int i = corpses.size - 1; i >= 0; i--) {
            Corpse corpse = corpses.get(i); corpse.age += dt;
            if (corpse.age >= corpse.duration) {
                corpses.removeIndex(i); awardEnemy(corpse.enemy);
                if (!bossVictoryPlayed) { bossVictoryPlayed = true; sound(38); }
            }
        }
        for (int i = bursts.size - 1; i >= 0; i--) {
            Burst b = bursts.get(i);
            if (b.age < 0 && b.age + dt >= 0 && b.soundOnStart >= 0) sound(b.soundOnStart);
            b.age += dt;
            if (b.age >= 0) { b.vx += b.ax * dt; b.vy += b.ay * dt; b.x += b.vx * dt; b.y += b.vy * dt; }
            if (b.followPlayer) { b.x = playerX; b.y = playerY; b.rotation = presentationRotation(); }
            if (b.age >= b.duration) burstPool.free(bursts.removeIndex(i));
        }
    }

    private void clearHostileShots() {
        for (int i = shots.size - 1; i >= 0; i--) if (shots.get(i).hostile) shotPool.free(shots.removeIndex(i));
    }

    private void clearActors() {
        enemies.clear();
        for (Shot shot : shots) shotPool.free(shot);
        for (Burst b : bursts) burstPool.free(b);
        shots.clear(); bursts.clear(); corpses.clear();
        missileFlight = 0;
        missileDetonationRemaining = 0;
    }

    public void devApplyLoadout(int level, int branch, boolean fake) {
        if (!replay) return;
        if (level < 1 || level > 5 || branch < 0 || branch > 2) throw new IllegalArgumentException("Invalid dev loadout");
        java.util.Arrays.fill(save.weaponLevels, level); save.activeWeapon = branch;
        devFakeShots = fake; revision++;
    }

    public void devStartRoute(int route) {
        if (!replay) return;
        if (route == 2 || route == 3) devBossRoute = route;
        if (route >= 4) {
            stage = Stage.RESULT; save.missionCleared = true;
            startNextMission(true);
            if (route == 6 && regionTravel) {
                journeyPhase = 5; journeyHeading = -90; journeySpeed = .015f;
                journeyPalette = journeyReveal = 1; save.regionReady = true;
                announcement = "HÀNH TINH TIẾP THEO";
                requestView(mission.get("Intro").get("ApproachView"), 0);
                resize(width / height); playerX = width * .18f; playerY = height * .5f;
            }
            revision++;
        }
    }

    private void tickDevBossRoute() {
        if (!replay || devBossRoute == 0) return;
        for (Enemy enemy : enemies) {
            if (enemy.boss == null || !enemy.holding) continue;
            for (BossCombat.Node node : enemy.boss.nodes)
                if (node.hp > 0) enemy.boss.damageNode(node, node.hp);
            if (!canDamage(enemy)) return;
            int route = devBossRoute; devBossRoute = 0;
            damage(enemy, route == 3 ? enemy.hp : (float)Math.ceil(enemy.maxHp * .51f));
            return;
        }
    }

    public void devSpawnHostileShot() {
        if (!replay || !gameplayInput()) return;
        for (Enemy enemy : enemies) if (enemy.spec.fire != null) {
            launchPattern(enemy.spec.fire, null, playerX + (direction() == 1 ? 3 : 0),
                    playerY + (direction() == 0 ? 3 : 0), true, direction() == 0 ? -90 : 180);
            return;
        }
    }

    public void devGodModeToggle() {
        devGodMode = !devGodMode;
        if (devGodMode) player.protect(999999f);
        else player.protection = 0f;
        revision++;
    }

    public void devMaxWeapons() {
        if (save.weaponLevels != null) {
            for (int i = 0; i < save.weaponLevels.length; i++) save.weaponLevels[i] = 5;
        }
        revision++;
    }

    public void devKillAllEnemies() {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            damage(e, e.hp + 99999);
        }
        clearHostileShots();
        revision++;
    }

    public void devFullSupplies() {
        save.missiles = 3;
        save.lives = 5;
        save.shard += 500;
        player.shield = true;
        player.overdriveRemaining = 10f;
        player.fireRateMultiplier = 1.5f;
        player.droneRemaining = 15f;
        revision++;
    }

    public void devSkipEncounter() {
        advanceEncounter();
        revision++;
    }

    public void devJumpMission(int targetMission) {
        if (targetMission < 1 || targetMission > 8) return;
        save.mission = targetMission;
        save.encounter = 0;
        save.missionCleared = false;
        CampaignStore.resetMissionStats(save);
        regionTravel = false;
        beginMission();
        revision++;
    }
}
