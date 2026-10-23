package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;

/** Cấu hình thử chỉ sống trong session, không serialize vào profile. */
public final class DevRunConfig {
    public static final String[] ROUTES = {"Đợt đã chọn", "Boss", "Boss · phase B", "Boss · chiến thắng", "Chuyển nhiệm vụ", "Bay tới hành tinh 2", "Hành tinh 2 · chờ chạm"};
    public int mission = 1, encounter, route, level = 1, weapon;
    public boolean god = true, skipIntro = true, fakeShots;

    public void prepare(CampaignStore.State copy, CampaignCatalog catalog) {
        if (mission < 1 || mission > 8 || route < 0 || route >= ROUTES.length || level < 1 || level > 5 || weapon < 0 || weapon > 2)
            throw new IllegalArgumentException("Invalid dev selection");
        copy.mission = route >= 5 ? 4 : mission;
        if (route == 4 && mission == 8) throw new IllegalArgumentException("M8 không có nhiệm vụ kế tiếp");
        int count = catalog.missions[copy.mission - 1].get("Encounters").size;
        if (route >= 1 && route <= 3) {
            int bossIndex = -1;
            for (int i = 0; i < count; i++)
                if (catalog.resolve(catalog.missions[copy.mission - 1].get("Encounters").get(i)).getInt("Completion") == 3) bossIndex = i;
            if (bossIndex < 0) throw new IllegalArgumentException("Màn đã chọn không có boss; chọn M4 hoặc M8");
            copy.encounter = bossIndex;
        } else copy.encounter = Math.max(0, Math.min(encounter, count - 1));
        copy.lives = 5; copy.missiles = 3; copy.weaponLevels = new int[]{level, level, level}; copy.activeWeapon = weapon;
        copy.score = copy.shard = copy.encounterShardHighWater = 0;
        copy.missionCleared = copy.completed = copy.regionReady = false;
        CampaignStore.resetMissionStats(copy);
    }
}
