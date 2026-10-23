package com.alexei.spaceshooter.data;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.math.MathUtils;

/** Catalog offline; runtime chỉ đọc JSON, không cần Unity hoặc repository nguồn. */
public final class CampaignCatalog {
    public final JsonValue root;
    public final JsonValue[] missions = new JsonValue[8];
    private final ObjectMap<String, JsonValue> byPath = new ObjectMap<>();

    public CampaignCatalog(FileHandle file) {
        root = new JsonReader().parse(file);
        if (root.getInt("schema") != 1) throw new IllegalArgumentException("Unsupported campaign catalog");
        for (JsonValue record : root.get("records")) {
            String path = record.getString("path");
            byPath.put(path, record);
            if (path.matches(".*[/]Missions/Mission0[1-8]\\.asset")) {
                JsonValue mission = record.get("documents").get(0).get("data");
                missions[mission.getInt("MissionNumber") - 1] = mission;
            }
        }
        for (int i = 0; i < missions.length; i++)
            if (missions[i] == null) throw new IllegalArgumentException("Missing mission " + (i + 1));
    }

    public JsonValue record(String suffix) {
        for (ObjectMap.Entry<String, JsonValue> entry : byPath)
            if (entry.key.endsWith(suffix)) return entry.value;
        throw new IllegalArgumentException("Missing catalog record: " + suffix);
    }

    public JsonValue asset(String suffix) {
        return record(suffix).get("documents").get(0).get("data");
    }

    public JsonValue resolve(JsonValue reference) {
        if (reference == null || !reference.has("guid")) return null;
        JsonValue record = root.get("records").get(reference.getString("guid"));
        if (record == null) throw new IllegalArgumentException("Unresolved catalog GUID: " + reference);
        String id = reference.getString("fileID");
        for (JsonValue doc : record.get("documents"))
            if (doc.getString("fileID").equals(id)) return doc.get("data");
        throw new IllegalArgumentException("Unresolved catalog object: " + reference);
    }

    public JsonValue referencedRecord(JsonValue reference) {
        return root.get("records").get(reference.getString("guid"));
    }

    public JsonValue component(JsonValue record, String className) {
        for (JsonValue doc : record.get("documents")) {
            JsonValue data = doc.get("data");
            if (String.valueOf(data.getString("m_EditorClassIdentifier", "")).endsWith("." + className)) return data;
        }
        return null;
    }

    public JsonValue rootComponent(JsonValue record, String className) {
        String objectId = null;
        for (JsonValue doc : record.get("documents")) {
            JsonValue data = doc.get("data");
            if (doc.getString("type").equals("Transform") && data.get("m_Father").getString("fileID").equals("0"))
                objectId = data.get("m_GameObject").getString("fileID");
        }
        for (JsonValue doc : record.get("documents")) {
            JsonValue data = doc.get("data");
            if (String.valueOf(data.getString("m_EditorClassIdentifier", "")).endsWith("." + className) &&
                    data.get("m_GameObject").getString("fileID").equals(objectId)) return data;
        }
        return null;
    }

    public static final class Pose {
        public float x, y, rotation, sx = 1, sy = 1;
    }

    public static Pose pose(JsonValue record, String objectId) {
        ObjectMap<String, JsonValue> transforms = new ObjectMap<>();
        String id = null;
        for (JsonValue doc : record.get("documents")) if (doc.getString("type").equals("Transform")) {
            JsonValue d = doc.get("data");
            transforms.put(doc.getString("fileID"), d);
            if (d.get("m_GameObject").getString("fileID").equals(objectId)) id = doc.getString("fileID");
        }
        if (id == null) throw new IllegalArgumentException("Missing transform for " + objectId);
        Pose pose = new Pose();
        int depth = 0;
        while (!id.equals("0")) {
            if (++depth > transforms.size) throw new IllegalArgumentException("Cyclic transform hierarchy");
            JsonValue d = transforms.get(id);
            if (d == null) throw new IllegalArgumentException("Missing parent transform " + id);
            JsonValue p = d.get("m_LocalPosition"), s = d.get("m_LocalScale"), q = d.get("m_LocalRotation");
            float angle = 2 * MathUtils.atan2(q.getFloat("z"), q.getFloat("w")) * MathUtils.radiansToDegrees;
            float x = pose.x * s.getFloat("x"), y = pose.y * s.getFloat("y");
            pose.x = p.getFloat("x") + x * MathUtils.cosDeg(angle) - y * MathUtils.sinDeg(angle);
            pose.y = p.getFloat("y") + x * MathUtils.sinDeg(angle) + y * MathUtils.cosDeg(angle);
            pose.sx *= s.getFloat("x"); pose.sy *= s.getFloat("y"); pose.rotation += angle;
            id = d.get("m_Father").getString("fileID");
        }
        return pose;
    }

    public Array<JsonValue> components(JsonValue record, String className) {
        Array<JsonValue> result = new Array<>();
        for (JsonValue doc : record.get("documents")) {
            JsonValue data = doc.get("data");
            if (String.valueOf(data.getString("m_EditorClassIdentifier", "")).endsWith("." + className)) result.add(data);
        }
        return result;
    }

    public static String spriteKey(JsonValue reference) {
        return reference.getString("guid") + ":" + reference.getString("fileID");
    }
}
