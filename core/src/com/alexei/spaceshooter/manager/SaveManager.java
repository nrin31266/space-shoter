package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Manages save/load of game progression using LibGDX Preferences.
 *
 * Responsibilities:
 * - Save game state (wave, score, life, stars)
 * - Load saved game state
 * - Clear save data
 * - Check if a saved game exists
 *
 * Does NOT:
 * - Know about game logic or entities
 * - Render anything
 *
 * Preferences key: "space-shooter-save"
 */
public class SaveManager {

    private static final String PREFS_NAME = "space-shooter-save";
    private static final String KEY_SAVED_WAVE = "savedWave";
    private static final String KEY_SCORE = "score";
    private static final String KEY_LIFE = "life";
    private static final String KEY_STARS = "stars";
    private static final String KEY_HAS_SAVE = "hasSave";

    private Preferences prefs;

    public SaveManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    /**
     * Save current game progression.
     *
     * @param wave  Current wave number (1-based)
     * @param score Current score
     * @param life  Ship's current life
     * @param stars Number of coins/stars collected
     */
    public void save(int wave, long score, float life, long stars) {
        prefs.putInteger(KEY_SAVED_WAVE, wave);
        prefs.putLong(KEY_SCORE, score);
        prefs.putFloat(KEY_LIFE, life);
        prefs.putLong(KEY_STARS, stars);
        prefs.putBoolean(KEY_HAS_SAVE, true);
        prefs.flush();

        Gdx.app.log("[SaveManager]", "Game Saved — Wave:" + wave +
                " Score:" + score + " Life:" + life + " Stars:" + stars);
    }

    /**
     * Load saved game data. Returns null if no save exists.
     */
    public SaveData load() {
        if (!hasSavedGame()) {
            Gdx.app.log("[SaveManager]", "No saved game found");
            return null;
        }

        SaveData data = new SaveData();
        data.savedWave = prefs.getInteger(KEY_SAVED_WAVE, 1);
        data.score = prefs.getLong(KEY_SCORE, 0);
        data.life = prefs.getFloat(KEY_LIFE, 5f);
        data.stars = prefs.getLong(KEY_STARS, 0);

        Gdx.app.log("[SaveManager]", "Game Loaded — Wave:" + data.savedWave +
                " Score:" + data.score + " Life:" + data.life + " Stars:" + data.stars);
        return data;
    }

    /**
     * Check whether a saved game exists.
     */
    public boolean hasSavedGame() {
        return prefs.getBoolean(KEY_HAS_SAVE, false);
    }

    /**
     * Clear all saved game data.
     */
    public void clear() {
        prefs.clear();
        prefs.flush();
        Gdx.app.log("[SaveManager]", "Save data cleared");
    }

    /**
     * Simple DTO for saved game state.
     */
    public static class SaveData {
        public int savedWave;
        public long score;
        public float life;
        public long stars;
    }
}
