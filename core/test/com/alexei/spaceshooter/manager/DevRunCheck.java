package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;

public final class DevRunCheck {
    private static CampaignWorld world(CampaignCatalog catalog, DevRunConfig config) {
        CampaignStore.State state = new CampaignStore.State();
        config.prepare(state, catalog);
        CampaignWorld world = new CampaignWorld(catalog, state, true, new CampaignWorld.Listener() {
            public void sound(int cue) {}
            public void checkpoint(CampaignStore.State state) { throw new AssertionError("Dev wrote campaign"); }
        });
        world.devGodModeToggle(); world.devStartRoute(config.route); return world;
    }
    public static void run(CampaignCatalog catalog) {
        DevRunConfig config = new DevRunConfig(); config.mission = 2; config.route = 1;
        boolean rejected = false;
        try { world(catalog, config); } catch (IllegalArgumentException expected) { rejected = true; }
        assert rejected : "Boss route must reject missions without boss";
        config.route = 5;
        CampaignWorld journey = world(catalog, config);
        assert journey.save.mission == 4 && journey.stage == CampaignWorld.Stage.REGION_TRAVEL;
        for (int i = 0; i < 600; i++) journey.update(1f / 60);
        assert journey.awaitingContinue() && journey.readyToContinue();
        assert journey.acceptBriefing() && journey.stage == CampaignWorld.Stage.APPROACH;
        for (int i = 0; i < 210; i++) journey.update(1f / 60);
        assert journey.save.mission == 5;
        config.route = 6;
        journey = world(catalog, config);
        assert journey.awaitingContinue() && journey.journeyReveal == 1 && journey.announcement.equals("HÀNH TINH TIẾP THEO");
        assert journey.acceptBriefing();
        config.route = 2; config.mission = 4;
        CampaignWorld phase = world(catalog, config);
        boolean phaseB = false;
        for (int i = 0; i < 600; i++) {
            phase.update(1f / 60);
            for (CampaignWorld.Enemy enemy : phase.enemies) if (enemy.boss != null && enemy.boss.phaseB) phaseB = true;
        }
        assert phaseB : "Phase B route applies after boss arrives";
        config.route = 0; config.mission = 1; config.weapon = 2; config.level = 4;
        CampaignWorld fake = world(catalog, config); fake.devFakeShots = true;
        CampaignWorld.Enemy target = null;
        for (int i = 0; i < 1200 && target == null; i++) {
            fake.update(1f / 60);
            for (CampaignWorld.Enemy enemy : fake.enemies) if (enemy.holding) target = enemy;
        }
        assert target != null;
        float hp = target.hp;
        CampaignWorld.Shot shot = new CampaignWorld.Shot();
        shot.x = shot.previousX = target.x; shot.y = shot.previousY = target.y;
        shot.damage = 99999; shot.life = 1; shot.splash = 10;
        fake.shots.add(shot); fake.update(1f / 60);
        assert target.hp == hp && shot.life <= 0 : "Fake shots still collide without damage";
        assert fake.save.activeWeapon == 2 && fake.save.weaponLevels[2] == 4;
        fake.devApplyLoadout(5, 1, false);
        assert fake.save.activeWeapon == 1 && fake.save.weaponLevels[1] == 5 && !fake.devFakeShots;
        CampaignWorld campaign = new CampaignWorld(catalog, new CampaignStore.State(), false, new CampaignWorld.Listener() {
            public void sound(int cue) {}
            public void checkpoint(CampaignStore.State state) {}
        });
        campaign.devApplyLoadout(5, 2, true); campaign.devStartRoute(5);
        assert campaign.save.mission == 1 && campaign.save.weaponLevels[0] == 1 && !campaign.devFakeShots : "Dev controls cannot mutate a campaign run";
        System.out.println("Dev: invalid boss route, isolated planet journey/tap, phase B and harmless projectile contact OK");
    }
}
