package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.Locale;

public final class HazardSchedule {

    private HazardSchedule() {
    }

    public static Array<JsonValue> build(JsonValue config, float duration) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        if (!Float.isFinite(duration) || duration <= 0f) {
            throw new IllegalArgumentException("duration must be positive and finite: " + duration);
        }

        int count = getInt(config, 22, "Count", "count");
        if (count <= 0 || count > 4096) {
            throw new IllegalArgumentException("Count must be in range [1, 4096]: " + count);
        }

        float[] dominantDirection = getVec2(config, 0.42f, -1f, "DominantDirection", "dominantDirection");
        float domX = dominantDirection[0];
        float domY = dominantDirection[1];
        if (!Float.isFinite(domX) || !Float.isFinite(domY) || domY >= -0.1f) {
            throw new IllegalArgumentException("DominantDirection must be finite with y < -0.1f: (" + domX + ", " + domY + ")");
        }

        float domLen = (float) Math.hypot(domX, domY);
        if (domLen < 1e-6f || !Float.isFinite(domLen)) {
            throw new IllegalArgumentException("DominantDirection magnitude is invalid");
        }
        float normX = domX / domLen;
        float normY = domY / domLen;

        float angularVariance = getFloat(config, 3f, "AngularVariance", "angularVariance");
        if (!Float.isFinite(angularVariance) || angularVariance < 0f || angularVariance > 8f) {
            throw new IllegalArgumentException("AngularVariance must be finite and within [0, 8]: " + angularVariance);
        }

        float[] interval = getVec2(config, 0.5f, 0.9f, "Interval", "interval");
        if (!Float.isFinite(interval[0]) || !Float.isFinite(interval[1]) || interval[0] <= 0f || interval[1] < interval[0]) {
            throw new IllegalArgumentException("Interval must be positive finite with y >= x");
        }

        float[] scaleRange = getVec2(config, 0.65f, 1.4f, "Scale", "scale");
        if (!Float.isFinite(scaleRange[0]) || !Float.isFinite(scaleRange[1]) || scaleRange[0] <= 0f || scaleRange[1] < scaleRange[0]) {
            throw new IllegalArgumentException("Scale must be positive finite with y >= x");
        }

        float[] transitSeconds = getVec2(config, 2.8f, 4.2f, "TransitSeconds", "transitSeconds");
        if (!Float.isFinite(transitSeconds[0]) || !Float.isFinite(transitSeconds[1]) || transitSeconds[0] <= 0f || transitSeconds[1] < transitSeconds[0]) {
            throw new IllegalArgumentException("TransitSeconds must be positive finite with y >= x");
        }

        JsonValue variantsNode = find(config, "Variants", "variants");
        Array<JsonValue> variants = new Array<JsonValue>();
        if (variantsNode != null) {
            for (JsonValue v = variantsNode.child; v != null; v = v.next) {
                float vScale = getFloat(v, 1f, "Scale", "scale");
                int vHp = getInt(v, 4, "Hp", "hp");
                float vMult = getFloat(v, 1f, "TransitMultiplier", "transitMultiplier");
                if (!Float.isFinite(vScale) || vScale <= 0f || vHp <= 0 || !Float.isFinite(vMult) || vMult <= 0f) {
                    throw new IllegalArgumentException("AsteroidVariant fields must be positive and finite");
                }
                variants.add(v);
            }
        }

        JsonValue prefabRef = find(config, "Prefab", "EnemyPrefab", "prefab", "enemyPrefab");
        if (prefabRef == null || !prefabRef.has("guid")) throw new IllegalArgumentException("Missing hazard prefab");
        String prefabJson = prefabRef.toJson(JsonWriter.OutputType.json);

        int seed = getInt(config, 521, "Seed", "seed");
        RewardRules.SourceRandom random = new RewardRules.SourceRandom(seed);

        JsonReader reader = new JsonReader();
        Array<JsonValue> result = new Array<JsonValue>(count);
        float time = 0.2f;

        while (result.size < count && time < duration - 0.7f) {
            int burst = 1 + random.nextInt(2);
            for (int i = 0; i < burst && result.size < count; i++) {
                boolean right = domX >= 0f;
                float x = lerp(right ? -0.12f : 0.25f, right ? 0.75f : 1.12f, (float) random.nextDouble());
                float deviation = lerp(-angularVariance, angularVariance, (float) random.nextDouble());

                float rad = (float) Math.toRadians(deviation);
                float cos = (float) Math.cos(rad);
                float sin = (float) Math.sin(rad);
                float dirX = normX * cos - normY * sin;
                float dirY = normX * sin + normY * cos;
                if (dirY >= 0) throw new IllegalArgumentException("Hazard direction must stay downward");

                float fromX = x;
                float fromY = 1.15f;
                float entryFactor = 0.17f / -dirY;
                float entryX = fromX + dirX * entryFactor;
                float entryY = fromY + dirY * entryFactor;
                float exitFactor = 1.18f / -dirY;
                float exitX = entryX + dirX * exitFactor;
                float exitY = entryY + dirY * exitFactor;

                float scale;
                int hpOverride;
                float exitDuration;

                if (variants.size > 0) {
                    JsonValue variant = variants.get(random.nextInt(variants.size));
                    scale = getFloat(variant, 1f, "Scale", "scale");
                    hpOverride = getInt(variant, 4, "Hp", "hp");
                    float mult = getFloat(variant, 1f, "TransitMultiplier", "transitMultiplier");
                    exitDuration = transitSeconds[0] * mult;
                } else {
                    scale = lerp(scaleRange[0], scaleRange[1], (float) random.nextDouble());
                    hpOverride = 0;
                    float weight = inverseLerp(scaleRange[0], scaleRange[1], scale);
                    exitDuration = lerp(transitSeconds[0], transitSeconds[1], weight);
                }

                float visualSpin = lerp(-70f, 70f, (float) random.nextDouble());
                float startTime = time + i * 0.12f;

                String actionJson = String.format(Locale.ROOT,
                    "{\"Count\":1,\"StartTime\":%.6f,\"Required\":0,\"GrantsRewards\":1,\"EnemyPrefab\":%s,"
                    + "\"EnemyDefinition\":{\"fileID\":\"0\"},\"RewardOverride\":{\"fileID\":\"0\"},"
                    + "\"Elite\":{\"fileID\":\"0\"},\"Sequence\":{\"fileID\":\"0\"},\"Plan\":{\"fileID\":\"0\"},"
                    + "\"EntryBeat\":0,\"Stagger\":0,\"Formation\":0,\"Anchor\":{\"x\":0.0,\"y\":0.0},"
                    + "\"SlotStep\":{\"x\":0.0,\"y\":0.0},\"FormationRise\":0,\"FirstSlot\":0,\"ContinuousTransit\":1,"
                    + "\"SpawnScale\":%.6f,\"HpOverride\":%d,\"VisualSpin\":%.6f,"
                    + "\"EntryAnchor\":{\"x\":%.6f,\"y\":%.6f},\"HoldAnchor\":{\"x\":%.6f,\"y\":%.6f},"
                    + "\"ExitAnchor\":{\"x\":%.6f,\"y\":%.6f},\"EntryCurve\":{\"x\":0.0,\"y\":0.0},"
                    + "\"EntryDuration\":0.65,\"HoldDuration\":0.0,\"ExitDuration\":%.6f}",
                    startTime, prefabJson, scale, hpOverride, visualSpin,
                    fromX, fromY, entryX, entryY, exitX, exitY, exitDuration);

                result.add(reader.parse(actionJson));
            }
            time += lerp(interval[0], interval[1], (float) random.nextDouble());
        }

        return result;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float inverseLerp(float a, float b, float value) {
        if (a != b) {
            float t = (value - a) / (b - a);
            if (t < 0f) return 0f;
            if (t > 1f) return 1f;
            return t;
        }
        return 0f;
    }

    private static JsonValue find(JsonValue root, String... names) {
        if (root == null) return null;
        for (String name : names) {
            JsonValue val = root.get(name);
            if (val != null) return val;
        }
        return null;
    }

    private static float getFloat(JsonValue root, float ignored, String... names) {
        return root.getFloat(names[0]);
    }

    private static int getInt(JsonValue root, int ignored, String... names) {
        return root.getInt(names[0]);
    }

    private static float[] getVec2(JsonValue root, float ignoredX, float ignoredY, String... names) {
        JsonValue node = root.get(names[0]);
        if (node == null || !node.isObject()) throw new IllegalArgumentException("Missing vector " + names[0]);
        return new float[]{node.getFloat("x"), node.getFloat("y")};
    }
}
