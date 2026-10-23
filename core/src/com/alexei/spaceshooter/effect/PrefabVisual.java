package com.alexei.spaceshooter.effect;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.alexei.spaceshooter.manager.CampaignAssets;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/** Pose đã export, tính hierarchy một lần; không giữ GameObject/component runtime. */
public final class PrefabVisual {
    private static final class Layer {
        CampaignAssets.Sprite sprite;
        CampaignAssets.Sprite[] frames;
        float frameSeconds, x, y, sx, sy, rotation;
        int order;
        final Color color = new Color();
        boolean hull;
        boolean visible = true;
        float healthOverride = Float.NaN;
        final Array<String> ancestors = new Array<>();
        CampaignAssets.Sprite damaged, critical;
        float damagedAt = .5f, criticalAt;
    }
    private final Array<Layer> layers = new Array<>();
    private CampaignAssets.Sprite damaged, critical;
    private float damagedAt = .5f, criticalAt;

    public PrefabVisual(CampaignAssets assets, JsonValue record) {
        CampaignCatalog catalog = assets.catalog;
        ObjectMap<String, JsonValue> objects = new ObjectMap<>();
        ObjectMap<String, JsonValue> transforms = new ObjectMap<>();
        ObjectMap<String, String> objectTransform = new ObjectMap<>();
        ObjectMap<String, Boolean> transientRenderers = new ObjectMap<>();
        for (JsonValue doc : record.get("documents")) {
            String type = doc.getString("type"), id = doc.getString("fileID");
            JsonValue d = doc.get("data");
            if (type.equals("GameObject")) objects.put(id, d);
            if (type.equals("Transform")) {
                transforms.put(id, d);
                objectTransform.put(d.get("m_GameObject").getString("fileID"), id);
            }
        }
        String[][] owned = {{"PlayerEffectPresentation", "shield"}, {"PlayerCombatPresentation", "muzzle", "reward", "survival"},
                {"PlayerDeathPresentation", "explosion", "fragments"}, {"EnemyFirePresentation", "chargeCue"},
                {"EnemyHitPresentation", "impact", "sparks", "smoke"},
                {"DirectedAttackTelegraph", "warning", "launch"}, {"BossSkillPhasePresentation", "cue"},
                {"BossBeamPresentation", "column", "core", "sweepArrow"}, {"BossPhaseRunner", "phaseCue"},
                {"BossShieldNetwork", "shield", "breaks"}, {"BossArmorBreakPresentation", "fragments", "bursts"},
                {"BossDeathSequence", "explosions"}, {"StagedDestructionPresentation", "fragments"}};
        for (String[] owner : owned) {
            for (JsonValue component : catalog.components(record, owner[0]))
                for (int i = 1; i < owner.length; i++) if (component.has(owner[i])) {
                    JsonValue refs = component.get(owner[i]);
                    if (refs.isArray()) for (JsonValue ref : refs) transientRenderers.put(ref.getString("fileID"), true);
                    else transientRenderers.put(refs.getString("fileID"), true);
                }
        }
        for (JsonValue staged : catalog.components(record, "StagedDestructionPresentation"))
            for (JsonValue stage : staged.get("stages")) transientRenderers.put(stage.get("blast").getString("fileID"), true);
        JsonValue damage = catalog.rootComponent(record, "EnemyDamagePresentation");
        if (damage == null) damage = catalog.component(record, "PlayerDamagePresentation");
        String hull = damage == null || !damage.has("hull") ? "" : damage.get("hull").getString("fileID");
        if (damage != null) {
            damaged = optional(assets, damage.get("damagedSprite"));
            critical = optional(assets, damage.get("criticalSprite"));
            damagedAt = damage.getFloat("damagedHealthFraction", .5f);
            criticalAt = damage.getFloat("criticalHealthFraction", 0);
        }
        for (JsonValue doc : record.get("documents")) {
            if (!doc.getString("type").equals("SpriteRenderer")) continue;
            if (transientRenderers.containsKey(doc.getString("fileID"))) continue;
            JsonValue d = doc.get("data");
            if (d.getInt("m_Enabled") == 0 || !d.get("m_Sprite").has("guid")) continue;
            String objectId = d.get("m_GameObject").getString("fileID");
            String transformId = objectTransform.get(objectId);
            if (transformId == null) continue;
            Layer layer = new Layer();
            layer.sprite = assets.sprite(d.get("m_Sprite"));
            layer.sx = d.getInt("m_FlipX", 0) == 0 ? 1 : -1;
            layer.sy = d.getInt("m_FlipY", 0) == 0 ? 1 : -1;
            boolean visible = true;
            for (String id = transformId; id != null && !id.equals("0");) {
                JsonValue transform = transforms.get(id);
                if (transform == null) throw new IllegalArgumentException("Missing visual transform: " + id);
                JsonValue object = objects.get(transform.get("m_GameObject").getString("fileID"));
                layer.ancestors.add(transform.get("m_GameObject").getString("fileID"));
                if (object.getInt("m_IsActive") == 0) visible = false;
                JsonValue pos = transform.get("m_LocalPosition"), scale = transform.get("m_LocalScale"), q = transform.get("m_LocalRotation");
                float angle = 2 * MathUtils.atan2(q.getFloat("z"), q.getFloat("w")) * MathUtils.radiansToDegrees;
                float x = layer.x * scale.getFloat("x"), y = layer.y * scale.getFloat("y");
                layer.x = pos.getFloat("x") + x * MathUtils.cosDeg(angle) - y * MathUtils.sinDeg(angle);
                layer.y = pos.getFloat("y") + x * MathUtils.sinDeg(angle) + y * MathUtils.cosDeg(angle);
                layer.sx *= scale.getFloat("x"); layer.sy *= scale.getFloat("y");
                layer.rotation += angle;
                id = transform.get("m_Father").getString("fileID");
            }
            if (!visible) continue;
            JsonValue color = d.get("m_Color");
            layer.color.set(color.getFloat("r"), color.getFloat("g"), color.getFloat("b"), color.getFloat("a"));
            layer.order = d.getInt("m_SortingOrder");
            layer.hull = doc.getString("fileID").equals(hull);
            for (JsonValue presentation : catalog.components(record, "EnemyDamagePresentation")) {
                if (!presentation.get("hull").getString("fileID").equals(doc.getString("fileID"))) continue;
                layer.damaged = optional(assets, presentation.get("damagedSprite"));
                layer.critical = optional(assets, presentation.get("criticalSprite"));
                layer.damagedAt = presentation.getFloat("damagedHealthFraction", .5f);
                layer.criticalAt = presentation.getFloat("criticalHealthFraction", 0);
            }
            for (JsonValue loop : catalog.components(record, "SpriteFrameLoop")) {
                if (!loop.get("m_GameObject").getString("fileID").equals(objectId)) continue;
                JsonValue frames = loop.get("frames");
                if (frames == null || frames.size == 0) continue;
                layer.frames = new CampaignAssets.Sprite[frames.size];
                for (int i = 0; i < frames.size; i++) layer.frames[i] = assets.sprite(frames.get(i));
                layer.frameSeconds = loop.getFloat("frameSeconds");
            }
            layers.add(layer);
        }
        layers.sort((a, b) -> Integer.compare(a.order, b.order));
        if (layers.size == 0) throw new IllegalArgumentException("Empty visual: " + record.getString("path"));
    }

    private static CampaignAssets.Sprite optional(CampaignAssets assets, JsonValue ref) {
        return ref == null || !ref.has("guid") ? null : assets.sprite(ref);
    }

    public void draw(SpriteBatch batch, float x, float y, float scale, float rotation, float clock, float hpFraction, float alpha) {
        float cos = MathUtils.cosDeg(rotation), sin = MathUtils.sinDeg(rotation);
        for (Layer layer : layers) {
            if (!layer.visible) continue;
            CampaignAssets.Sprite sprite = layer.sprite;
            if (layer.frames != null) sprite = layer.frames[(int)(clock / layer.frameSeconds) % layer.frames.length];
            if (layer.hull && hpFraction < criticalAt && critical != null) sprite = critical;
            else if (layer.hull && hpFraction < damagedAt && damaged != null) sprite = damaged;
            float health = Float.isNaN(layer.healthOverride) ? hpFraction : layer.healthOverride;
            if (health < layer.criticalAt && layer.critical != null) sprite = layer.critical;
            else if (health < layer.damagedAt && layer.damaged != null) sprite = layer.damaged;
            float px = x + (layer.x * cos - layer.y * sin) * scale;
            float py = y + (layer.x * sin + layer.y * cos) * scale;
            batch.setColor(layer.color.r, layer.color.g, layer.color.b, layer.color.a * alpha);
            batch.draw(sprite.region, px - sprite.pivotX * scale, py - sprite.pivotY * scale,
                    sprite.pivotX * scale, sprite.pivotY * scale, sprite.width * scale, sprite.height * scale,
                    layer.sx, layer.sy, rotation + layer.rotation);
        }
        batch.setColor(Color.WHITE);
    }

    public void resetObjectHealth() {
        for (Layer layer : layers) { layer.visible = true; layer.healthOverride = Float.NaN; }
    }

    public void setObjectHealth(String objectId, float fraction) {
        for (Layer layer : layers) if (layer.ancestors.contains(objectId, false)) {
            layer.visible = fraction > 0;
            layer.healthOverride = fraction;
        }
    }
}
