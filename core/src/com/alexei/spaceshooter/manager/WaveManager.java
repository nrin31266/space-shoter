package com.alexei.spaceshooter.manager;

import com.alexei.spaceshooter.data.wave.SpawnAction;
import com.alexei.spaceshooter.data.wave.WaveConfig;
import com.alexei.spaceshooter.data.wave.WaveData;
import com.alexei.spaceshooter.entity.Unit;
import com.alexei.spaceshooter.factory.EnemyFactory;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages wave progression: loading, timing, triggering spawns, and wave transitions.
 *
 * Responsibilities:
 * - Load wave data from WaveConfig
 * - Track elapsed time within a wave
 * - Trigger SpawnAction when delay is reached
 * - Track wave completion state
 *
 * Does NOT:
 * - Create enemies directly (delegates to EnemyFactory)
 * - Render anything
 * - Access Stage, UI, or collision
 *
 * Extensible: add boss waves, difficulty scaling, endless mode.
 */
public class WaveManager {

    private final EnemyFactory enemyFactory;
    private WaveConfig waveConfig;

    private int currentWaveId = 0;
    private int effectiveWaveId = 0;
    private float elapsedTime = 0f;
    private int currentActionIndex = 0;

    /** All spawn actions for the current wave, in order */
    private List<SpawnAction> currentActions = new ArrayList<>();

    /** Actions that have already been processed this wave */
    private final List<SpawnAction> processedActions = new ArrayList<>();

    /** Flag: all actions have been triggered */
    private boolean allActionsTriggered = false;

    private int activePlayerWeaponType = 0;
    public void setActivePlayerWeaponType(int type) { this.activePlayerWeaponType = type; }

    /** Flag: wave is fully cleared (all enemies dead after all actions triggered) */
    private boolean waveCleared = false;

    /** Total enemies successfully spawned in this wave (prevents false-clear on empty action list) */
    private int totalEnemiesSpawned = 0;

    public WaveManager(EnemyFactory enemyFactory) {
        this.enemyFactory = enemyFactory;
    }

    /**
     * Load wave data from a WaveConfig.
     */
    public void loadConfig(WaveConfig config) {
        this.waveConfig = config;
        Gdx.app.log("[WaveManager]", "Config loaded: " +
                (config != null ? config.getWaveCount() + " waves" : "null"));
    }

    /**
     * Start a specific wave by ID.
     */
    public void startWave(int waveId) {
        startWave(waveId, waveId);
    }

    public void startWave(int waveId, int effectiveWaveId) {
        if (waveConfig == null) {
            Gdx.app.error("[WaveManager]", "Cannot start wave — no config loaded");
            return;
        }

        WaveData data = waveConfig.getWave(waveId);
        if (data == null) {
            Gdx.app.error("[WaveManager]", "Wave " + waveId + " not found in config");
            return;
        }

        this.currentWaveId = waveId;
        this.effectiveWaveId = effectiveWaveId;
        elapsedTime = 0f;
        currentActionIndex = 0;
        allActionsTriggered = false;
        waveCleared = false;
        totalEnemiesSpawned = 0;
        currentActions = new ArrayList<>(data.actions);
        processedActions.clear();

        Gdx.app.log("[WaveManager]", "Start Wave " + waveId + " (Effective " + effectiveWaveId + ", " + currentActions.size() + " actions)");
    }

    /**
     * Update wave timer. Called every frame during PLAYING state.
     * Checks for pending SpawnActions and returns new enemies to spawn.
     *
     * @param deltaTime   Milliseconds since last frame
     * @param screenWidth Current screen width
     * @param screenHeight Current screen height
     * @return List of new enemies that should be added to GameState (may be empty)
     */
    public List<Unit> update(float deltaTime, float screenWidth, float screenHeight) {
        List<Unit> newEnemies = new ArrayList<>();

        if (allActionsTriggered) {
            return newEnemies;
        }

        elapsedTime += deltaTime / 1000f; // Convert ms to seconds

        // Check for actions whose delay has passed
        while (currentActionIndex < currentActions.size()) {
            SpawnAction action = currentActions.get(currentActionIndex);
            if (elapsedTime >= action.delay) {
                // Trigger this action with HP scaling by effectiveWaveId
                List<Unit> spawned = enemyFactory.createFromAction(action, screenWidth, screenHeight, effectiveWaveId > 0 ? effectiveWaveId : currentWaveId);
                
                // Pity System (Section 6.2): Guaranteed Upgrade Drop on final action of waves 4, 9, 14, 18, 19
                boolean isPityWave = (currentWaveId == 4 || currentWaveId == 9 || currentWaveId == 14 || currentWaveId == 18 || currentWaveId == 19);
                boolean isLastAction = (currentActionIndex == currentActions.size() - 1);
                if (isPityWave && isLastAction && !spawned.isEmpty() && activePlayerWeaponType >= 0) {
                    spawned.get(0).setPityWeaponType(activePlayerWeaponType);
                    Gdx.app.log("[WaveManager]", "Pity Drop assigned for player weapon track " + activePlayerWeaponType);
                }

                newEnemies.addAll(spawned);
                totalEnemiesSpawned += spawned.size();
                processedActions.add(action);
                currentActionIndex++;

                Gdx.app.log("[WaveManager]", "Action triggered: " + action.toString());
            } else {
                break; // Remaining actions have higher delay
            }
        }

        // Check if all actions have been triggered
        if (currentActionIndex >= currentActions.size()) {
            allActionsTriggered = true;
            Gdx.app.log("[WaveManager]", "All actions triggered for Wave " + currentWaveId);
        }

        return newEnemies;
    }

    /**
     * Call this when all enemies are destroyed AND all actions have been triggered.
     * Marks the wave as cleared.
     */
    public void markWaveCleared() {
        if (allActionsTriggered) {
            waveCleared = true;
            Gdx.app.log("[WaveManager]", "Wave " + currentWaveId + " CLEARED!");
        }
    }

    /**
     * @return true if current wave is fully complete (all actions done + all enemies dead).
     */
    public boolean isWaveCleared() {
        return waveCleared;
    }

    /**
     * @return true if all spawn actions have been triggered (enemies may still be alive).
     */
    public boolean isWaveFinished() {
        return allActionsTriggered;
    }

    /**
     * @return true if there is another wave after the current one.
     */
    public boolean hasMoreWaves() {
        return waveConfig != null && waveConfig.hasWave(currentWaveId + 1);
    }

    public int getCurrentWaveId() {
        return currentWaveId;
    }

    /** Returns true once startWave() has been called (a wave is in progress). */
    public boolean isWaveStarted() {
        return currentWaveId > 0;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Get actions that have already been processed in the current wave.
     */
    public List<SpawnAction> getProcessedActions() {
        return processedActions;
    }

    public int getTotalEnemiesSpawned() {
        return totalEnemiesSpawned;
    }

    /**
     * Reset to initial state (wave 0, no config changes).
     */
    public void reset() {
        currentWaveId = 0;
        elapsedTime = 0f;
        currentActionIndex = 0;
        currentActions.clear();
        processedActions.clear();
        allActionsTriggered = false;
        waveCleared = false;
        totalEnemiesSpawned = 0;
    }
}
