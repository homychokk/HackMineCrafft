package com.liquidglass.gui;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Кастомный шрифт меню (PT Sans, лежит в ресурсах мода).
 * Используется через Style.withFont — обычный textRenderer его отрисует.
 */
public final class Fonts {
    public static final Identifier GUI = new Identifier("liquidglass", "gui");

    private Fonts() {}

    public static Text t(String s) {
        return Text.literal(s).setStyle(Style.EMPTY.withFont(GUI));
    }
}
