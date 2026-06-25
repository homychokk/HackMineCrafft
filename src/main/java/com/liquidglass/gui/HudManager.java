package com.liquidglass.gui;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.effect.StatusEffectInstance;

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
    }

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
