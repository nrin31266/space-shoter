package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

public final class FlightMath {
    private FlightMath() {}

    public static Vector2 evaluate(JsonValue phase, float progress, float fromX, float fromY,
                                   float toX, float toY, Vector2 out) {
        if (!Float.isFinite(progress) || out == null) throw new IllegalArgumentException("Invalid flight evaluation");
        float t = MathUtils.clamp(progress, 0, 1);
        if (phase.getBoolean("Smooth")) t = t * t * (3 - 2 * t);
        float x = fromX + (toX - fromX) * t, y = fromY + (toY - fromY) * t;
        JsonValue curve = phase.get("Curve");
        float cx = curve.getFloat("x"), cy = curve.getFloat("y");
        float angle = t * MathUtils.PI2 * phase.getFloat("Cycles");
        switch (phase.getInt("Path")) {
            case 2: return out.set(x - Math.abs(cx) * 4 * t * (1 - t), y + cy * 4 * t * (1 - t));
            case 3: return out.set(x + Math.abs(cx) * 4 * t * (1 - t), y + cy * 4 * t * (1 - t));
            case 4:
                float sine = (float)(Math.sin(angle) * Math.sin(t * Math.PI));
                return out.set(x + cx * sine, y + cy * sine);
            case 5:
                float dive = (float)Math.sin(t * Math.PI);
                return out.set(x + cx * dive, y + cy * dive);
            case 6: case 9:
                return out.set(x + cx * (float)Math.sin(angle), y + cy * (1 - (float)Math.cos(angle)));
            case 8: case 10: return out.set(x + cx * 4 * t * (1 - t), y + cy * 4 * t * (1 - t));
            default: return out.set(x, y);
        }
    }

    public static Vector2 slot(JsonValue group, JsonValue plan, int index, Vector2 out) {
        int count = group.getInt("Count");
        if (count < 1 || index < 0 || index >= count || out == null)
            throw new IllegalArgumentException("Invalid formation slot " + index);
        if (plan != null) {
            JsonValue slots = plan.get("Slots");
            int first = group.getInt("FirstSlot");
            if (first < 0 || first + count > slots.size) throw new IllegalArgumentException("Formation plan too small");
            JsonValue point = slots.get(first + index);
            return out.set(point.getFloat("x"), point.getFloat("y"));
        }
        JsonValue step = group.get("SlotStep");
        float x = step.getFloat("x"), y = step.getFloat("y"), sx = Math.abs(x), sy = Math.abs(y);
        if (sx == 0) sx = sy;
        if (sy == 0) sy = sx;
        float slot = index - (count - 1) * .5f, rise = group.getFloat("FormationRise");
        switch (group.getInt("Formation")) {
            case 1: return out.set(slot * sx, 0);
            case 2: return out.set(0, slot * sy);
            case 3: return out.set(slot * sx, Math.abs(slot) * rise);
            case 4: return out.set(slot * sx, -Math.abs(slot) * rise);
            case 5: return out.set(slot * sx, ((index % 2) - .5f) * sy);
            case 6: return out.set((index % 2 == 0 ? -1 : 1) * sx * (1 + index / 2), index / 2 * sy);
            case 7:
                if (index == 0) return out.setZero();
                int arm = (index - 1) % 4, distance = 1 + (index - 1) / 4;
                return out.set((arm == 0 ? sx : arm == 1 ? -sx : 0) * distance,
                        (arm == 2 ? sy : arm == 3 ? -sy : 0) * distance);
            case 8:
                return index == 0 ? out.setZero() : out.set((index % 2 == 0 ? 1 : -1) * sx * ((index + 1) / 2), -rise * ((index + 1) / 2));
            case 9:
                float angle = index * MathUtils.PI2 / count;
                return out.set((float)Math.cos(angle) * sx, (float)Math.sin(angle) * sy);
            default: return out.set(x * slot, y * slot + Math.abs(slot) * rise);
        }
    }

    public static Vector2 orient(float x, float y, int direction, Vector2 out) {
        if (direction < 0 || direction > 1) throw new IllegalArgumentException("Unknown flight direction");
        return direction == 1 ? out.set(y, 1 - x) : out.set(x, y);
    }
}
