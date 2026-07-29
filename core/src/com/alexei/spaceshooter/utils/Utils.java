package com.alexei.spaceshooter.utils;

/**
 * Created by Alex on 30/06/2015.
 */
public class Utils {
    /**
     * Given an angle in degrees, returns angle in range of -pi/2 to pi/2
     * @return
     */
    public static float normalizeAngle(float angle) {
        float norm =  angle % 360;
        return norm > 180 ? norm-360 : norm;
    }

    /**
     * Given an angle in degrees from -pi/2 to pi/2, returns angle in range of 0-360
     * @param angle
     * @return
     */
    public static float normalizeAngle360(float angle) {
        if (angle >= 0 && angle <= 360) return angle;
        if (angle > 180 || angle < -180) {
            angle = normalizeAngle(angle);
        }

        if (angle < 0) return angle + 360;
        return angle;
    }
}
