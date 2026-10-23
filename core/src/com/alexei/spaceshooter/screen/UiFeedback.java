package com.alexei.spaceshooter.screen;

import java.util.function.IntConsumer;

/** Phát trước điều hướng để click không bị mất khi screen cũ được dispose. */
final class UiFeedback {
    static void activate(String text, boolean enabled, IntConsumer audio, Runnable action) {
        if (!enabled) return;
        audio.accept(text.startsWith("QUAY LẠI") || text.startsWith("HỦY") || text.startsWith("VỀ MENU") || text.equals("MÀN HÌNH CHÍNH") ? 33 : 32);
        if (action != null) action.run();
    }
}
