package com.alexei.spaceshooter.screen;

public final class UiFeedbackCheck {
    public static void run() {
        StringBuilder calls = new StringBuilder();
        for (String name : new String[]{"NHIỆM VỤ", "CÀI ĐẶT", "ĐẠN GIẢ", "QUAY LẠI", "HỦY", "MÀN HÌNH CHÍNH"}) {
            calls.setLength(0);
            UiFeedback.activate(name, true, cue -> calls.append(cue).append(':'), () -> calls.append("action"));
            assert calls.toString().equals((name.equals("QUAY LẠI") || name.equals("HỦY") || name.equals("MÀN HÌNH CHÍNH") ? "33" : "32") + ":action") : calls;
        }
        calls.setLength(0);
        UiFeedback.activate("ĐÃ KHÓA", false, cue -> calls.append(cue), () -> calls.append("wrong"));
        assert calls.length() == 0;
        UiFeedback.activate("BẤT TỬ", true, cue -> calls.append(cue), null);
        assert calls.toString().equals("32") : "Toggle has feedback even without navigation";
        System.out.println("UI feedback: Confirm/Back before action, disabled silent, toggle/keyboard shared dispatch OK");
    }
}
