package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

public final class CampaignCombatCheck {
    public static void main(String[] args) {
        CampaignCatalog catalog = new CampaignCatalog(new FileHandle(args[0]));
        int bosses = 0, directed = 0;
        boolean[] bossMissions = new boolean[8];
        for (int mission = 1; mission <= 8; mission++) {
            for (int encounter = 0; encounter < catalog.missions[mission - 1].get("Encounters").size; encounter++) {
                CampaignStore.State state = new CampaignStore.State();
                state.mission = mission; state.encounter = encounter;
                CampaignWorld world = new CampaignWorld(catalog, state, true, new CampaignWorld.Listener() {
                    public void sound(int cue) {}
                    public void checkpoint(CampaignStore.State save) { throw new AssertionError("Replay wrote campaign"); }
                });
                boolean bossSeen = false, bossAttack = false, directedSeen = false, directedAttack = false, boundsChecked = false;
                CampaignWorld.Enemy boss = null;
                for (int tick = 0; tick < 60 * 70; tick++) {
                    world.player.protection = 100;
                    world.update(1f / 60);
                    if (!boundsChecked && world.stage == CampaignWorld.Stage.RUNNING) {
                        world.move(10000, 10000);
                        assert world.playerY <= world.height * (world.playableY + world.playableHeight) : "Playable height ignored";
                        assert world.playerX <= world.width * (world.playableX + world.playableWidth) : "Playable width ignored";
                        boundsChecked = true;
                    }
                    for (CampaignWorld.Enemy enemy : world.enemies) {
                        assert Float.isFinite(enemy.x) && Float.isFinite(enemy.y);
                        if (enemy.boss != null) { bossSeen = true; boss = enemy; if (enemy.boss.busy()) bossAttack = true; }
                        if (enemy.directed != null) {
                            directedSeen = true;
                            if (enemy.directed.attackCount > 0) directedAttack = true;
                        }
                    }
                }
                if (bossSeen) {
                    if (!bossMissions[mission - 1]) { bossMissions[mission - 1] = true; bosses++; }
                    assert bossAttack : "Boss did not start a real attack: " + mission;
                    for (BossCombat.Node node : boss.boss.nodes) node.hp = 0;
                    boss.hp = boss.maxHp * .1f;
                    for (int tick = 0; tick < 240; tick++) { world.player.protection = 100; world.update(1f / 60); }
                    assert !boss.boss.blocksCoreDamage() && boss.boss.phaseB : "Boss core/phase transition";
                    boss.boss.cancel();
                    assert !boss.boss.busy() && !boss.motionPaused : "Boss cancel";
                    for (BossCombat.Beam beam : boss.boss.beams) assert !beam.active && !beam.warning;
                }
                if (directedSeen) { directed++; assert directedAttack : "Directed attack never became active: " + mission + "/" + encounter; }
            }
        }
        assert bosses == 2 && directed > 0 : "Production combat coverage: bosses=" + bosses + ", directed=" + directed;
        CampaignStore.State cleared = new CampaignStore.State(); cleared.missionCleared = true;
        CampaignWorld resumed = new CampaignWorld(catalog, cleared, false, new CampaignWorld.Listener() {
            public void sound(int cue) {}
            public void checkpoint(CampaignStore.State state) { throw new AssertionError("Cleared Continue wrote checkpoint"); }
        });
        assert resumed.stage == CampaignWorld.Stage.MISSION_TRAVEL && resumed.enemies.size == 0;
        System.out.println("Real-time attacks, boss phases/cancel, replay isolation, playable bounds and cleared Continue: OK");
    }
}
