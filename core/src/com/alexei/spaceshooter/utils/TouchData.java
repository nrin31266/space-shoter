package com.alexei.spaceshooter.utils;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Alex on 29/06/2015.
 */
public class TouchData {
    private static boolean isTouchUpRaised;
    private static boolean isTouchDownRaised;
    private static Vector2 position = new Vector2();

    public TouchData() {}

    public void set(float x, float y, boolean isTouchUp) {
        position.set(x, y);

        if (isTouchUp) isTouchUpRaised = true;
        else isTouchDownRaised = true;
    }
    public void handleTouchDown() { isTouchDownRaised = false; }
    public void handleTouchUp() { isTouchDownRaised = false; isTouchUpRaised = false; }

    public void handle() {
        isTouchUpRaised = false;
        isTouchDownRaised = false;
    }

    public boolean isTouchUpRaised() { return isTouchDownRaised && isTouchUpRaised; }
    public boolean isTouchDownRaised() { return isTouchDownRaised; }
}
