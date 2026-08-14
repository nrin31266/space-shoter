package com.alexei.spaceshooter.entity;

import com.alexei.spaceshooter.SpaceShooter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

/**
 * Created by Alex on 16/06/2015.
 * An abstract object which represents any visual element in the game, otherwise something that could be drawn.
 */
public class Visual {
    private static ArrayList<Visual> visuals = new ArrayList<Visual>();

    protected Vector2 position;
    protected Vector2 size;

    private float dir = 0; // specifies the direction angle in which the velocity points
    private float orientation = 0; // specifies the orientation angle of the visual
    private boolean orientInDirectionOfVelocity = true; // when false, orientation angle is specified by 'orientation' parameter
    private float speed = 100; // the magnitude of the velocity. Units are in dpi/second. Dpi refers to the Android 'density independent pixel' units.
    private Vector2 velocity = new Vector2(0,0); // velocity delta which increments the position of the visual by the x and y component values on each render call.
    private Color color = new Color(Color.MAGENTA);
    private TextureRegion region = null;

    public Visual(float x, float y, float width, float height) {
        position = new Vector2(x,y);
        size = new Vector2(width,height);
    }

    /***
     * The base method simply renders the bounding box and fill color of this visual
     * @param sr
     */
    public void render(ShapeRenderer sr, SpriteBatch batch) {
        if (region != null && batch != null && batch.isDrawing()) {
            batch.draw(region,
                    position.x, position.y,          // position
                    size.x * 0.5f, size.y * 0.5f,    // origin (center)
                    size.x, size.y,                   // size
                    1f, 1f,                            // scale
                    orientInDirectionOfVelocity ? dir : orientation); // rotation
        } else if (region == null && sr != null && sr.isDrawing()) {
            sr.setColor(color);
            if (orientInDirectionOfVelocity) {
                sr.rect(position.x, position.y, size.x * 0.5f, size.y * 0.5f, size.x, size.y, 1, 1, dir);
            } else {
                sr.rect(position.x, position.y, size.x * 0.5f, size.y * 0.5f, size.x, size.y, 1, 1, orientation);
            }
        }
    }

    /***
     * Make sure to call super.update() when overriding this method if you want to update the Visual's position
     */
    public void update(float deltaTime) {
        updatePosition(deltaTime);
    }

    /***
     * Increments the position of the visual by the velocity delta. Intended to be called on each render call.
     * Zero-allocation: avoids velocity.cpy() garbage by computing inline.
     */
    private void updatePosition(float deltaTime) {
        float scale = 1f;
        // when the app is backgrounded, for a moment, value of Gdx.graphics.getDeltaTime() is zero
        // causing a NaN if we divide it, so we guard against it here
        float curDelta = Gdx.graphics.getDeltaTime();
        if (curDelta > 0) {
            scale = deltaTime / (curDelta * 1000);
        }
        // Direct component math - avoids Vector2 allocation every frame
        position.x += velocity.x * scale;
        position.y += velocity.y * scale;
    }

    /**
     * All game objects call this method whenever they produce any kind of visual effect, such as sparks, explosions, etc.
     * The Visual class maintains a class variable which holds all such effects. They are rendered by the main game loop
     * and are automatically removed after expiring.
     * @param effects
     */
    public static void addVisualEffects(ArrayList<Visual> effects) {
        if (visuals == null) return;
        Visual.visuals.addAll(effects);
    }

    /** Add a single effect to the shared list (zero extra allocation). */
    public static void addVisualEffect(Visual effect) {
        if (visuals == null || effect == null) return;
        Visual.visuals.add(effect);
    }

    public static ArrayList<Visual> getVisualEffects() { return Visual.visuals; }

    /**
     * Set the shared visual effects list. Called by GamePlayScreen to use its own list
     * instead of the default static one.
     */
    public static void setVisualEffectsList(ArrayList<Visual> list) {
        visuals = list;
    }

    public boolean isDead() {
        return false;
    }

    /**
     * Check if this Visual is overlapping with the given visual. This is a simple AABB collision test.
     * @param visual
     * @return
     */
    public boolean isColliding(Visual visual) {
        return !isNotColliding(visual);
        //return (getX() + getWidth() > visual.getX() && getX() < visual.getX() + visual.getWidth() && getY() + getHeight() > visual.getY() && getY() < visual.getY() + visual.getHeight());
    }

    public boolean isNotColliding(Visual visual) {
        return (position.x > visual.getRight() || position.x+size.x < visual.getX() || position.y > visual.getTop() || position.y+size.y < visual.getY());
        //return (getX() + getWidth() > visual.getX() && getX() < visual.getX() + visual.getWidth() && getY() + getHeight() > visual.getY() && getY() < visual.getY() + visual.getHeight());
    }

    public boolean isPointInside(float x, float y) {
        return (x >= position.x && x <= position.x + size.x
        && y >= position.y && y <= position.y + size.y);
    }

    public boolean isPointInside(Vector2 point) {
        return isPointInside(point.x, point.y);
    }

    public Vector2 vectorTo(Visual visual) {
        return visual.position.cpy().sub(position);
    }
    /** NOTE: Returns a new Vector2. Prefer squareDistanceToCenter() when only distance is needed. */
    public Vector2 vectorToCenter(Visual visual) {
        return new Vector2(visual.getCenterX()-getCenterX(),visual.getCenterY()-getCenterY());
    }

    public float squareDistanceToCenter(Visual visual) {
        // Zero-allocation: avoid new Vector2() per item-magnetization check
        float dx = visual.getCenterX() - getCenterX();
        float dy = visual.getCenterY() - getCenterY();
        return dx * dx + dy * dy;
    }

    // getters/setters
    public void setVelocityVector(float dx, float dy) {
        velocity.set(dx, dy);
    }
    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public Vector2 getVelocity() {
        return velocity;
    }
    public void setVelocity(float dir, float speed) {
        this.speed = speed;
        this.dir = dir;
        velocity.set(MathUtils.cosDeg(dir) * speed / SpaceShooter.FPS, MathUtils.sinDeg(dir) * speed / SpaceShooter.FPS);
    }
    public void setVelocity(Vector2 velocity) {
        this.velocity.set(velocity);
    }
    public float getSpeed() {
        return speed;
    }
    public void setSpeed(float speed) {
        this.setVelocity(dir, speed);
    }
    public void setDirection(float dir) {
        this.setVelocity(dir, speed);
    }
    public void setDirection(Visual visual) {
        float dir = MathUtils.radiansToDegrees * MathUtils.atan2(visual.getCenterY() - this.getCenterY(), visual.getCenterX() - this.getCenterX());
        this.setVelocity(dir, speed);
    }
    public float getDirection() {
        return dir;
    }
    public float getY() {
        return position.y;
    }
    public void setY(float y) {
        position.y = y;
    }
    public float getX() {
        return position.x;
    }
    public void setX(float x) {
        position.x = x;
    }
    public float getWidth() {
        return size.x;
    }
    public void setWidth(float width) {
        size.x = width;
    }
    public float getHeight() {
        return size.y;
    }
    public void setHeight(float height) {
        size.y = height;
    }
    public float getCenterX() { return position.x+size.x/2; }
    public float getCenterY() { return position.y+size.y/2; }
    public float getRight() { return position.x+size.x; }
    public float getTop() { return position.y+size.y; }
    public TextureRegion getTextureRegion() {
        return region;
    }
    public void setTextureRegion(Texture texture) {
        this.region = new TextureRegion(texture, 0, 0, texture.getWidth(), texture.getHeight());
    }
    public void setTextureRegion(TextureRegion textureRegion) {
        this.region = textureRegion;
    }
    public boolean isOrientInDirectionOfVelocity() {
        return orientInDirectionOfVelocity;
    }

    public void setOrientInDirectionOfVelocity(boolean orientInDirectionOfVelocity) {
        this.orientInDirectionOfVelocity = orientInDirectionOfVelocity;
    }

    public float getOrientation() {
        return orientation;
    }

    /**
     * Rotation angle in degrees around this object's center.
     */
    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }
}
