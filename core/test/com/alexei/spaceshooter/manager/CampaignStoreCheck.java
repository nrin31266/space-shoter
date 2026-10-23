package com.alexei.spaceshooter.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class CampaignStoreCheck {
    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("campaign_store_test_");
        File dir = tempDir.toFile();
        try {
            testWriteFailure(dir);
            testProfileRewards(dir);
            testRoundtrip(dir);
            testTruncatedPrimaryBackupRecovery(dir);
            testCorruptBothReport(dir);
            testInvalidRanges(dir);
            testNewerSchemaRefused(dir);
            testEarlierBackupNotReplacedByCorruptPrimary(dir);
            System.out.println("ALL CAMPAIGN STORE CHECKS PASSED SUCCESSFULLY.");
        } finally {
            deleteRecursively(dir);
        }
    }

    private static void testWriteFailure(File dir) throws IOException {
        cleanDirectory(dir);
        CampaignStore store = new CampaignStore(dir);
        CampaignStore.State state = new CampaignStore.State();
        store.save(state);
        File blocked = new File(dir, CampaignStore.TEMP_NAME);
        assertTrue(blocked.mkdir(), "Create blocked temp path");
        state.score = 99;
        boolean failed = false;
        try { store.save(state); } catch (RuntimeException expected) { failed = true; }
        assertTrue(failed && store.load().score == 0, "Write failure preserves prior checkpoint");
    }

    private static void testProfileRewards(File dir) {
        cleanDirectory(dir);
        CampaignStore store = new CampaignStore(dir);
        CampaignStore.State state = new CampaignStore.State();
        CampaignStore.creditShards(state, 8);
        store.save(state);
        state = store.load();
        CampaignStore.creditShards(state, 3);
        CampaignStore.creditShards(state, 8);
        assertTrue(state.shard == 8, "Retry must not credit shards twice");
        CampaignStore.creditShards(state, 10);
        assertTrue(state.shard == 10, "Only credit above prior encounter high water");
        com.badlogic.gdx.utils.JsonValue option = new com.badlogic.gdx.utils.JsonReader().parse("{Id:skin-amber,Slot:1,ShardCost:6}");
        assertTrue(CampaignStore.equipCosmetic(state, option), "Affordable skin");
        assertTrue(state.shard == 4 && state.equippedCosmetics[1].equals("skin-amber"), "Purchase and equip");
        assertTrue(CampaignStore.equipCosmetic(state, option) && state.shard == 4, "Owned item is free to equip");
        option.get("Id").set("skin-other");
        assertTrue(!CampaignStore.equipCosmetic(state, option) && state.shard == 4, "Insufficient funds preserves state");
        store.save(state);
        state = store.load();
        assertTrue(state.equippedCosmetics[1].equals("skin-amber") && state.ownedCosmetics.length == 4, "Inventory roundtrip");
    }

    private static void testRoundtrip(File dir) {
        cleanDirectory(dir);
        CampaignStore store = new CampaignStore(dir);
        CampaignStore.State initial = store.load();
        assertTrue(initial == null, "Initial load on empty dir must return null");

        CampaignStore.State state = new CampaignStore.State();
        state.mission = 4;
        state.encounter = 1;
        state.lives = 4;
        state.missiles = 3;
        state.activeWeapon = 1;
        state.weaponLevels = new int[]{2, 1, 0};
        state.shard = 150;
        state.score = 9800;
        state.unlockedMission = 5;
        state.missionCleared = true; state.regionReady = true;
        state.completed = false;
        state.bestScores = new int[]{1000, 2000, 3000, 3800, 0, 0, 0, 0};
        state.rewardedMissions = new boolean[]{true, true, true, true, false, false, false, false};
        state.cosmetic = "stealth_fighter";
        state.musicVolume = 0.5f;
        state.sfxVolume = 0.75f;
        state.reducedMotion = true; state.vibrationEnabled = false;

        store.save(state);

        CampaignStore.State loaded = store.load();
        assertTrue(loaded != null, "Loaded state must not be null");
        assertTrue(loaded.version == 1, "Version match");
        assertTrue(loaded.mission == 4, "Mission match");
        assertTrue(loaded.encounter == 1, "Encounter match");
        assertTrue(loaded.lives == 4, "Lives match");
        assertTrue(loaded.missiles == 3, "Missiles match");
        assertTrue(loaded.activeWeapon == 1, "ActiveWeapon match");
        assertTrue(Arrays.equals(loaded.weaponLevels, new int[]{2, 1, 0}), "WeaponLevels match");
        assertTrue(loaded.shard == 150, "Shard match");
        assertTrue(loaded.score == 9800, "Score match");
        assertTrue(loaded.unlockedMission == 5, "UnlockedMission match");
        assertTrue(loaded.missionCleared && loaded.regionReady, "Mission clear/region-ready roundtrip");
        assertTrue(!loaded.completed, "Completed match");
        assertTrue(Arrays.equals(loaded.bestScores, new int[]{1000, 2000, 3000, 3800, 0, 0, 0, 0}), "BestScores match");
        assertTrue(Arrays.equals(loaded.rewardedMissions, new boolean[]{true, true, true, true, false, false, false, false}), "RewardedMissions match");
        assertTrue("stealth_fighter".equals(loaded.cosmetic), "Cosmetic match");
        assertTrue(Math.abs(loaded.musicVolume - 0.5f) < 0.0001f, "MusicVolume match");
        assertTrue(Math.abs(loaded.sfxVolume - 0.75f) < 0.0001f, "SfxVolume match");
        assertTrue(loaded.reducedMotion && !loaded.vibrationEnabled, "Feedback settings roundtrip");
    }

    private static void testTruncatedPrimaryBackupRecovery(File dir) throws IOException {
        cleanDirectory(dir);
        CampaignStore store = new CampaignStore(dir);

        CampaignStore.State state1 = new CampaignStore.State();
        state1.mission = 2;
        state1.score = 500;
        store.save(state1);

        CampaignStore.State state2 = new CampaignStore.State();
        state2.mission = 3;
        state2.score = 1200;
        store.save(state2);

        File primary = new File(dir, CampaignStore.FILE_NAME);
        File corrupt = new File(dir, CampaignStore.CORRUPT_NAME);
        // Làm hỏng file primary bằng chuỗi JSON dở dang
        try (FileOutputStream fos = new FileOutputStream(primary)) {
            fos.write("{\"version\":1, \"mission\":".getBytes(StandardCharsets.UTF_8));
        }

        CampaignStore.State recovered = store.load();
        assertTrue(recovered != null, "State recovered from backup must not be null");
        assertTrue(recovered.mission == 2, "State should rollback to valid backup state1");
        assertTrue(corrupt.exists(), "Corrupted primary file must be preserved");
    }

    private static void testCorruptBothReport(File dir) throws IOException {
        cleanDirectory(dir);
        CampaignStore store = new CampaignStore(dir);

        File primary = new File(dir, CampaignStore.FILE_NAME);
        File bak = new File(dir, CampaignStore.BACKUP_NAME);

        try (FileOutputStream fos = new FileOutputStream(primary)) {
            fos.write("corrupt content".getBytes(StandardCharsets.UTF_8));
        }
        try (FileOutputStream fos = new FileOutputStream(bak)) {
            fos.write("corrupt backup".getBytes(StandardCharsets.UTF_8));
        }

        boolean threw = false;
        try {
            store.load();
        } catch (RuntimeException ex) {
            threw = true;
        }
        assertTrue(threw, "Loading when both primary and backup are corrupt must throw exception");
    }

    private static void testInvalidRanges(File dir) {
        cleanDirectory(dir);
        CampaignStore store = new CampaignStore(dir);

        CampaignStore.State state = new CampaignStore.State();
        state.missiles = 4; // Vượt quá max 3
        assertValidationFails(state, "Missiles > 3");

        state = new CampaignStore.State();
        state.missiles = -1;
        assertValidationFails(state, "Missiles < 0");

        state = new CampaignStore.State();
        state.lives = 0;
        assertValidationFails(state, "Lives < 1");

        state = new CampaignStore.State();
        state.lives = 6;
        assertValidationFails(state, "Lives > 5");

        state = new CampaignStore.State();
        state.mission = 9;
        assertValidationFails(state, "Mission > 8");

        state = new CampaignStore.State();
        state.weaponLevels[0] = 0; // Vũ khí chính phải >= 1
        assertValidationFails(state, "Primary weapon < 1");

        state = new CampaignStore.State();
        state.cosmetic = "   ";
        assertValidationFails(state, "Blank cosmetic");

        state = new CampaignStore.State();
        state.musicVolume = 1.2f;
        assertValidationFails(state, "MusicVolume > 1.0");

        boolean saveBlocked = false;
        try {
            state.missiles = 5;
            store.save(state);
        } catch (Exception ex) {
            saveBlocked = true;
        }
        assertTrue(saveBlocked, "Save with invalid state must be rejected before disk operations");
    }

    private static void testNewerSchemaRefused(File dir) throws IOException {
        cleanDirectory(dir);
        CampaignStore store = new CampaignStore(dir);

        File primary = new File(dir, CampaignStore.FILE_NAME);
        String futureJson = "{\"version\":2,\"mission\":1,\"encounter\":0,\"lives\":3,\"missiles\":1,"
                + "\"activeWeapon\":0,\"weaponLevels\":[1,0,0],\"shard\":0,\"score\":0,\"unlockedMission\":1,"
                + "\"missionCleared\":false,\"completed\":false,\"bestScores\":[0,0,0,0,0,0,0,0],"
                + "\"rewardedMissions\":[false,false,false,false,false,false,false,false],\"cosmetic\":\"default\","
                + "\"musicVolume\":0.65,\"sfxVolume\":0.8,\"reducedMotion\":false}";
        try (FileOutputStream fos = new FileOutputStream(primary)) {
            fos.write(futureJson.getBytes(StandardCharsets.UTF_8));
        }

        boolean loadBlocked = false;
        try {
            store.load();
        } catch (IllegalStateException e) {
            loadBlocked = true;
        }
        assertTrue(loadBlocked, "Loading newer schema version must be refused immediately");

        boolean saveBlocked = false;
        try {
            CampaignStore.State validState = new CampaignStore.State();
            store.save(validState);
        } catch (IllegalStateException e) {
            saveBlocked = true;
        }
        assertTrue(saveBlocked, "Overwriting newer schema save file must be blocked");
    }

    private static void testEarlierBackupNotReplacedByCorruptPrimary(File dir) throws IOException {
        cleanDirectory(dir);
        CampaignStore store = new CampaignStore(dir);

        // Save 1: Lưu trạng thái hợp lệ A
        CampaignStore.State stateA = new CampaignStore.State();
        stateA.mission = 1;
        stateA.score = 111;
        store.save(stateA);

        // Save 2: Lưu trạng thái hợp lệ B -> stateA trở thành backup
        CampaignStore.State stateB = new CampaignStore.State();
        stateB.mission = 2;
        stateB.score = 222;
        store.save(stateB);

        // Làm hỏng file primary
        File primary = new File(dir, CampaignStore.FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(primary)) {
            fos.write("CORRUPTED_PRIMARY_DATA".getBytes(StandardCharsets.UTF_8));
        }

        // Lưu trạng thái hợp lệ C. Primary hỏng không được phép ghi đè lên backup A
        CampaignStore.State stateC = new CampaignStore.State();
        stateC.mission = 3;
        stateC.score = 333;
        store.save(stateC);

        // Primary mới bây giờ là stateC
        CampaignStore.State loaded = store.load();
        assertTrue(loaded.score == 333, "Primary should now reflect stateC");

        // Giả lập làm hỏng primary lần nữa để xác nhận backup vẫn là stateA chứ không phải rác
        try (FileOutputStream fos = new FileOutputStream(primary)) {
            fos.write("CORRUPT_AGAIN".getBytes(StandardCharsets.UTF_8));
        }
        CampaignStore.State backupRecovered = store.load();
        assertTrue(backupRecovered.score == 111, "Backup must remain stateA, not overwritten by corrupted primary");
    }

    private static void assertValidationFails(CampaignStore.State state, String caseName) {
        boolean failed = false;
        try {
            CampaignStore.validate(state);
        } catch (Exception ex) {
            failed = true;
        }
        assertTrue(failed, "Validation should fail for case: " + caseName);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("ASSERTION FAILED: " + message);
        }
    }

    private static void cleanDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
