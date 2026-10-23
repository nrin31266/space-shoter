package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.files.FileHandle;

public final class CampaignParityCheck {
    private static CampaignWorld world(CampaignCatalog catalog) {
        CampaignWorld world = new CampaignWorld(catalog, new CampaignStore.State(), true, new CampaignWorld.Listener() {
            public void sound(int cue) {}
            public void checkpoint(CampaignStore.State state) { throw new AssertionError("Isolated run wrote campaign"); }
        });
        while (world.stage == CampaignWorld.Stage.INTRO) world.update(1f / 60);
        return world;
    }

    public static void main(String[] args) {
        CampaignCatalog catalog = new CampaignCatalog(new FileHandle(args[0]));
        CampaignWorld world = world(catalog);
        CampaignWorld.Shot existing = new CampaignWorld.Shot();
        existing.hostile = true; existing.life = 5; existing.x = existing.previousX = world.width * .8f;
        existing.y = existing.previousY = world.height * .8f;
        world.shots.add(existing);
        world.hitPlayer();
        world.update(1f / 60);
        assert world.save.lives == 2 && world.shots.contains(existing, true) : "Death must preserve existing hostile attacks";
        for (int i = 0; i < 40; i++) world.update(1f / 60);
        assert !world.player.canInput() && world.player.respawnRemaining > 0 : "Respawn entrance precedes input";
        for (int i = 0; i < 40 && !world.player.canInput(); i++) world.update(1f / 60);
        assert world.player.canInput() && world.player.protection > 0 && world.player.enemyFireGrace > 0;

        world = world(catalog);
        world.save.lives = 1; world.hitPlayer();
        assert world.stage == CampaignWorld.Stage.RUNNING : "Game-over waits for death presentation";
        for (int i = 0; i < 40; i++) world.update(1f / 60);
        assert world.stage == CampaignWorld.Stage.GAME_OVER;

        world = world(catalog);
        world.playerX = world.width * .5f; world.playerY = world.height * .5f;
        world.ortho += 2; world.resize(world.width / world.height);
        assert Math.abs(world.playerX - world.width * .5f) < .001f && Math.abs(world.playerY - world.height * .5f) < .001f
                : "Zoom keeps world position around camera center";

        world = world(catalog);
        world.player.protection = 100;
        for (int i = 0; i < 180; i++) world.update(1f / 60);
        world.shots.clear(); world.held = true;
        for (int i = 0; i < 9; i++) world.update(1f / 60);
        int friendly = 0;
        for (CampaignWorld.Shot shot : world.shots) if (!shot.hostile) friendly++;
        assert friendly <= 1 : "No fire backlog after release";
        System.out.println("Source parity: preserved attacks on death, delayed game-over, respawn/input, zoom and fire backlog OK");
    }
}
