package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public final class HazardScheduleCheck {
    public static void main(String[] args) {
        JsonReader reader = new JsonReader();
        String base = "{Prefab:{guid:asteroid,fileID:1},Seed:521,DominantDirection:{x:0.42,y:-1},AngularVariance:3,Count:22," +
                "Interval:{x:0.5,y:0.9},Scale:{x:0.65,y:1.4},TransitSeconds:{x:2.8,y:4.2},Variants:[]}";
        Array<JsonValue> a = HazardSchedule.build(reader.parse(base), 30);
        Array<JsonValue> b = HazardSchedule.build(reader.parse(base), 30);
        assert a.size == 22 && b.size == a.size;
        for (int i = 0; i < a.size; i++) {
            assert a.get(i).toJson(JsonWriter.OutputType.json).equals(b.get(i).toJson(JsonWriter.OutputType.json));
            assert a.get(i).getInt("Required") == 0 && a.get(i).getInt("Count") == 1;
        }
        JsonValue variant = reader.parse(base);
        variant.remove("Variants");
        variant.addChild("Variants", reader.parse("[{Scale:1.5,Hp:10,TransitMultiplier:0.5}]"));
        for (JsonValue group : HazardSchedule.build(variant, 30))
            assert group.getInt("HpOverride") == 10 && Math.abs(group.getFloat("ExitDuration") - 1.4f) < .0001f;
        assert HazardSchedule.build(reader.parse(base), .7f).size == 0;
        JsonValue invalid = reader.parse(base);
        invalid.remove("DominantDirection"); invalid.addChild("DominantDirection", reader.parse("{x:1,y:0}"));
        boolean rejected = false;
        try { HazardSchedule.build(invalid, 30); } catch (IllegalArgumentException expected) { rejected = true; }
        assert rejected : "Invalid direction must not enter schedule loop";
        System.out.println("Hazard seed, cutoff, variants, invalid direction: OK");
    }
}
