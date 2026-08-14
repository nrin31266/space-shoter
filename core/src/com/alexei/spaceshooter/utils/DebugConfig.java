package com.alexei.spaceshooter.utils;

/**
 * Cấu hình Debug / Testing tập trung cho Game.
 * Để test game, mở duy nhất file này và bật ENABLE_DEBUG = true!
 */
public class DebugConfig {

    /** 
     * Bật/tắt chế độ Test Debug. 
     * - true: Áp dụng các thông số debug bên dưới (Wave, HP, Weapon Level/Type, Test Enemy).
     * - false: Chạy game bình thường.
     */
    public static final boolean ENABLE_DEBUG = false;

    /** Wave bắt đầu test khi chọn NEW GAME (từ 1 đến 20, VD: 16 hoặc 20) */
    public static final int DEBUG_START_WAVE = 20;

    /** Vòng Loop bắt đầu test khi chọn NEW GAME (0 = Loop 1 [Wave 1-20], 1 = Loop 2 [Wave 21-40], 2 = Loop 3 [Wave 41-60]...) */
    public static final int DEBUG_START_WAVE_LOOP_COUNT = 0;

    /** Lượng máu khởi đầu khi test (VD: 10f, tối đa maxLife là 10f) */
    public static final float DEBUG_START_HP = 3f;

    /** Loại đạn khởi đầu khi test (0=Plasma, 1=Explosive, 2=Homing) */
    public static final int DEBUG_START_WEAPON_TYPE = 0;

    /** Level đạn khởi đầu khi test (từ 1 đến 7, VD: 7) */
    public static final int DEBUG_START_WEAPON_LEVEL = 7;

    // ── Single Enemy Playtest Mode (Chế độ test quan sát riêng quái mới) ──

    /** Bật/Tắt chế độ spawn duy nhất 1 đợt quái test riêng */
    public static final boolean DEBUG_TEST_SINGLE_ENEMY = false;

    /** Tên loại quái cần test riêng ("EnemyShipE" hoặc "EnemyShipF") */
    public static final String  DEBUG_TEST_ENEMY_TYPE   = "EnemyShipE";

    /** Số lượng quái test (3 - 5 con) */
    public static final int     DEBUG_TEST_ENEMY_COUNT  = 3;

    /** Hệ số nhân HP quái test (VD: 5.0f để quái sống lâu quan sát pattern) */
    public static final float   DEBUG_TEST_HP_MULTIPLIER = 5.0f;

    // ── Drop Rate Config (Tỉ lệ rơi vật phẩm khi diệt quái) ──

    /** Tỉ lệ rơi Item Nâng Cấp Vũ Khí (Mặc định 0.15f = 15%) */
    public static float DROP_RATE_WEAPON_UPGRADE = 0.05f;

    /** Tỉ lệ rơi Item Máu (Mặc định 0.05f = 5%) */
    public static float DROP_RATE_HP = 0.05f;

    /** Tỉ lệ rơi Sao (Mặc định 0.90f = 90%) */
    public static float DROP_RATE_STAR = 0.90f;
}
