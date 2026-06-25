package com.liquidglass.gui;

/**
 * Цветовая палитра меню в стиле liquid glass (тёмное матовое стекло + неоновый акцент).
 * Формат цветов — ARGB (0xAARRGGBB).
 */
public final class Theme {
    private Theme() {}

    // Затемнение фона игры
    public static final int SCREEN_DIM = 0x880A0710;

    // Основная стеклянная панель (полупрозрачная, фон игры просвечивает)
    public static final int GLASS_TOP = 0xC5141322;
    public static final int GLASS_BOTTOM = 0xC50C0A16;

    // Сайдбар
    public static final int SIDEBAR_TOP = 0xBE17142A;
    public static final int SIDEBAR_BOTTOM = 0xBE0B0918;

    // Блик стекла (светлая полоса сверху)
    public static final int GLASS_HIGHLIGHT = 0x2EFFFFFF;
    // Обводка
    public static final int BORDER = 0x4AFFFFFF;
    public static final int BORDER_SOFT = 0x22FFFFFF;

    // Акцент (неоновый градиент: мята -> розовый)
    public static final int ACCENT_A = 0xFF38E0BE; // мятный
    public static final int ACCENT_B = 0xFFFF5C9D; // розовый

    // Текст
    public static final int TEXT = 0xFFF4F5FB;
    public static final int TEXT_DIM = 0xFFA2A7BC;
    public static final int TEXT_FAINT = 0xFF626780;

    // Строки модулей
    public static final int ROW_HOVER = 0x22FFFFFF;
    public static final int ROW_ACTIVE = 0x2A38E0BE;

    // Тень
    public static final int SHADOW = 0x66000000;

    /** Смешивает два ARGB цвета по коэффициенту t (0..1). */
    public static int mix(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int na = (int) (aa + (ba - aa) * t);
        int nr = (int) (ar + (br - ar) * t);
        int ng = (int) (ag + (bg - ag) * t);
        int nb = (int) (ab + (bb - ab) * t);
        return (na << 24) | (nr << 16) | (ng << 8) | nb;
    }

    /** Умножает альфа-канал цвета на коэффициент (0..1). */
    public static int withAlpha(int color, float factor) {
        int a = (int) (((color >> 24) & 0xFF) * Math.max(0f, Math.min(1f, factor)));
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
