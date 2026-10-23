package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.alexei.spaceshooter.effect.MovementTrail;
import com.badlogic.gdx.files.FileHandle;

public final class CampaignContractCheck {
    private static CampaignWorld world(CampaignCatalog catalog, int mission, int encounter) {
        CampaignStore.State state = new CampaignStore.State(); state.mission = mission; state.encounter = encounter;
        return new CampaignWorld(catalog, state, true, new CampaignWorld.Listener() {
            public void sound(int cue) {}
            public void checkpoint(CampaignStore.State save) { throw new AssertionError("Dev writes save"); }
        });
    }

    public static void main(String[] args) {
        CampaignWorld.Shot curtain = new CampaignWorld.Shot();
        curtain.halfWidth = .95f; curtain.halfHeight = .225f; curtain.vy = -8.5f;
        curtain.y = curtain.previousY = 0;
        assert CampaignWorld.shotHitsPlayer(curtain, .8f, 0, .1f) : "Curtain uses authored width, not normal-bullet radius";
        assert !CampaignWorld.shotHitsPlayer(curtain, 1.2f, 0, .1f);
        curtain.vy = 0; curtain.vx = -8.5f;
        assert CampaignWorld.shotHitsPlayer(curtain, 0, .8f, .1f) : "Horizontal curtain rotates its collider";
        AudioVoices audio = new AudioVoices();
        for (int i = 0; i < 4; i++) assert audio.acquire(24 + i, 3, 4, 0, 1, .3f) >= 0;
        assert audio.acquire(28, 3, 4, 0, 1, .3f) == -1 : "Common pool has four voices";
        assert audio.acquire(0, 2, 2, 0, 1, .3f) == 6 : "Weapon pool remains available under common pressure";
        assert audio.acquire(13, 0, 1, 0, 1, .3f) == 0 : "Danger has reserved slots";
        audio.tick(0, 0);
        assert audio.gain(6, 0, false) < .3f : "Danger ducks weapon without stealing its slot";
        assert audio.acquire(13, 0, 1, 0, 1, .3f) == -1 : "Cue voice cap is retained";
        int uiSlot = audio.acquire(32, 5, 1, 0, 1, .2f);
        audio.clearGameplay(); assert audio.gain(uiSlot, 0, false) > 0 : "Screen change preserves UI click tail";
        audio.clear(); assert !audio.danger(0) && audio.acquire(10, 3, 4, 0, 1, .3f) == 8;
        BeamContact beam = new BeamContact();
        assert !beam.hit(0, -5, 0, 5, .2f, -2, 0, .1f);
        assert beam.hit(0, -5, 0, 5, .2f, 2, 0, .1f) : "Fast drag must not tunnel through active beam";
        beam.reset();
        assert !beam.hit(0, -5, 0, 5, .2f, -2, 0, .1f) : "Reset must discard prior active window";
        assert beam.hit(-4, -5, -4, 5, .2f, -2, 0, .1f) : "Sweep must catch moving beam across stationary player";
        CampaignCatalog catalog = new CampaignCatalog(new FileHandle(args[0]));
        DevRunCheck.run(catalog);
        MovementTrail trail = new MovementTrail();
        trail.update(0, 0, 0, .16f, true);
        trail.update(1, 1, .02f, .16f, true);
        assert trail.size() == 2 : "Movement joins previous point to new point";
        trail.update(1, 1, .02f, .16f, true);
        assert trail.size() == 2 : "Pause cannot add trail points";
        trail.update(1, 1, .3f, .16f, true);
        assert trail.size() == 0 : "Stationary trail expires";
        trail.update(2, 1, .31f, .16f, false);
        assert trail.size() == 0 : "Disabled/no trail leaves no ribbon";

        CampaignWorld storm = world(catalog, 2, 1);
        int sampled = 0;
        for (int frame = 0; frame < 600; frame++) {
            storm.player.protection = 100;
            storm.update(1f / 60);
            for (CampaignWorld.Enemy enemy : storm.enemies) {
                if (!enemy.spec.asteroid || !enemy.group.getBoolean("ContinuousTransit")) continue;
                float total = enemy.group.getFloat("EntryDuration") + enemy.group.getFloat("HoldDuration") + enemy.group.getFloat("ExitDuration");
                float t = Math.min(1, enemy.age / total);
                float fromX = enemy.group.get("EntryAnchor").getFloat("x"), toX = enemy.group.get("ExitAnchor").getFloat("x");
                float fromY = enemy.group.get("EntryAnchor").getFloat("y"), toY = enemy.group.get("ExitAnchor").getFloat("y");
                assert Math.abs(enemy.x / storm.width - (fromX + (toX - fromX) * t)) < .0001f : "Asteroid changed origin mid-flight";
                assert Math.abs(enemy.y / storm.height - (fromY + (toY - fromY) * t)) < .0001f;
                if (enemy.age > .65f) sampled++;
            }
        }
        assert sampled > 50 : "Exercise flight beyond entry threshold";

        CampaignWorld world = world(catalog, 1, 0);
        while (world.stage != CampaignWorld.Stage.RUNNING) world.update(1f / 60);
        world.held = true; world.hitPlayer();
        assert world.held : "Death keeps valid held intent";
        for (int i = 0; i < 80; i++) world.update(1f / 60);
        assert world.player.canInput() && world.held;
        world.enemies.clear(); world.shots.clear(); world.held = false;
        CampaignWorld.Shot hostile = new CampaignWorld.Shot();
        hostile.hostile = true; hostile.life = 10; hostile.x = hostile.previousX = world.width * .9f;
        hostile.y = hostile.previousY = world.height * .9f; world.shots.add(hostile);
        assert world.missile();
        for (int i = 0; i < 18; i++) world.update(1f / 60);
        assert world.shots.contains(hostile, true) : "Launch must not clear bullets";
        for (int i = 0; i < 3; i++) world.update(1f / 60);
        assert !world.shots.contains(hostile, true) : "Detonation clears hostile bullets";
        CampaignWorld.Shot after = new CampaignWorld.Shot(); after.hostile = true; after.life = 10;
        after.x = after.previousX = world.width * .9f; after.y = after.previousY = world.height * .9f;
        world.shots.add(after); world.update(1f / 60);
        assert world.shots.contains(after, true) : "Splash artwork cannot continuously clear new bullets";
        world.stage = CampaignWorld.Stage.GAME_OVER;
        float clock = world.clock;
        for (int i = 0; i < 120; i++) world.update(1f / 60);
        assert world.clock == clock && world.stage == CampaignWorld.Stage.GAME_OVER : "Terminal freezes until explicit Retry";
        assert !world.canUseMissile();
        com.badlogic.gdx.utils.JsonValue shield = catalog.asset("Data/Rewards/ShieldCell.asset");
        CombatState combat = new CombatState(new CampaignStore.State());
        assert combat.pickup(shield) && combat.pickup(shield) && combat.shield : "Shield refresh does not stack or reject collection";
        com.alexei.spaceshooter.screen.HudLayoutCheck.main(args);
        System.out.println("Contracts: continuous asteroid transit, joined trail/expiry/pause, held respawn, detonation-only clear, terminal freeze OK");
    }
}
