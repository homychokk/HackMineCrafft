package com.liquidglass.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Кастомный рендер примитивов (без текстур майнкрафта):
 * скруглённые прямоугольники, вертикальные градиенты и мягкие тени.
 */
public final class RenderUtil {
    private RenderUtil() {}

    public static void roundedRect(DrawContext ctx, float x, float y, float w, float h, float radius, int color) {
        roundedGradient(ctx, x, y, w, h, radius, color, color);
    }

    public static void roundedGradient(DrawContext ctx, float x, float y, float w, float h, float radius, int topColor, int bottomColor) {
        if (w <= 0 || h <= 0) return;
        radius = Math.max(0f, Math.min(radius, Math.min(w, h) / 2f));

        Matrix4f matrix = ctx.getMatrices().peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                com.mojang.blaze3d.platform.GlStateManager.SrcFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.SrcFactor.ONE,
                com.mojang.blaze3d.platform.GlStateManager.DstFactor.ZERO);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        List<float[]> points = new ArrayList<>();
        if (radius < 0.5f) {
            points.add(new float[]{x, y});
            points.add(new float[]{x + w, y});
            points.add(new float[]{x + w, y + h});
            points.add(new float[]{x, y + h});
        } else {
            int seg = Math.max(8, Math.min(22, (int) (radius * 1.6f)));
            addArc(points, x + w - radius, y + radius, radius, -90, 0, seg);     // правый верх
            addArc(points, x + w - radius, y + h - radius, radius, 0, 90, seg);  // правый низ
            addArc(points, x + radius, y + h - radius, radius, 90, 180, seg);    // левый низ
            addArc(points, x + radius, y + radius, radius, 180, 270, seg);       // левый верх
        }

        float cx = x + w / 2f;
        float cy = y + h / 2f;
        vertex(buffer, matrix, cx, cy, colorAtY(cy, y, h, topColor, bottomColor));
        for (float[] p : points) {
            vertex(buffer, matrix, p[0], p[1], colorAtY(p[1], y, h, topColor, bottomColor));
        }
        float[] first = points.get(0);
        vertex(buffer, matrix, first[0], first[1], colorAtY(first[1], y, h, topColor, bottomColor));

        tessellator.draw();

        // Полностью возвращаем состояние к дефолту GUI, чтобы ничего не утекало в мир
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    /** Контур скруглённого прямоугольника. */
    public static void roundedOutline(DrawContext ctx, float x, float y, float w, float h, float radius, float thickness, int color) {
        // Внешний скруглённый прямоугольник минус внутренний = аккуратная рамка без угловых квадратов
        roundedRect(ctx, x, y, w, h, radius, color);
        int innerAlpha = (color >> 24) & 0xFF;
        if (innerAlpha > 0 && w > thickness * 2 && h > thickness * 2) {
            // Вырезаем центр прозрачным (рисуем поверх то, что было под рамкой)
            // Для простоты используем 4 полосы с правильными скруглёнными концами
            roundedRect(ctx, x + thickness, y + thickness,
                    w - thickness * 2, h - thickness * 2,
                    Math.max(0, radius - thickness), 0x00000000);
        }
    }

    /** Мягкая тень под панелью (несколько расширяющихся слоёв). */
    public static void dropShadow(DrawContext ctx, float x, float y, float w, float h, float radius, int baseColor) {
        int layers = 9;
        int baseAlpha = (baseColor >> 24) & 0xFF;
        for (int i = layers; i >= 1; i--) {
            float spread = i * 2.4f;
            float a = baseAlpha * (1f - (float) i / (layers + 1)) / layers;
            int col = ((int) Math.max(0, a) << 24) | (baseColor & 0x00FFFFFF);
            roundedRect(ctx, x - spread, y - spread + 2f, w + spread * 2f, h + spread * 2f, radius + spread, col);
        }
    }

    private static void addArc(List<float[]> points, float cx, float cy, float r, float startDeg, float endDeg, int segments) {
        for (int i = 0; i <= segments; i++) {
            double ang = Math.toRadians(startDeg + (endDeg - startDeg) * ((double) i / segments));
            points.add(new float[]{cx + (float) Math.cos(ang) * r, cy + (float) Math.sin(ang) * r});
        }
    }

    private static int colorAtY(float py, float y, float h, int topColor, int bottomColor) {
        if (topColor == bottomColor) return topColor;
        float t = h == 0 ? 0 : (py - y) / h;
        return Theme.mix(topColor, bottomColor, t);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        buffer.vertex(matrix, x, y, 0f).color(r, g, b, a).next();
    }
}
