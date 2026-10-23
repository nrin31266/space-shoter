package com.alexei.spaceshooter.screen;

import com.alexei.spaceshooter.manager.CampaignAssets;
import com.alexei.spaceshooter.manager.CampaignWorld;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.JsonValue;

/** Preview dùng cùng sockets, plume và nhịp animation của tàu trong catalog. */
final class ShipPreview extends Actor {
    private final TextureRegion hull;
    private final TextureRegion[] engines, ribbons;
    private final JsonValue ship;
    private final boolean showRibbon;
    private float age;

    ShipPreview(CampaignAssets assets, JsonValue ship, String skin, JsonValue trail) {
        this.ship = ship;
        hull = assets.sprite(ship.get(skin.equals("skin-amber") ? "AmberSprite" : "Sprite")).region;
        engines = regions(assets, assets.catalog.asset("Data/Cosmetics/ShipCosmetics.asset").get("EngineFrames"));
        ribbons = regions(assets, trail.get("AnimationFrames"));
        showRibbon = trail.getFloat("TrailSeconds") > 0;
    }

    private static TextureRegion[] regions(CampaignAssets assets, JsonValue references) {
        String[] keys = CampaignWorld.frames(references);
        TextureRegion[] result = new TextureRegion[keys.length];
        for (int i = 0; i < keys.length; i++) result[i] = assets.sprite(keys[i]).region;
        return result;
    }

    @Override public void act(float delta) { super.act(delta); age += delta; }

    @Override public void draw(Batch batch, float parentAlpha) {
        float fit = Math.min(getWidth() / hull.getRegionWidth(), getHeight() / hull.getRegionHeight());
        float bobX = MathUtils.sin(age * .9f) * 2f;
        float bobY = MathUtils.sin(age * 1.2f) * 2f;
        float wobbleRot = MathUtils.sin(age * 1.1f) * 1.4f;
        float cx = getX() + getWidth() / 2 + bobX, cy = getY() + getHeight() / 2 + bobY;
        batch.setColor(1, 1, 1, parentAlpha);
        float tail = Float.POSITIVE_INFINITY;
        int i = 0;
        for (JsonValue socket : ship.get("EngineSockets")) {
            tail = Math.min(tail, socket.getFloat("y"));
            if (engines.length > 0) {
                float plume = ship.getFloat("PlumeScale") * fit;
                batch.draw(engines[(int)(age / .075f + i) % engines.length],
                        cx + socket.getFloat("x") * 56.25f * fit - 16 * plume,
                        cy + socket.getFloat("y") * 56.25f * fit - 40 * plume, 32 * plume, 40 * plume);
            }
            i++;
        }
        if (showRibbon && ribbons.length > 0) {
            batch.setColor(1, 1, 1, parentAlpha * .9f);
            batch.draw(ribbons[(int)(age / .1f) % ribbons.length],
                    cx + ship.get("MovementTrailSocket").getFloat("x") * 56.25f * fit - 2.5f * fit,
                    cy + (tail * 56.25f - 30) * fit, 5 * fit, 28 * fit);
        }
        batch.setColor(1, 1, 1, parentAlpha);
        float w = hull.getRegionWidth() * fit, h = hull.getRegionHeight() * fit;
        batch.draw(hull, cx - w / 2, cy - h / 2, w / 2, h / 2, w, h, 1, 1, wobbleRot);
    }
}
