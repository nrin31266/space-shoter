package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public final class FlightMathCheck {
    public static void main(String[] args) {
        JsonReader reader = new JsonReader();
        Vector2 out = new Vector2();
        JsonValue phase = reader.parse("{Path:3,Smooth:0,Curve:{x:0.2,y:0.1},Cycles:1}");
        FlightMath.evaluate(phase, .5f, 0, 0, 0, 1, out);
        assert out.epsilonEquals(.2f, .6f, .0001f) : out;
        FlightMath.evaluate(phase, 2, 0, 0, 0, 1, out);
        assert out.epsilonEquals(0, 1, .0001f);
        JsonValue group = reader.parse("{Count:2,FirstSlot:1}");
        JsonValue plan = reader.parse("{Slots:[{x:0,y:0},{x:0.3,y:-0.1},{x:0.5,y:0.2}]}");
        FlightMath.slot(group, plan, 0, out);
        assert out.epsilonEquals(.3f, -.1f, .0001f);
        boolean rejected = false;
        try { FlightMath.slot(group, plan, 2, out); } catch (IllegalArgumentException expected) { rejected = true; }
        assert rejected : "Invalid slot must not silently clamp";
        FlightMath.orient(.2f, .8f, 1, out);
        assert out.epsilonEquals(.8f, .8f, .0001f);
        System.out.println("Flight curves, numeric enums, slot bounds, direction: OK");
    }
}
