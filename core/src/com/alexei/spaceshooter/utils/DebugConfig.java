package com.alexei.spaceshooter.utils;

/**
 * Cấu hình Debug / Testing tập trung cho Game.
 * Để test game, người dùng chỉ cần mở duy nhất file này và bật ENABLE_DEBUG = true!
 */
public class DebugConfig {

    /** 
     * Bật/tắt chế độ Test Debug. 
     * - true: Áp dụng các thông số debug bên dưới (Wave, HP, Weapon Level).
     * - false: Chạy game bình thường (Wave 1, 5 HP, Weapon Level 1).
     */
    public static final boolean ENABLE_DEBUG = true;

    /** Wave bắt đầu test khi chọn NEW GAME (VD: 15 để test boss wave 15) */
    public static final int DEBUG_START_WAVE = 15;

    /** Lượng máu khởi đầu khi test (VD: 10f, tối đa maxLife là 10f) */
    public static final float DEBUG_START_HP = 10f;

    /** Level đạn khởi đầu khi test (từ 1 đến 5, VD: 5) */
    public static final int DEBUG_START_WEAPON_LEVEL = 5;

}
