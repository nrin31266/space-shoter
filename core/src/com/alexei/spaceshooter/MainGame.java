package com.alexei.spaceshooter;

import com.alexei.spaceshooter.manager.CampaignAssets;
import com.alexei.spaceshooter.manager.CampaignAudio;
import com.alexei.spaceshooter.manager.CampaignStore;
import com.alexei.spaceshooter.screen.LoadingScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;

public class MainGame extends Game {
    public final com.alexei.spaceshooter.manager.DevRunConfig devConfig = new com.alexei.spaceshooter.manager.DevRunConfig();
    private final AssetManager assetManager = new AssetManager();
    public CampaignAssets assets;
    public CampaignAudio audio;
    public CampaignStore store;
    public CampaignStore.State profile;
    public String saveError;
    public CampaignStore.State pendingCheckpoint;
    private boolean storageBlocked;
    private final int previewMission, previewEncounter;

    public MainGame() { this(0, 0); }
    public MainGame(int previewMission, int previewEncounter) {
        this.previewMission = previewMission; this.previewEncounter = previewEncounter;
    }

    public void openInitialScreen() {
        if (previewMission >= 1 && previewMission <= 8 && previewEncounter >= 0
                && previewEncounter < assets.catalog.missions[previewMission - 1].get("Encounters").size) {
            CampaignStore.State state = copy(profile);
            com.badlogic.gdx.utils.JsonValue preset = assets.catalog.missions[previewMission - 1].get("ReplayPreset");
            state.mission = previewMission; state.encounter = previewEncounter;
            state.lives = preset.getInt("Lives"); state.missiles = preset.getInt("Missiles");
            state.weaponLevels = preset.get("WeaponLevels").asIntArray(); state.activeWeapon = preset.getInt("ActiveWeapon");
            state.missionCleared = state.completed = state.regionReady = false;
            state.shard = state.score = 0;
            CampaignStore.resetMissionStats(state);
            setScreen(new com.alexei.spaceshooter.screen.GamePlayScreen(this, state, true, previewEncounter == 0));
        } else setScreen(new com.alexei.spaceshooter.screen.MainMenuScreen(this));
    }

    @Override public void create() {
        Gdx.input.setCatchKey(com.badlogic.gdx.Input.Keys.BACK, true);
        setScreen(new LoadingScreen(this));
    }

    public void loadCampaign() {
        assets = new CampaignAssets(assetManager);
        audio = new CampaignAudio(assets);
        store = new CampaignStore(Gdx.files.local("campaign").file());
        try {
            profile = store.load();
            if (profile != null) {
                com.badlogic.gdx.utils.JsonValue options = assets.catalog.asset("Data/Cosmetics/ShipCosmetics.asset").get("Options");
                for (int slot = 0; slot < 3; slot++) {
                    boolean found = false;
                    for (com.badlogic.gdx.utils.JsonValue option : options) if (option.getString("Id").equals(profile.equippedCosmetics[slot]) && option.getInt("Slot") == slot) found = true;
                    if (!found) throw new IllegalStateException("Unknown cosmetic in save");
                }
            }
        } catch (RuntimeException error) {
            saveError = error.getMessage();
            storageBlocked = true;
            profile = null;
            Gdx.app.error("CampaignSave", "Giữ nguyên dữ liệu, không tạo đè save", error);
        }
        if (profile == null) { profile = new CampaignStore.State(); profile.hasCampaign = false; }
        audio.musicVolume = profile.musicVolume;
        audio.sfxVolume = profile.sfxVolume;
    }

    public CampaignStore.State copy(CampaignStore.State state) {
        Json json = new Json();
        json.setUsePrototypes(false);
        return json.fromJson(CampaignStore.State.class, json.toJson(state));
    }

    public void checkpoint(CampaignStore.State state) {
        if (storageBlocked) return;
        pendingCheckpoint = copy(state);
        flushPending();
    }

    public boolean flushPending() {
        if (storageBlocked) return false;
        if (pendingCheckpoint == null) return true;
        try {
            store.save(pendingCheckpoint);
            profile = copy(pendingCheckpoint);
            pendingCheckpoint = null;
            saveError = null;
            return true;
        } catch (RuntimeException error) {
            saveError = error.getMessage();
            Gdx.app.error("CampaignSave", "Không ghi được checkpoint", error);
            return false;
        }
    }

    public int creditShards(int cumulative) {
        CampaignStore.State candidate = copy(pendingCheckpoint == null ? profile : pendingCheckpoint);
        int previous = candidate.encounterShardHighWater;
        CampaignStore.creditShards(candidate, cumulative);
        if (candidate.encounterShardHighWater != previous) checkpoint(candidate);
        return candidate.shard;
    }

    public boolean equipCosmetic(String id) {
        if (!flushPending()) return false;
        com.badlogic.gdx.utils.JsonValue options = assets.catalog.asset("Data/Cosmetics/ShipCosmetics.asset").get("Options");
        for (com.badlogic.gdx.utils.JsonValue option : options) if (option.getString("Id").equals(id)) {
            CampaignStore.State candidate = copy(profile);
            if (!CampaignStore.equipCosmetic(candidate, option)) return false;
            checkpoint(candidate);
            return saveError == null;
        }
        return false;
    }

    public AssetManager getAssetManager() { return assetManager; }

    @Override public void setScreen(Screen next) {
        Screen previous = getScreen();
        super.setScreen(next);
        if (previous != null) previous.dispose();
    }

    @Override public void pause() {
        super.pause();
        if (audio != null) audio.suspend(true);
    }

    @Override public void resume() {
        super.resume();
        if (audio != null) audio.suspend(false);
    }

    @Override public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        if (assets != null) assets.dispose();
        assetManager.dispose();
    }
}
