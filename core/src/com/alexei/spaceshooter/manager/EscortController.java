package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.CampaignCatalog;
import com.alexei.spaceshooter.manager.CampaignWorld.Enemy;
import com.alexei.spaceshooter.manager.CampaignWorld.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

public class EscortController {
    public final JsonValue component;
    public final JsonValue escortPrefab;
    public final JsonValue escortFlight;
    public final int capacity;
    public final float intervalSeconds;
    public final boolean automatic;

    private final CampaignWorld world;
    private final Enemy owner;
    private final Array<Enemy> spawned = new Array<Enemy>();
    private float remaining;
    private int ordinal;

    public EscortController(CampaignWorld world, CampaignWorld.Enemy owner, JsonValue component) {
        if (world == null || owner == null || component == null) {
            throw new IllegalArgumentException("world, owner, and component cannot be null");
        }
        this.world = world;
        this.owner = owner;
        this.component = component;

        this.escortPrefab = component.get("escortPrefab");
        this.escortFlight = component.get("escortFlight");
        if (this.escortPrefab == null || this.escortFlight == null) {
            throw new IllegalArgumentException("escortPrefab and escortFlight references are required");
        }

        this.capacity = component.has("capacity") ? component.getInt("capacity") : 3;
        this.intervalSeconds = component.has("intervalSeconds") ? component.getFloat("intervalSeconds") : 5f;
        this.automatic = !component.has("automatic") || component.getBoolean("automatic");

        if (this.capacity < 1 || this.intervalSeconds <= 0f || Float.isNaN(this.intervalSeconds) || Float.isInfinite(this.intervalSeconds)) {
            throw new IllegalArgumentException("capacity and intervalSeconds must be positive and finite");
        }
        this.remaining = this.intervalSeconds;
    }

    public int liveCount() {
        for (int i = spawned.size - 1; i >= 0; i--) {
            Enemy enemy = spawned.get(i);
            if (enemy == null || enemy.hp <= 0 || !world.enemies.contains(enemy, true)) {
                spawned.removeIndex(i);
            }
        }
        return spawned.size;
    }

    public boolean spawn() {
        if (owner.hp <= 0 || !owner.holding || !owner.canShoot) return false;
        if (world.stage != Stage.RUNNING || !world.player.canInput() || world.player.enemyFireGrace > 0) return false;
        if (liveCount() >= capacity) return false;

        float tx = 0f;
        float ty = 0f;
        JsonValue go = component.get("m_GameObject");
        if (go != null && owner.spec != null && owner.spec.record != null) {
            String fileId = go.getString("fileID", null);
            if (fileId != null) {
                CampaignCatalog.Pose pose = CampaignCatalog.pose(owner.spec.record, fileId);
                if (pose != null) {
                    tx = (world.direction() == 1) ? pose.y : pose.x;
                    ty = (world.direction() == 1) ? -pose.x : pose.y;
                    tx *= owner.scale;
                    ty *= owner.scale;
                }
            }
        }

        float spawnX = owner.x + tx;
        float spawnY = owner.y + ty;
        float slotOffset = (ordinal++ % capacity - (capacity - 1) * 0.5f) * 0.15f;

        Enemy escort = world.spawnEscort(escortPrefab, escortFlight, spawnX, spawnY, slotOffset);
        if (escort == null) return false;
        spawned.add(escort);
        return true;
    }

    public void tick(float seconds) {
        if (!automatic || owner.hp <= 0 || !owner.holding || !owner.canShoot) return;
        if (world.stage != Stage.RUNNING || !world.player.canInput() || world.player.enemyFireGrace > 0) return;

        remaining -= seconds;
        if (remaining > 0f) return;
        remaining = intervalSeconds;
        spawn();
    }

    public void cancel() {
        for (int i = 0; i < spawned.size; i++) {
            Enemy enemy = spawned.get(i);
            if (enemy != null && enemy.hp > 0 && world.enemies.contains(enemy, true)) {
                world.retreat(enemy);
            }
        }
        spawned.clear();
    }
}
