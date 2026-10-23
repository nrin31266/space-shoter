package com.alexei.spaceshooter.manager;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CampaignStore {
    public static final int CURRENT_VERSION = 1;
    public static final String FILE_NAME = "campaign-v1.json";
    public static final String BACKUP_NAME = "campaign-v1.json.bak";
    public static final String TEMP_NAME = "campaign-v1.json.tmp";
    public static final String CORRUPT_NAME = "campaign-v1.json.corrupt";

    private final File directory;
    private final File primaryFile;
    private final File bakFile;
    private final File tmpFile;
    private final File corruptFile;

    public CampaignStore(File directory) {
        if (directory == null) {
            throw new IllegalArgumentException("Directory cannot be null");
        }
        this.directory = directory;
        this.primaryFile = new File(directory, FILE_NAME);
        this.bakFile = new File(directory, BACKUP_NAME);
        this.tmpFile = new File(directory, TEMP_NAME);
        this.corruptFile = new File(directory, CORRUPT_NAME);
    }

    public static class State {
        public int version = CURRENT_VERSION;
        public boolean hasCampaign = true;
        public int missionScore;
        public float missionSeconds;
        public int enemiesDestroyed, coresCollected, supplyCaptured, livesLost;
        public float[] bestTimes = new float[8];
        public int encounterShardHighWater;
        public String[] ownedCosmetics = {"chassis-arrow", "skin-cyan", "trail-none"};
        public String[] equippedCosmetics = {"chassis-arrow", "skin-cyan", "trail-none"};
        public int mission = 1;
        public int encounter = 0;
        public int lives = 3;
        public int missiles = 1; // Sức chứa tối đa tên lửa dự trữ là 3 theo hợp đồng
        public int activeWeapon = 0;
        public int[] weaponLevels = new int[]{1, 0, 0};
        public int shard = 0;
        public int score = 0;
        public int unlockedMission = 1;
        public boolean regionReady;
        public boolean missionCleared = false;
        public boolean completed = false;
        public int[] bestScores = new int[8];
        public boolean[] rewardedMissions = new boolean[8];
        public String cosmetic = "default";
        public float musicVolume = 0.65f;
        public float sfxVolume = 0.8f;
        public boolean reducedMotion = false;
        public boolean vibrationEnabled = true;

        public State() {
        }
    }

    public static void validate(State state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        if (state.version != CURRENT_VERSION) {
            throw new IllegalStateException("Unsupported schema version: " + state.version);
        }
        if (state.mission < 1 || state.mission > 8) {
            throw new IllegalStateException("Mission out of range (1..8): " + state.mission);
        }
        int[] counts = {2, 3, 3, 2, 3, 3, 3, 2};
        if (state.encounter < 0 || state.encounter >= counts[state.mission - 1]) {
            throw new IllegalStateException("Encounter cannot be negative: " + state.encounter);
        }
        if (state.lives < 1 || state.lives > 5) {
            throw new IllegalStateException("Lives out of range (1..5): " + state.lives);
        }
        if (state.missiles < 0 || state.missiles > 3) {
            throw new IllegalStateException("Missiles out of range (0..3): " + state.missiles);
        }
        if (state.weaponLevels == null || state.weaponLevels.length != 3) {
            throw new IllegalStateException("WeaponLevels must have length 3");
        }
        if (state.weaponLevels[0] < 1) {
            throw new IllegalStateException("Primary weapon level must be >= 1");
        }
        for (int i = 0; i < state.weaponLevels.length; i++) {
            if (state.weaponLevels[i] < 0 || state.weaponLevels[i] > 5) {
                throw new IllegalStateException("WeaponLevel at index " + i + " cannot be negative");
            }
        }
        if (state.activeWeapon < 0 || state.activeWeapon >= state.weaponLevels.length) {
            throw new IllegalStateException("ActiveWeapon index out of bounds: " + state.activeWeapon);
        }
        if (state.weaponLevels[state.activeWeapon] == 0) throw new IllegalStateException("Active weapon is locked");
        if (!Float.isFinite(state.missionSeconds) || state.missionSeconds < 0 || state.enemiesDestroyed < 0
                || state.coresCollected < 0 || state.supplyCaptured < 0 || state.livesLost < 0)
            throw new IllegalStateException("Invalid mission statistics");
        if (state.bestTimes == null || state.bestTimes.length != 8) throw new IllegalStateException("Invalid mission time records");
        for (float seconds : state.bestTimes) if (!Float.isFinite(seconds) || seconds < 0) throw new IllegalStateException("Invalid best time");
        if (state.missionScore < 0 || state.encounterShardHighWater < 0) throw new IllegalStateException("Invalid run counters");
        if (state.ownedCosmetics == null || state.equippedCosmetics == null || state.equippedCosmetics.length != 3)
            throw new IllegalStateException("Invalid cosmetic inventory");
        for (String id : state.ownedCosmetics) if (id == null || id.trim().isEmpty())
            throw new IllegalStateException("Invalid owned cosmetic");
        for (String id : state.equippedCosmetics) {
            boolean owned = false;
            for (String candidate : state.ownedCosmetics) if (candidate.equals(id)) owned = true;
            if (!owned) throw new IllegalStateException("Equipped cosmetic is not owned");
        }
        if (state.shard < 0) {
            throw new IllegalStateException("Shard cannot be negative: " + state.shard);
        }
        if (state.score < 0) {
            throw new IllegalStateException("Score cannot be negative: " + state.score);
        }
        if (state.unlockedMission < 1 || state.unlockedMission > 8) {
            throw new IllegalStateException("UnlockedMission out of range (1..8): " + state.unlockedMission);
        }
        if (state.bestScores == null || state.bestScores.length != 8) {
            throw new IllegalStateException("BestScores must have length 8");
        }
        for (int i = 0; i < state.bestScores.length; i++) {
            if (state.bestScores[i] < 0) {
                throw new IllegalStateException("BestScore at index " + i + " cannot be negative");
            }
        }
        if (state.rewardedMissions == null || state.rewardedMissions.length != 8) {
            throw new IllegalStateException("RewardedMissions must have length 8");
        }
        if (state.cosmetic == null || state.cosmetic.trim().isEmpty()) {
            throw new IllegalStateException("Cosmetic identifier cannot be empty");
        }
        if (Float.isNaN(state.musicVolume) || state.musicVolume < 0.0f || state.musicVolume > 1.0f) {
            throw new IllegalStateException("MusicVolume out of range (0.0..1.0): " + state.musicVolume);
        }
        if (Float.isNaN(state.sfxVolume) || state.sfxVolume < 0.0f || state.sfxVolume > 1.0f) {
            throw new IllegalStateException("SfxVolume out of range (0.0..1.0): " + state.sfxVolume);
        }
    }

    public static void resetMissionStats(State state) {
        state.missionScore = state.encounterShardHighWater = 0;
        state.missionSeconds = 0;
        state.enemiesDestroyed = state.coresCollected = state.supplyCaptured = state.livesLost = 0;
    }

    public static void creditShards(State state, int cumulative) {
        if (cumulative < 0) throw new IllegalArgumentException("Negative shard total");
        int delta = cumulative - state.encounterShardHighWater;
        if (delta <= 0) return;
        state.shard = Math.addExact(state.shard, delta);
        state.encounterShardHighWater = cumulative;
    }

    public static boolean equipCosmetic(State state, JsonValue option) {
        validate(state);
        String id = option.getString("Id");
        int slot = option.getInt("Slot"), cost = option.getInt("ShardCost");
        if (slot < 0 || slot >= 3 || cost < 0) throw new IllegalArgumentException("Invalid cosmetic option");
        boolean owned = false;
        for (String item : state.ownedCosmetics) if (item.equals(id)) owned = true;
        if (!owned && state.shard < cost) return false;
        if (!owned) {
            state.ownedCosmetics = java.util.Arrays.copyOf(state.ownedCosmetics, state.ownedCosmetics.length + 1);
            state.ownedCosmetics[state.ownedCosmetics.length - 1] = id;
            state.shard -= cost;
        }
        state.equippedCosmetics[slot] = id;
        return true;
    }

    public State load() {
        if (!primaryFile.exists() && !bakFile.exists()) {
            if (corruptFile.exists()) throw new IllegalStateException("Corrupt save preserved; recovery required");
            return null;
        }

        // Schema mới hơn phải bị từ chối ngay lập tức, không fallback và không ghi đè làm mất dữ liệu người dùng
        if (primaryFile.exists()) {
            int primaryVer = peekVersion(primaryFile);
            if (primaryVer > CURRENT_VERSION) {
                throw new IllegalStateException("Primary save file schema version " + primaryVer + " is newer than supported " + CURRENT_VERSION);
            }
            try {
                return readAndValidate(primaryFile);
            } catch (Exception primaryEx) {
                // Giữ lại file hỏng để phục vụ phân tích/cứu hộ, không xóa bỏ
                preserveCorruptedFile(primaryFile);

                if (bakFile.exists()) {
                    int bakVer = peekVersion(bakFile);
                    if (bakVer > CURRENT_VERSION) {
                        throw new IllegalStateException("Backup save schema version " + bakVer + " is newer than supported " + CURRENT_VERSION, primaryEx);
                    }
                    try {
                        return readAndValidate(bakFile);
                    } catch (Exception bakEx) {
                        throw new RuntimeException("Primary save was corrupt (preserved) and backup save also failed", bakEx);
                    }
                }
                throw new RuntimeException("Primary save was corrupt (preserved) and no backup exists", primaryEx);
            }
        }

        // Trường hợp không có primary nhưng còn backup
        int bakVer = peekVersion(bakFile);
        if (bakVer > CURRENT_VERSION) {
            throw new IllegalStateException("Backup save schema version " + bakVer + " is newer than supported " + CURRENT_VERSION);
        }
        try {
            return readAndValidate(bakFile);
        } catch (Exception bakEx) {
            throw new RuntimeException("Primary save missing and backup save is corrupt", bakEx);
        }
    }

    public void save(State state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        validate(state);

        if (!directory.exists() && !directory.mkdirs() && !directory.exists()) {
            throw new RuntimeException("Cannot create save directory: " + directory.getAbsolutePath());
        }

        // Ngăn chặn ghi đè làm hỏng file save của phiên bản tương lai
        if (primaryFile.exists() && peekVersion(primaryFile) > CURRENT_VERSION) {
            throw new IllegalStateException("Cannot overwrite primary save: existing version is newer than supported");
        }
        if (bakFile.exists() && peekVersion(bakFile) > CURRENT_VERSION) {
            throw new IllegalStateException("Cannot overwrite backup save: existing version is newer than supported");
        }

        Json json = createStrictJson();
        String jsonText = json.toJson(state);

        // Ghi tạm thời và ép đồng bộ vật lý xuống ổ đĩa tránh mất điện làm hỏng dữ liệu
        try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
            fos.write(jsonText.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.getFD().sync();
        } catch (IOException e) {
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            throw new RuntimeException("Failed writing temporary save file", e);
        }

        // Kiểm tra tính toàn vẹn và hợp lệ của file tạm trước khi thay thế file chính
        try {
            readAndValidate(tmpFile);
        } catch (Exception e) {
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            throw new RuntimeException("Temporary save verification failed before commit", e);
        }

        // Chỉ sao lưu file primary nếu nó hợp lệ; không sao lưu file hỏng đè mất backup cũ
        if (primaryFile.exists()) {
            boolean primaryValid = false;
            try {
                readAndValidate(primaryFile);
                primaryValid = true;
            } catch (Exception e) {
                preserveCorruptedFile(primaryFile);
            }
            if (primaryValid) {
                try {
                    Files.copy(primaryFile.toPath(), bakFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Failed creating backup before save commit", e);
                }
            }
        }

        // Đổi tên nguyên tử tránh tình trạng file bị cắt ngắn nếu ứng dụng crash giữa chừng
        try {
            moveAtomic(tmpFile.toPath(), primaryFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to atomically commit save file", e);
        }
    }

    private static Json createStrictJson() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
        // Bắt buộc cấu trúc chặt chẽ, phát hiện trường lạ hoặc sai lệch
        json.setIgnoreUnknownFields(false);
        return json;
    }

    private State readAndValidate(File file) throws IOException {
        if (!file.exists() || file.length() == 0) {
            throw new IOException("Save file empty or missing: " + file.getName());
        }
        State state;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Json json = createStrictJson();
            JsonValue root = new JsonReader().parse(reader);
            if (!root.has("version") || root.getInt("version") != CURRENT_VERSION)
                throw new IOException("Missing or unsupported save version");
            state = json.readValue(State.class, root);
        } catch (Exception e) {
            throw new IOException("JSON parsing failed for " + file.getName(), e);
        }
        if (state == null) {
            throw new IOException("Parsed state was null from " + file.getName());
        }
        validate(state);
        return state;
    }

    private int peekVersion(File file) {
        if (!file.exists() || file.length() == 0) {
            return -1;
        }
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonValue root = new JsonReader().parse(reader);
            if (root != null) {
                JsonValue ver = root.get("version");
                if (ver != null) {
                    return ver.asInt();
                }
            }
        } catch (Exception ignored) {
            // Định dạng không phải JSON hợp lệ sẽ trả về -1
        }
        return -1;
    }

    private void preserveCorruptedFile(File file) {
        try {
            moveAtomic(file.toPath(), corruptFile.toPath());
        } catch (Exception e) {
            file.renameTo(corruptFile);
        }
    }

    private static void moveAtomic(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            // Hỗ trợ fallback khi hệ thống tệp không hỗ trợ cờ ATOMIC_MOVE
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
