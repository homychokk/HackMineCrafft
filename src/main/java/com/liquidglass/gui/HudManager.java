package com.liquidglass.gui;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.ModuleManager;
import com.liquidglass.module.modules.FreeCam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Внутриигровой HUD (рисуется поверх экрана, независимо от меню). */
public final class HudManager {
    private HudManager() {}

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null || mc.options.hudHidden) return;

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        var tr = mc.textRenderer;
        float time = System.nanoTime() / 1_000_000_000f;

        int y = 4;
        if (ModuleManager.isEnabled("Watermark")) {
            ctx.getMatrices().push();
            ctx.getMatrices().translate(5, 4, 0);
            ctx.getMatrices().scale(1.25f, 1.25f, 1f);
            ctx.drawText(tr, Fonts.t("Liquid"), 0, 0, Theme.TEXT, true);
            int lw = tr.getWidth(Fonts.t("Liquid "));
            float flow = (float) (Math.sin(time * 1.4f) * 0.5f + 0.5f);
            ctx.drawText(tr, Fonts.t("Glass"), lw, 0, Theme.mix(Theme.ACCENT_A, Theme.ACCENT_B, flow), true);
            ctx.getMatrices().pop();
            y = 20;
        }

        if (ModuleManager.isEnabled("FPS")) {
            ctx.drawText(tr, Fonts.t(mc.getCurrentFps() + " fps"), 6, y, Theme.TEXT_DIM, true);
            y += 11;
        }

        if (ModuleManager.isEnabled("Coords")) {
            String c = String.format("XYZ: %.1f, %.1f, %.1f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ());
            ctx.drawText(tr, Fonts.t(c), 6, y, Theme.TEXT_DIM, true);
            y += 11;
        }

        if (ModuleManager.isEnabled("PotionHUD")) {
            for (StatusEffectInstance eff : mc.player.getStatusEffects()) {
                String name = eff.getEffectType().getName().getString();
                int amp = eff.getAmplifier() + 1;
                int secs = eff.getDuration() / 20;
                String line = name + " " + amp + "  " + (secs / 60) + ":" + String.format("%02d", secs % 60);
                ctx.drawText(tr, Fonts.t(line), 6, y, 0xFFB8E0FF, true);
                y += 11;
            }
        }

        if (ModuleManager.isEnabled("ArrayList")) {
            List<Module> active = new ArrayList<>();
            for (Module m : ModuleManager.getModules()) {
                if (m.isEnabled() && m.getCategory() != Category.HUD) active.add(m);
            }
            active.sort((a, b) -> tr.getWidth(Fonts.t(b.getName())) - tr.getWidth(Fonts.t(a.getName())));

            int ly = 4;
            for (int i = 0; i < active.size(); i++) {
                String name = active.get(i).getName();
                int w = tr.getWidth(Fonts.t(name));
                int x = sw - w - 8;
                float t = (float) i / Math.max(1, active.size());
                int accent = Theme.mix(Theme.ACCENT_A, Theme.ACCENT_B, t);

                RenderUtil.roundedRect(ctx, x - 5, ly - 1, w + 9, 12, 3f, 0x96000000);
                RenderUtil.roundedRect(ctx, sw - 2, ly - 1, 2, 12, 1f, accent);
                ctx.drawText(tr, Fonts.t(name), x, ly + 1, accent, true);
                ly += 13;
            }
        }

        if (ModuleManager.isEnabled("Keystrokes")) {
            renderKeystrokes(ctx, mc, 14, sh - 92);
        }

        // FreeCam: тултип содержимого сундука под прицелом
        if (FreeCam.active && FreeCam.chestPreview != null) {
            renderChestTooltip(ctx, mc, sw, sh);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Тултип содержимого сундука (FreeCam)
    // ─────────────────────────────────────────────────────────

    // Столбцов в строке
    private static final int COLS = 9;
    // Размер одной ячейки (иконка 16 + отступ)
    private static final int CELL = 18;
    // Отступ внутри тултипа
    private static final int PAD = 8;

    private static void renderChestTooltip(DrawContext ctx, MinecraftClient mc, int sw, int sh) {
        List<ItemStack> items = FreeCam.chestPreview;
        String label = FreeCam.chestLabel != null ? FreeCam.chestLabel : "Сундук";

        // Координаты сундука в строку
        String posStr = FreeCam.chestPos != null
                ? FreeCam.chestPos.getX() + ", " + FreeCam.chestPos.getY() + ", " + FreeCam.chestPos.getZ()
                : "";

        // Считаем количество строк сетки (ровно столько, сколько слотов)
        int rows = Math.max(1, (int) Math.ceil((double) items.size() / COLS));

        int gridW = COLS * CELL;
        int gridH = rows * CELL;

        // Заголовок
        var tr = mc.textRenderer;
        int labelW = tr.getWidth(Fonts.t(label));
        int posW = tr.getWidth(Fonts.t(posStr));
        int headerW = Math.max(labelW, posW);

        int panelW = Math.max(gridW, headerW) + PAD * 2;
        // Высота: заголовок (9px) + координаты (8px) + разделитель (6px) + сетка + отступы
        int headerH = 9 + (posStr.isEmpty() ? 0 : 8) + 6;
        int panelH = PAD + headerH + gridH + PAD;

        // Позиционируем по центру-верху экрана, чуть ниже прицела (y = 20% от высоты)
        int px = (sw - panelW) / 2;
        int py = sh / 5;

        // Фон тултипа
        RenderUtil.dropShadow(ctx, px, py, panelW, panelH, 10f, 0x55000000);
        RenderUtil.roundedGradient(ctx, px, py, panelW, panelH, 10f,
                0xD4141322, 0xD40C0A16);
        // Верхняя акцентная полоска
        RenderUtil.roundedGradient(ctx, px, py, panelW, 2f, 1f,
                Theme.withAlpha(Theme.ACCENT_A, 0.9f), Theme.withAlpha(Theme.ACCENT_B, 0.9f));
        // Внешняя рамка
        RenderUtil.roundedOutline(ctx, px, py, panelW, panelH, 10f, 1f, 0x4AFFFFFF);

        int textX = px + PAD;
        int textY = py + PAD;

        // Название контейнера
        ctx.drawText(tr, Fonts.t(label), textX, textY, Theme.TEXT, true);
        textY += 10;

        // Координаты (мелким текстом)
        if (!posStr.isEmpty()) {
            ctx.drawText(tr, Fonts.t(posStr), textX, textY, Theme.TEXT_FAINT, false);
            textY += 9;
        }

        // Горизонтальный разделитель
        textY += 2;
        RenderUtil.roundedRect(ctx, px + PAD, textY, panelW - PAD * 2, 1f, 0f,
                0x33FFFFFF);
        textY += 4;

        // Подсказка про ПКМ
        long nonEmpty = items.stream().filter(s -> s != null && !s.isEmpty()).count();
        String hint = nonEmpty == 0 ? "Пусто • ПКМ — открыть" : "ПКМ — открыть • Предметов: " + nonEmpty;
        ctx.drawText(tr, Fonts.t(hint), textX, textY, Theme.TEXT_FAINT, false);
        textY += 10;

        // ── Сетка предметов ──────────────────────────────────
        int startX = px + PAD;
        for (int i = 0; i < rows * COLS; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cellX = startX + col * CELL;
            int cellY = textY + row * CELL;

            // Фон ячейки
            RenderUtil.roundedRect(ctx, cellX, cellY, CELL - 1, CELL - 1, 3f,
                    0x66000000);

            // Предмет
            if (i < items.size()) {
                ItemStack stack = items.get(i);
                // Рисуем иконку предмета (встроенный метод DrawContext)
                ctx.drawItem(stack, cellX + 1, cellY + 1);
                // Количество (встроенный метод)
                ctx.drawItemInSlot(tr, stack, cellX + 1, cellY + 1);
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Клавиши
    // ─────────────────────────────────────────────────────────

    private static void renderKeystrokes(DrawContext ctx, MinecraftClient mc, int x, int y) {
        int s = 16, gap = 2;
        key(ctx, mc, x + s + gap, y, s, s, "W", mc.options.forwardKey);
        key(ctx, mc, x, y + s + gap, s, s, "A", mc.options.leftKey);
        key(ctx, mc, x + s + gap, y + s + gap, s, s, "S", mc.options.backKey);
        key(ctx, mc, x + (s + gap) * 2, y + s + gap, s, s, "D", mc.options.rightKey);
        int wide = s * 3 + gap * 2;
        key(ctx, mc, x, y + (s + gap) * 2, wide, 12, "SPACE", mc.options.jumpKey);
    }

    private static void key(DrawContext ctx, MinecraftClient mc, int x, int y, int w, int h, String label, KeyBinding bind) {
        boolean pressed = bind.isPressed();
        int bg = pressed ? Theme.withAlpha(Theme.ACCENT_A, 0.85f) : 0x80000000;
        int fg = pressed ? 0xFFFFFFFF : Theme.TEXT_DIM;
        RenderUtil.roundedRect(ctx, x, y, w, h, 4f, bg);
        int tw = mc.textRenderer.getWidth(Fonts.t(label));
        ctx.drawText(mc.textRenderer, Fonts.t(label), x + (w - tw) / 2, y + (h - 8) / 2, fg, false);
    }
}
