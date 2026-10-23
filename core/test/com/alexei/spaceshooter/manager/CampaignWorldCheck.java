package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.files.FileHandle;

public final class CampaignWorldCheck {
    public static void main(String[] args) {
        CampaignCatalog catalog = new CampaignCatalog(new FileHandle(args[0]));
        int[] checkpoints = {0};
        CampaignWorld world = new CampaignWorld(catalog, new CampaignStore.State(), false, new CampaignWorld.Listener() {
            public void sound(int cue) {}
            public void checkpoint(CampaignStore.State state) { CampaignStore.validate(state); checkpoints[0]++; }
        });
        world.resize(20f / 9);
        while (world.stage == CampaignWorld.Stage.INTRO) world.update(1f / 60);
        CampaignWorld.Shot hostile = new CampaignWorld.Shot();
        hostile.hostile = true;
        hostile.x = hostile.previousX = world.playerX;
        hostile.y = hostile.previousY = world.playerY;
        hostile.life = 1;
        world.shots.add(hostile);
        world.update(1f / 60);
        assert world.save.lives == 2 && world.shots.size == 0 : "Death cleanup during collision must not invalidate list";
        int completed = 0;
        for (int step = 0; step < 60 * 1800; step++) {
            if (world.awaitingContinue()) world.acceptBriefing();
            world.player.protection = 10;
            for (CampaignWorld.Enemy enemy : world.enemies) {
                if (enemy.boss != null) for (BossCombat.Node node : enemy.boss.nodes) {
                    if (node.hp <= 0) continue;
                    CampaignWorld.Shot nodeShot = new CampaignWorld.Shot();
                    nodeShot.x = nodeShot.previousX = node.x; nodeShot.y = nodeShot.previousY = node.y;
                    nodeShot.life = 1; nodeShot.damage = node.hp + 1;
                    world.shots.add(nodeShot);
                }
                CampaignWorld.Shot shot = new CampaignWorld.Shot();
                shot.x = shot.previousX = enemy.x; shot.y = shot.previousY = enemy.y;
                shot.life = 1; shot.damage = enemy.hp + 1;
                world.shots.add(shot);
            }
            world.update(1f / 60);
            completed = world.save.mission - 1;
            if (world.stage == CampaignWorld.Stage.RESULT && world.save.completed) { completed = 8; break; }
        }
        assert completed == 8 : "Campaign stuck at mission " + world.save.mission + ", encounter " + world.save.encounter + ", " + world.stage;
        assert checkpoints[0] >= 29 : checkpoints[0];
        assert world.save.completed && world.save.unlockedMission == 8;
        System.out.println("8 missions / 21 encounters: schedule, config references, cleanup and checkpoints OK (test damage)");
    }
}
