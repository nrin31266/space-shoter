package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.utils.JsonValue;

public final class RewardRules {
    public static final int KIND_SHARD = 0;
    public static final int KIND_WEAPON_CORE = 1;
    public static final int KIND_SHIELD = 2;
    public static final int KIND_OVERDRIVE = 3;
    public static final int KIND_SUPPORT_DRONE = 4;
    public static final int KIND_MISSILE_AMMO = 5;
    public static final int KIND_EXTRA_LIFE = 6;

    public static final int SOURCE_NORMAL = 0;
    public static final int SOURCE_ELITE = 1;
    public static final int SOURCE_MAJOR_CRATE = 2;
    public static final int SOURCE_RESOURCE = 3;

    private final CampaignCatalog catalog;
    private final CombatState player;
    private final long seed;
    private final SourceRandom rollerRandom;
    private final SourceRandom quotaRandom;

    private int lowLifeCratesWithoutDefense;
    private int remainingEnemies;
    private int quotaRemaining;
    private int kills;
    private int firstDeadline;

    public RewardRules(CampaignCatalog catalog, CombatState player, long seed) {
        this.catalog = catalog;
        this.player = player;
        this.seed = seed;
        this.rollerRandom = new SourceRandom((int)seed);
        this.quotaRandom = new SourceRandom((int)seed);
    }

    public void beginQuota(int enemies, int quota, int firstByKill) {
        if (enemies < 0 || quota < 0 || quota > enemies || firstByKill < 0
                || firstByKill > enemies || (firstByKill > 0 && quota == 0)) {
            throw new IllegalArgumentException("Invalid encounter quota configuration");
        }
        quotaRandom.reset((int)seed);
        remainingEnemies = enemies;
        quotaRemaining = quota;
        kills = 0;
        firstDeadline = firstByKill;
    }

    public void beginEncounter(String mission, String encounter, int enemies, int quota, int firstByKill) {
        beginQuota(enemies, quota, firstByKill);
        int encounterSeed = (int)seed;
        String key = mission + "/" + encounter;
        for (int i = 0; i < key.length(); i++) encounterSeed = (encounterSeed ^ key.charAt(i)) * 16777619;
        rollerRandom.reset(encounterSeed);
        quotaRandom.reset(encounterSeed ^ 0x51f15e);
    }

    public int remaining() {
        return quotaRemaining;
    }

    public boolean resolveKill(boolean satisfiesFloor) {
        if (remainingEnemies <= 0) return false;
        kills++;
        boolean deadline = firstDeadline > 0 && kills >= firstDeadline;
        boolean force = !satisfiesFloor && quotaRemaining > 0
                && (deadline || quotaRandom.nextInt(remainingEnemies) < quotaRemaining);
        remainingEnemies--;
        if (satisfiesFloor || force) {
            quotaRemaining = Math.max(0, quotaRemaining - 1);
            firstDeadline = 0;
        }
        return force;
    }

    public void resolveEscape() {
        remainingEnemies = Math.max(0, remainingEnemies - 1);
    }

    public boolean resolveFallback() {
        if (quotaRemaining <= 0) return false;
        quotaRemaining--;
        return true;
    }

    public JsonValue direct(JsonValue table) {
        if (table == null) return null;
        JsonValue guaranteed = resolve(table.get("GuaranteedPickup"));
        if (guaranteed != null) return guaranteed;
        if (table.getInt("Source", SOURCE_NORMAL) == SOURCE_RESOURCE) {
            return resolve(table.get("Shard"));
        }
        if (rollerRandom.nextDouble() < table.getFloat("MajorChance", 0.0075f)) {
            return selectMajor(table, false, null);
        }
        return rollerRandom.nextDouble() < table.getFloat("ShardChance", 0.035f)
                ? resolve(table.get("Shard"))
                : null;
    }

    public JsonValue selectMajor(JsonValue table, boolean crate, Boolean weaponCore) {
        if (table == null) return null;
        JsonValue guaranteed = resolve(table.get("GuaranteedPickup"));
        if (guaranteed != null && canUse(guaranteed)
                && (weaponCore == null || (guaranteed.getInt("Kind", -1) == KIND_WEAPON_CORE) == weaponCore.booleanValue())) {
            return guaranteed;
        }

        boolean pity = crate && player != null && player.save.lives == 1 && lowLifeCratesWithoutDefense >= 2;
        JsonValue majorItems = table.get("MajorItems");
        if (majorItems == null) return null;

        double total = 0;
        for (JsonValue entry = majorItems.child; entry != null; entry = entry.next) {
            total += weight(entry, pity, weaponCore);
        }

        double roll = rollerRandom.nextDouble() * total;
        for (JsonValue entry = majorItems.child; entry != null; entry = entry.next) {
            float w = weight(entry, pity, weaponCore);
            if (w <= 0) continue;
            roll -= w;
            if (roll <= 0) {
                return resolve(entry.get("Item"));
            }
        }
        return null;
    }

    public void recordCrate(JsonValue item, int livesBeforeCollection) {
        if (isDefensive(item) || livesBeforeCollection > 1) {
            lowLifeCratesWithoutDefense = 0;
        } else {
            lowLifeCratesWithoutDefense++;
        }
    }

    private float weight(JsonValue entry, boolean pity, Boolean weaponCore) {
        if (entry == null) return 0;
        JsonValue item = resolve(entry.get("Item"));
        float baseWeight = entry.getFloat("Weight", 1f);
        if (item == null || baseWeight <= 0 || !canUse(item)) return 0;

        int kind = item.getInt("Kind", -1);
        if (weaponCore != null && (kind == KIND_WEAPON_CORE) != weaponCore.booleanValue()) return 0;

        boolean defensive = isDefensive(item);
        if (pity && !defensive) return 0;

        float w = baseWeight;
        if (player != null && player.save.lives == 1 && defensive) {
            w *= 3f;
        }
        if (kind == KIND_WEAPON_CORE && player != null) {
            int branch = item.getInt("Branch", 0);
            if (branch >= 0 && branch < player.save.weaponLevels.length && player.save.weaponLevels[branch] == 0) {
                w *= 2f;
            }
        }
        return w;
    }

    private boolean canUse(JsonValue item) {
        return player == null || player.canUse(item);
    }

    private static boolean isDefensive(JsonValue item) {
        if (item == null) return false;
        int kind = item.getInt("Kind", -1);
        return kind == KIND_SHIELD || kind == KIND_EXTRA_LIFE;
    }

    // Giữ chuỗi seeded roll như System.Random nguồn để Retry lặp lại đúng reward.
    static final class SourceRandom {
        private final int[] values = new int[56];
        private int index, nextIndex;
        SourceRandom(int seed) { reset(seed); }
        void reset(int seed) {
            int previous = 161803398 - (seed == Integer.MIN_VALUE ? Integer.MAX_VALUE : Math.abs(seed));
            values[55] = previous;
            int current = 1;
            for (int i = 1; i < 55; i++) {
                int slot = (21 * i) % 55;
                values[slot] = current;
                current = previous - current;
                if (current < 0) current += Integer.MAX_VALUE;
                previous = values[slot];
            }
            for (int pass = 0; pass < 4; pass++) for (int i = 1; i < 56; i++) {
                values[i] -= values[1 + (i + 30) % 55];
                if (values[i] < 0) values[i] += Integer.MAX_VALUE;
            }
            index = 0; nextIndex = 21;
        }
        int sample() {
            if (++index >= 56) index = 1;
            if (++nextIndex >= 56) nextIndex = 1;
            int value = values[index] - values[nextIndex];
            if (value == Integer.MAX_VALUE) value--;
            if (value < 0) value += Integer.MAX_VALUE;
            values[index] = value;
            return value;
        }
        double nextDouble() { return sample() * (1.0 / Integer.MAX_VALUE); }
        int nextInt(int bound) { return (int)(nextDouble() * bound); }
    }

    private JsonValue resolve(JsonValue ref) {
        return catalog != null ? catalog.resolve(ref) : null;
    }
}
