package com.alexei.spaceshooter.manager;

/** Sweep tương đối giữa tia và player, chỉ nối lịch sử trong cùng active window. */
public final class BeamContact {
    private float previousX, previousY, previousPlayerX, previousPlayerY;
    private boolean continuous;

    public void reset() { continuous = false; }

    public boolean hit(float ax, float ay, float bx, float by, float width, float px, float py, float radius) {
        float cx = (ax + bx) * .5f, cy = (ay + by) * .5f;
        float dx = bx - ax, dy = by - ay;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        float cos = length > .000001f ? dx / length : 1, sin = length > .000001f ? dy / length : 0;
        float toX = px - cx, toY = py - cy;
        float fromX = continuous ? previousPlayerX - previousX : toX;
        float fromY = continuous ? previousPlayerY - previousY : toY;
        boolean hit = crosses(fromX * cos + fromY * sin, -fromX * sin + fromY * cos,
                toX * cos + toY * sin, -toX * sin + toY * cos, length * .5f + radius, width * .5f + radius);
        previousX = cx; previousY = cy; previousPlayerX = px; previousPlayerY = py; continuous = true;
        return hit;
    }

    private static boolean crosses(float ax, float ay, float bx, float by, float hw, float hh) {
        float enter = 0, exit = 1;
        float dx = bx - ax, dy = by - ay;
        if (Math.abs(dx) < .000001f) { if (Math.abs(ax) > hw) return false; }
        else {
            float a = (-hw - ax) / dx, b = (hw - ax) / dx;
            enter = Math.max(enter, Math.min(a, b)); exit = Math.min(exit, Math.max(a, b));
        }
        if (Math.abs(dy) < .000001f) { if (Math.abs(ay) > hh) return false; }
        else {
            float a = (-hh - ay) / dy, b = (hh - ay) / dy;
            enter = Math.max(enter, Math.min(a, b)); exit = Math.min(exit, Math.max(a, b));
        }
        return enter <= exit;
    }
}
