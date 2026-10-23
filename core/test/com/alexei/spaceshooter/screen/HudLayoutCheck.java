package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;

public final class HudLayoutCheck {
    public static void main(String[] args) {
        CampaignCatalog catalog = new CampaignCatalog(new FileHandle(args[0]));
        JsonValue scene = catalog.record("Scenes/02_Gameplay.unity");
        JsonValue hud = catalog.component(scene, "GameplayHud");
        for (float width : new float[]{1280, 1600}) {
            Rectangle weapon = CampaignUi.hudBounds(scene, hud.get("weaponButton"), width, 720);
            Rectangle missile = CampaignUi.hudBounds(scene, hud.get("missileButton"), width, 720);
            assert Math.abs(width - weapon.x - weapon.width - 6.4f) < .01f : "Weapon should sit 8 source pixels from edge";
            assert Math.abs(missile.x - weapon.x) < .01f;
            assert Math.abs(missile.y - weapon.y - 86.4f) < .01f : "Serialized vertical spacing";
            Rectangle score = CampaignUi.hudBounds(scene, hud.get("scoreLabel"), width, 720);
            Rectangle shard = CampaignUi.hudBounds(scene, hud.get("shardLabel"), width, 720);
            assert Math.abs(shard.x - score.x - 94.4f) < .01f : "Counters use parent 590px width, not full screen";
        }
        System.out.println("HUD: source hierarchy / right edge / vertical spacing at 16:9 and 20:9 OK");
    }
}
