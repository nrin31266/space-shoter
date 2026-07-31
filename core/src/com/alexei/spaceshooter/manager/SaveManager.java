package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

/**
 * Manages save/load of game progression using LibGDX Preferences.
 * High Score and Total Stars are committed ONLY at the end of a game run (on Game Over / player death).
 */
public class SaveManager {

    private static final String PREFS_NAME = "space-shooter-save";
    private static final String KEY_SAVED_WAVE = "savedWave";
    private static final String KEY_SCORE = "score";
    private static final String KEY_LIFE = "life";
    private static final String KEY_STARS = "stars";
    private static final String KEY_HAS_SAVE = "hasSave";
    private static final String KEY_HIGH_SCORE = "highScore";
    private static final String KEY_TOTAL_STARS = "totalStars";

    private Preferences prefs;

    public SaveManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public long getHighScore() {
        return prefs.getLong(KEY_HIGH_SCORE, 0);
    }

    public long getTotalStars() {
        return prefs.getLong(KEY_TOTAL_STARS, 0);
    }

    /**
     * Commits final game stats ON GAME OVER (player death).
     * High Score updates if sessionScore > current high score.
     * Total Stars accumulates sessionStars.
     */
    public void commitGameStats(long sessionScore, long sessionStars) {
        long currentHigh = getHighScore();
        if (sessionScore > currentHigh) {
            prefs.putLong(KEY_HIGH_SCORE, sessionScore);
        }
        if (sessionStars > 0) {
            long total = getTotalStars() + sessionStars;
            prefs.putLong(KEY_TOTAL_STARS, total);
        }
        prefs.flush();
        Gdx.app.log("[SaveManager]", "Committed End-of-Game Stats — HighScore:" + getHighScore() + " TotalStars:" + getTotalStars());
    }

    /**
     * Save current game progression with full 3-track weapon states and stockpiles.
     */
    public void save(int wave, long score, float life, long stars,
                     int activeWeaponType, int lvl0, int lvl1, int lvl2,
                     int stock0, int stock1, int stock2, float maxLife) {
        prefs.putInteger(KEY_SAVED_WAVE, wave);
        prefs.putLong(KEY_SCORE, score);
        prefs.putFloat(KEY_LIFE, life);
        prefs.putLong(KEY_STARS, stars);

        prefs.putInteger("activeWeaponType", activeWeaponType);
        prefs.putInteger("weaponLevel_0", lvl0);
        prefs.putInteger("weaponLevel_1", lvl1);
        prefs.putInteger("weaponLevel_2", lvl2);
        prefs.putInteger("stockpile_0", stock0);
        prefs.putInteger("stockpile_1", stock1);
        prefs.putInteger("stockpile_2", stock2);
        prefs.putFloat("maxLife", maxLife);

        prefs.putBoolean(KEY_HAS_SAVE, true);
        prefs.flush();

        Gdx.app.log("[SaveManager]", "Game Saved — Wave:" + wave + " Score:" + score + " Life:" + life);
    }

    public void save(int wave, long score, float life, long stars) {
        save(wave, score, life, stars, 0, 1, 1, 1, 0, 0, 0, 10f);
    }

    public SaveData load() {
        if (!hasSavedGame()) {
            Gdx.app.log("[SaveManager]", "No saved game found");
            return null;
        }

        SaveData data = new SaveData();
        data.savedWave = prefs.getInteger(KEY_SAVED_WAVE, 1);
        data.savedWave = MathUtils.clamp(data.savedWave, 1, 20);
        data.score = prefs.getLong(KEY_SCORE, 0);
        data.life = prefs.getFloat(KEY_LIFE, 5f);
        data.stars = prefs.getLong(KEY_STARS, 0);

        data.activeWeaponType = prefs.getInteger("activeWeaponType", 0);
        data.weaponLevel0     = prefs.getInteger("weaponLevel_0", prefs.getInteger("weaponLevel", 1));
        data.weaponLevel1     = prefs.getInteger("weaponLevel_1", 1);
        data.weaponLevel2     = prefs.getInteger("weaponLevel_2", 1);

        data.stockpile0       = prefs.getInteger("stockpile_0", 0);
        data.stockpile1       = prefs.getInteger("stockpile_1", 0);
        data.stockpile2       = prefs.getInteger("stockpile_2", 0);

        data.maxLife          = prefs.getFloat("maxLife", 10f);

        Gdx.app.log("[SaveManager]", "Game Loaded — Wave:" + data.savedWave +
                " Type:" + data.activeWeaponType + " Lv0:" + data.weaponLevel0);
        return data;
    }

    public boolean hasSavedGame() {
        return prefs.getBoolean(KEY_HAS_SAVE, false);
    }

    public void clear() {
        long high = getHighScore();
        long stars = getTotalStars();

        prefs.clear();
        // Preserve High Score & Cumulative Total Stars even when clearing active wave save!
        prefs.putLong(KEY_HIGH_SCORE, high);
        prefs.putLong(KEY_TOTAL_STARS, stars);
        prefs.flush();
        Gdx.app.log("[SaveManager]", "Current run cleared. HighScore & TotalStars preserved.");
    }

    public static class SaveData {
        public int savedWave;
        public long score;
        public float life;
        public long stars;

        public int activeWeaponType = 0;
        public int weaponLevel0 = 1;
        public int weaponLevel1 = 1;
        public int weaponLevel2 = 1;
        public int stockpile0 = 0;
        public int stockpile1 = 0;
        public int stockpile2 = 0;
        public float maxLife = 10f;
    }
}
