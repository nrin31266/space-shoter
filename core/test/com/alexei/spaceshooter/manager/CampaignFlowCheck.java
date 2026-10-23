package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.files.FileHandle;

public final class CampaignFlowCheck {
    private static final float DT = 1f / 60;
    private static CampaignWorld world(CampaignCatalog catalog, CampaignStore.State state, boolean replay) {
        return new CampaignWorld(catalog, state, replay, new CampaignWorld.Listener() {
            public void sound(int cue) {}
            public void checkpoint(CampaignStore.State state) { CampaignStore.validate(state); }
        });
    }
    public static void main(String[] args) {
        CampaignCatalog catalog = new CampaignCatalog(new FileHandle(args[0]));
        CampaignWorld world = world(catalog, new CampaignStore.State(), true);
        world.introduceMission();
        assert !world.gameplayInput() && !world.acceptBriefing();
        for (int i = 0; i < 20; i++) world.update(DT);
        assert world.acceptBriefing() && world.gameplayInput() : "Tap releases input before arrival";
        world.held = true;
        float x = world.playerX;
        world.move(.5f, 0);
        assert world.playerX > x;
        boolean fired = false;
        for (int i = 0; i < 180; i++) {
            world.update(DT);
            assert world.enemies.size == 0 : "No enemy scheduling during approach/travel/brake";
            for (CampaignWorld.Shot shot : world.shots) if (!shot.hostile) fired = true;
        }
        assert fired : "Player can fire during approach";
        while (world.stage != CampaignWorld.Stage.RUNNING) world.update(DT);
        assert world.encounterClock == 0 : "Mission title lead is independent from encounter clock";
        float title = world.encounter.getFloat("TitleSeconds");
        for (int i = 0; i < 10; i++) world.update(DT);
        assert world.encounterClock > 0 && world.encounterClock < title : "Title must not delay encounter scheduling";

        world = world(new CampaignCatalog(new FileHandle(args[0])), new CampaignStore.State(), true);
        world.encounter.get("Duration").set(.01f, null);
        world.encounter.get("Completion").set(1, null);
        CampaignWorld.Shot shot = new CampaignWorld.Shot();
        shot.hostile = true; shot.x = shot.previousX = world.width / 2;
        shot.y = shot.previousY = world.height * .9f; shot.vx = 3; shot.life = 20;
        world.shots.add(shot);
        for (int i = 0; i < 180 && world.stage == CampaignWorld.Stage.RUNNING; i++) world.update(DT);
        assert world.stage == CampaignWorld.Stage.PROJECTILE_DRAIN : "Visible hostile flight precedes clear pause: " + world.stage + " clock=" + world.encounterClock + " shots=" + world.shots.size;
        assert world.gameplayInput();
        world.update(DT);
        assert world.stage == CampaignWorld.Stage.PROJECTILE_DRAIN;
        world.shots.clear(); world.update(DT);
        assert world.stage == CampaignWorld.Stage.CLEAR : "Intermediate encounter skips reward grace";
        int previous = world.save.encounter;
        while (world.stage == CampaignWorld.Stage.CLEAR) world.update(DT);
        assert world.stage == CampaignWorld.Stage.HANDOFF && world.save.encounter == previous;
        while (world.stage == CampaignWorld.Stage.HANDOFF) world.update(DT);
        assert world.save.encounter == previous + 1 && world.stage == CampaignWorld.Stage.RUNNING;

        CampaignStore.State cleared = new CampaignStore.State();
        cleared.mission = 4; cleared.encounter = 1; cleared.missionCleared = true;
        world = world(catalog, cleared, false);
        assert world.stage == CampaignWorld.Stage.REGION_TRAVEL && !world.gameplayInput();
        for (int i = 0; i < 600; i++) world.update(DT);
        assert world.awaitingContinue() && world.save.mission == 4 && world.save.regionReady;
        CampaignWorld restored = world(catalog, cleared, false);
        assert restored.awaitingContinue() : "Continue restores destination without replaying journey";
        assert Math.abs(restored.playerX / restored.width - .18f) < .001f && Math.abs(restored.playerY / restored.height - .5f) < .001f : "Restored planet arrival preserves ship pose";
        assert restored.acceptBriefing() && restored.gameplayInput() && restored.direction() == 1;
        int arrivalFrames = 0;
        while (restored.save.mission == 4 && arrivalFrames < 300) { restored.update(DT); arrivalFrames++; }
        assert arrivalFrames >= 197 && arrivalFrames <= 201 : "Planet 2 arrival uses .7/1.8/.8 seconds: " + arrivalFrames;
        assert restored.save.mission == 5 && !restored.save.regionReady;
        CampaignWorld dying = world(catalog, new CampaignStore.State(), false);
        dying.stage = CampaignWorld.Stage.HANDOFF; dying.stageClock = 10;
        dying.save.lives = 1; dying.hitPlayer();
        for (int i = 0; i < 40; i++) dying.update(DT);
        assert dying.stage == CampaignWorld.Stage.GAME_OVER && dying.save.encounter == 0 : "Last life during handoff must not write an invalid checkpoint";

        CampaignWorld pickupWorld = world(catalog, new CampaignStore.State(), true);
        pickupWorld.drop(catalog.asset("Data/Rewards/ShieldCell.asset"), pickupWorld.playerX, pickupWorld.playerY, true);
        pickupWorld.update(DT);
        assert pickupWorld.player.shield && pickupWorld.pickupAnnouncement.equals("KHIÊN") && pickupWorld.pickupCueRemaining > 1;
        for (int i = 0; i < 75; i++) pickupWorld.update(DT);
        assert pickupWorld.pickupCueRemaining == 0 : "Pickup cue expires independently from powerup";
        System.out.println("Flow: tap/input, intro schedule/title, visible projectile drain, clear/travel and region-ready Continue OK");
    }
}
