package com.alexei.spaceshooter.manager;

public final class RewardRulesCheck {
    public static void main(String[] args) {
        RewardRules.SourceRandom random = new RewardRules.SourceRandom(1);
        assert random.sample() == 534011718;
        assert random.sample() == 237820880;
        assert random.sample() == 1002897798;
        testInvalidRanges();
        testQuotaDeadline();
        testQuotaEscapeAndFallback();
    }

    // Xác thực chặn range tham số không hợp lệ từ dữ liệu wave
    private static void testInvalidRanges() {
        RewardRules rules = new RewardRules(null, null, 1337L);
        assertThrows(() -> rules.beginQuota(-1, 0, 0));
        assertThrows(() -> rules.beginQuota(5, 6, 0));
        assertThrows(() -> rules.beginQuota(5, 2, -1));
        assertThrows(() -> rules.beginQuota(5, 2, 6));
        assertThrows(() -> rules.beginQuota(5, 0, 1));
    }

    // Đảm bảo deadline ép rơi reward đúng mốc kill đầu tiên đã cấu hình
    private static void testQuotaDeadline() {
        RewardRules rules = new RewardRules(null, null, 42L);
        rules.beginQuota(10, 2, 3);
        assert rules.remaining() == 2 : "Quota khoi tao phai bang 2";

        boolean drop1 = rules.resolveKill(false);
        boolean drop2 = rules.resolveKill(false);
        boolean drop3 = rules.resolveKill(false);

        assert drop3 || rules.remaining() < 2 : "Moc deadline kill 3 phai kich hoat hoac da roi truoc do";
        assert rules.remaining() <= 1 : "Sau deadline quota phai giam";
    }

    // Đảm bảo kẻ địch thoát không kích hoạt rơi đồ nhưng fallback vét cạn quota còn thiếu
    private static void testQuotaEscapeAndFallback() {
        RewardRules rules = new RewardRules(null, null, 999L);
        rules.beginQuota(5, 2, 0);

        for (int i = 0; i < 5; i++) {
            rules.resolveEscape();
        }
        assert !rules.resolveKill(false) : "Khong con quai thi khong the resolve kill thanh cong";
        assert rules.remaining() == 2 : "Quai thoat khong tru quota authored";

        assert rules.resolveFallback() : "Fallback dau tien phai thanh cong";
        assert rules.remaining() == 1 : "Quota con 1";
        assert rules.resolveFallback() : "Fallback thu hai phai thanh cong";
        assert rules.remaining() == 0 : "Quota da vet het";
        assert !rules.resolveFallback() : "Fallback khi quota = 0 phai tra ve false";
    }

    private static void assertThrows(Runnable runnable) {
        try {
            runnable.run();
            throw new AssertionError("Mong doi IllegalArgumentException nhung khong bat duoc");
        } catch (IllegalArgumentException expected) {
            // Hop le
        }
    }
}
