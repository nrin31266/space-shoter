package com.alexei.spaceshooter.data.wave;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * Root DTO that holds all wave definitions loaded from waves.json.
 * WaveConfig is the single source of truth for wave data.
 * Contains NO gameplay logic — pure data + loader.
 */
public class WaveConfig {
    public List<WaveData> waves = new ArrayList<>();

    public WaveConfig() {
    }

    /**
     * Load wave configuration from a JSON file path (relative to the assets folder).
     * Uses LibGDX's built-in JSON parser.
     *
     * @param internalPath Path relative to assets/ (e.g., "data/waves.json")
     * @return Parsed WaveConfig, or a fallback config if parsing fails.
     */
    public static WaveConfig loadFromFile(String internalPath) {
        try {
            Json json = new Json();
            String content = Gdx.files.internal(internalPath).readString();
            WaveConfig config = json.fromJson(WaveConfig.class, content);
            Gdx.app.log("[WaveConfig]", "Loaded " + config.waves.size() + " waves from " + internalPath);
            return config;
        } catch (Exception e) {
            Gdx.app.error("[WaveConfig]", "Failed to load " + internalPath + ": " + e.getMessage());
            return createFallback();
        }
    }

    /**
     * Fallback configuration used when waves.json is missing or corrupt.
     * Ensures game is always playable.
     */
    private static WaveConfig createFallback() {
        Gdx.app.log("[WaveConfig]", "Using fallback wave configuration");
        WaveConfig config = new WaveConfig();

        List<SpawnAction> actions1 = new ArrayList<>();
        actions1.add(new SpawnAction(1.0f, "EnemyShipA", "LINE", 5));
        actions1.add(new SpawnAction(6.0f, "EnemyShipB", "V_SHAPE", 3));
        config.waves.add(new WaveData(1, actions1));

        List<SpawnAction> actions2 = new ArrayList<>();
        actions2.add(new SpawnAction(1.0f, "EnemyShipA", "LINE", 6));
        actions2.add(new SpawnAction(4.0f, "EnemyShipB", "V_SHAPE", 4));
        actions2.add(new SpawnAction(8.0f, "EnemyShipA", "RANDOM", 3));
        config.waves.add(new WaveData(2, actions2));

        List<SpawnAction> actions3 = new ArrayList<>();
        actions3.add(new SpawnAction(1.0f, "EnemyShipB", "LINE", 4));
        actions3.add(new SpawnAction(4.0f, "EnemyShipA", "V_SHAPE", 5));
        actions3.add(new SpawnAction(8.0f, "EnemyShipB", "RANDOM", 4));
        config.waves.add(new WaveData(3, actions3));

        return config;
    }

    /**
     * Get a wave by its ID (1-based).
     * @return WaveData or null if not found.
     */
    public WaveData getWave(int waveId) {
        for (WaveData w : waves) {
            if (w.waveId == waveId) return w;
        }
        return null;
    }

    /**
     * @return true if there is a wave after the given waveId.
     */
    public boolean hasWave(int waveId) {
        return getWave(waveId) != null;
    }

    public int getWaveCount() {
        return waves.size();
    }
}
