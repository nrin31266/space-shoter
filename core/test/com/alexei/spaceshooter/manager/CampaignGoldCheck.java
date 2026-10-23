package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public final class CampaignGoldCheck {
    private static final float DT = 1f / 60;
    private static CampaignWorld world(CampaignCatalog catalog, int mission) {
        CampaignStore.State state = new CampaignStore.State(); state.mission = mission;
        CampaignWorld world = new CampaignWorld(catalog, state, true, new CampaignWorld.Listener() {
            public void sound(int cue) {}
            public void checkpoint(CampaignStore.State state) { throw new AssertionError("Replay wrote a save"); }
        });
        world.stage = CampaignWorld.Stage.CLEAR; world.stageClock = -100;
        world.player.protection = 100;
        return world;
    }
    public static void main(String[] args) {
        CampaignCatalog catalog = new CampaignCatalog(new FileHandle(args[0]));
        JsonValue shard = catalog.asset("Data/Rewards/StellarShard.asset");
        for (int mission : new int[]{1, 5}) {
            CampaignWorld world = world(catalog, mission);
            world.playerX = world.width * .8f; world.playerY = world.height * .8f;
            CampaignWorld.Pickup gold = world.drop(shard, -100, -100, true);
            assert close(gold.x, world.width * (mission == 1 ? .05f : .15f));
            assert close(gold.y, world.height * (mission == 1 ? .15f : .05f));
            gold.x = world.width * .5f; gold.y = world.height * .5f;
            float side = mission == 1 ? gold.x : gold.y;
            int maximumBounces = 0;
            for (int tick = 0; tick < 360 && world.pickups.contains(gold, true); tick++) {
                world.update(DT);
                maximumBounces = Math.max(maximumBounces, gold.bounceCount);
                if (world.pickups.contains(gold, true)) assert close(mission == 1 ? gold.x : gold.y, side) : "No pickup attraction";
            }
            assert maximumBounces == 2 : "Gold bounces exactly twice in either direction";
            assert world.pickups.size == 0 : "Gold falls out after the second bounce";
        }
        CampaignWorld world = world(catalog, 1);
        for (int i = 0; i < 6; i++) assert world.drop(shard, 1, world.height * .8f, false) != null;
        assert world.drop(shard, 1, 4, false) == null : "Scene authored normal pickup limit is six";
        assert world.drop(shard, 1, 4, true) != null : "Authored quota bypasses normal cap";

        world = world(catalog, 1);
        world.playerX = world.width * .25f; world.playerY = world.height * .5f;
        world.drop(shard, world.width * .5f, world.playerY, true);
        world.move(world.width * .5f, 0);
        world.update(DT);
        assert world.pickups.size == 0 && world.save.shard == shard.getInt("Amount") : "Fast drag sweeps pickup contact";

        world = world(catalog, 1);
        world.playerX = world.width * .5f; world.playerY = world.height * .5f;
        world.drop(shard, world.playerX + .5f, world.playerY + .5f, true);
        world.update(DT);
        assert world.pickups.size == 1 : "Collection is radius .6, not a generous rectangle";
        world.stage = CampaignWorld.Stage.BRIEFING;
        world.playerX = world.pickups.first().x; world.playerY = world.pickups.first().y;
        world.update(DT);
        assert world.pickups.size == 1 : "No collection while input is locked";

        for (int mission : new int[]{1, 5}) {
            world = world(catalog, mission);
            CampaignWorld.Pickup gold = world.drop(shard, world.width * .5f, world.height * .6f, true);
            float x = gold.x, y = gold.y;
            world.eject(gold, -1.5f, 1);
            world.update(DT);
            if (mission == 1) assert gold.x < x : "Supply ejects sideways";
            else assert gold.y > y : "Sideways eject rotates with combat direction";
        }
        world = world(catalog, 1);
        JsonValue shortLife = new JsonReader().parse(shard.toJson(JsonWriter.OutputType.json));
        shortLife.get("Lifetime").set(.15f, null);
        CampaignWorld.Pickup fading = world.drop(shortLife, 1, world.height * .8f, true);
        world.update(DT);
        assert fading.opacity > 0 && fading.opacity < .5f;
        for (int i = 0; i < 12; i++) world.update(DT);
        assert world.pickups.size == 0 : "Lifetime expires before pickup contact";
        checkBossSupply(catalog);
        System.out.println("Gold: source RNG, spawn cap/clamp, two bounces, direction/aspect, eject, sweep, input gate and lifetime OK");
    }
    private static void checkBossSupply(CampaignCatalog catalog) {
        CampaignStore.State state = new CampaignStore.State(); state.mission = 4; state.encounter = 1;
        CampaignWorld world = new CampaignWorld(catalog, state, true, new CampaignWorld.Listener() {
            public void sound(int cue) {}
            public void checkpoint(CampaignStore.State ignored) { throw new AssertionError("Replay wrote a save"); }
        });
        CampaignWorld.Enemy boss = null;
        for (int tick = 0; tick < 600; tick++) {
            world.player.protection = 100; world.update(DT);
            for (CampaignWorld.Enemy enemy : world.enemies) if (enemy.boss != null && enemy.holding) boss = enemy;
            if (boss != null) break;
        }
        assert boss != null : "Production boss arrives";
        CampaignWorld.Shot probe = new CampaignWorld.Shot();
        probe.x = probe.previousX = boss.x; probe.y = probe.previousY = boss.y;
        assert boss.boss.coreHitFraction(probe, 0, 0) != Float.POSITIVE_INFINITY : "Core receives contact";
        probe.x = probe.previousX = boss.x + 6 * boss.scale;
        probe.y = probe.previousY = boss.y - 1.8f * boss.scale;
        assert boss.boss.coreHitFraction(probe, 0, 0) == Float.POSITIVE_INFINITY : "Flight envelope is not a boss hitbox";
        for (BossCombat.Node node : boss.boss.nodes) node.hp = 0;
        CampaignWorld.Shot shot = new CampaignWorld.Shot();
        shot.x = shot.previousX = boss.x; shot.y = shot.previousY = boss.y;
        shot.life = 1; shot.damage = boss.hp + 1;
        world.shots.add(shot); world.update(DT);
        assert world.enemies.size == 0 : "Boss defeated through collision";
        assert world.pickups.size == 0 && world.corpses.size == 1 : "Boss reward waits for staged destruction";
        for (int tick = 0; tick < 300 && world.corpses.size > 0; tick++) { world.player.protection = 100; world.update(DT); }
        assert world.corpses.size == 0;
        int shards = 0, majors = 0;
        for (CampaignWorld.Pickup pickup : world.pickups) {
            if (pickup.definition.getInt("Kind") == 0) shards++; else majors++;
        }
        assert shards == boss.rewards.getInt("BossShardCount") && majors == 2 : "Boss emits authored gold bundle plus core and support";
        assert world.save.supplyCaptured == 0 : "Destroying a supply does not count as capturing it";
        CampaignWorld.Pickup support = null;
        for (CampaignWorld.Pickup pickup : world.pickups) if (pickup.definition.getInt("Kind") != 0) { support = pickup; break; }
        support.x = world.playerX; support.y = world.playerY;
        world.update(DT);
        assert world.save.supplyCaptured == 1 : "Collecting either half captures supply exactly once";
        for (int tick = 0; tick < 150; tick++) { world.player.protection = 100; world.update(DT); }
        assert world.save.supplyCaptured == 1;
    }

    private static boolean close(float first, float second) { return Math.abs(first - second) < .0001f; }
}
