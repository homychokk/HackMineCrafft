package com.liquidglass.render;

import com.liquidglass.module.Module;
import com.liquidglass.module.ModuleManager;
import com.liquidglass.module.modules.XRay;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/** Светящиеся цветные боксы вокруг руд (видно сквозь блоки). */
public final class OreEspRenderer {
    private OreEspRenderer() {}

    // Кэш найденных руд: {x, y, z, baseColor}
    private static final List<int[]> CACHE = new ArrayList<>();
    private static long lastScan = 0;
    private static int lastRadius = 0;

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(OreEspRenderer::onRender);
    }

    private static void onRender(WorldRenderContext context) {
        Module m = ModuleManager.get("XRay");
        if (m == null || !m.isEnabled()) return;
        XRay xray = (XRay) m;
        if (!xray.glow.value) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        int radius = (int) xray.radius.value;
        long now = System.currentTimeMillis();
        if (now - lastScan > 700 || radius != lastRadius) {
            scan(mc, radius);
            lastScan = now;
            lastRadius = radius;
        }
        if (CACHE.isEmpty()) return;

        MatrixStack ms = context.matrixStack();
        if (ms == null) return;
        Vec3d cam = context.camera().getPos();
        float time = now / 1000f;

        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = ms.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.lineWidth(2.0f);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        String colMode = xray.colors.get();

        // Заливка
        if (xray.fill.value) {
            buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            for (int[] o : CACHE) {
                int col = colorFor(o[3], o, time, colMode);
                fillBox(buf, mat, o[0], o[1], o[2], col, 0.22f);
            }
            tess.draw();
        }

        // Контур (яркий, "жёсткое" свечение)
        buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (int[] o : CACHE) {
            int col = colorFor(o[3], o, time, colMode);
            outlineBox(buf, mat, o[0], o[1], o[2], col, 1.0f);
        }
        tess.draw();

        ms.pop();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1f);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void scan(MinecraftClient mc, int radius) {
        CACHE.clear();
        BlockPos center = mc.player.getBlockPos();
        BlockPos.Mutable p = new BlockPos.Mutable();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    p.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    Block b = mc.world.getBlockState(p).getBlock();
                    boolean shouldGlow = false;
                    String espMode = ((XRay) ModuleManager.get("XRay")).espMode.get();
                    if ("Только ценные".equals(espMode)) {
                        shouldGlow = XRayBlocks.isValuable(b);
                    } else {
                        shouldGlow = XRayBlocks.isOre(b);
                    }

                    if (shouldGlow) {
                        CACHE.add(new int[]{p.getX(), p.getY(), p.getZ(), baseColor(b)});
                    }
                }
            }
        }
    }

    private static int colorFor(int base, int[] o, float time, String mode) {
        return switch (mode) {
            case "Радуга" -> hsv(((o[0] * 13 + o[1] * 7 + o[2] * 3) % 360) / 360f + time * 0.15f, 0.85f, 1f);
            case "Один цвет" -> 0xFF38E0BE;
            default -> base;
        };
    }

    private static int baseColor(Block b) {
        if (b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE) return 0xFF4DF0E6;
        if (b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE) return 0xFF32E06E;
        if (b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE || b == Blocks.NETHER_GOLD_ORE) return 0xFFFCEE4B;
        if (b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE) return 0xFFE8C0A0;
        if (b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE) return 0xFFE0795A;
        if (b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE) return 0xFFFF3030;
        if (b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE) return 0xFF2A6BFF;
        if (b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE) return 0xFF707070;
        if (b == Blocks.NETHER_QUARTZ_ORE) return 0xFFF0E6DC;
        if (b == Blocks.ANCIENT_DEBRIS) return 0xFF9B5BFF;
        return 0xFFFFFFFF;
    }

    private static int hsv(float h, float s, float v) {
        h = (h % 1f + 1f) % 1f;
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float pp = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        float r, g, b;
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = pp; }
            case 1 -> { r = q; g = v; b = pp; }
            case 2 -> { r = pp; g = v; b = t; }
            case 3 -> { r = pp; g = q; b = v; }
            case 4 -> { r = t; g = pp; b = v; }
            default -> { r = v; g = pp; b = q; }
        }
        return 0xFF000000 | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    private static void color(BufferBuilder buf, Matrix4f m, float x, float y, float z, int col, float a) {
        float r = ((col >> 16) & 0xFF) / 255f;
        float g = ((col >> 8) & 0xFF) / 255f;
        float b = (col & 0xFF) / 255f;
        buf.vertex(m, x, y, z).color(r, g, b, a).next();
    }

    private static void fillBox(BufferBuilder buf, Matrix4f m, int bx, int by, int bz, int col, float a) {
        float x1 = bx, y1 = by, z1 = bz, x2 = bx + 1f, y2 = by + 1f, z2 = bz + 1f;
        // 6 граней (QUADS)
        color(buf, m, x1, y1, z1, col, a); color(buf, m, x2, y1, z1, col, a); color(buf, m, x2, y2, z1, col, a); color(buf, m, x1, y2, z1, col, a);
        color(buf, m, x1, y1, z2, col, a); color(buf, m, x1, y2, z2, col, a); color(buf, m, x2, y2, z2, col, a); color(buf, m, x2, y1, z2, col, a);
        color(buf, m, x1, y1, z1, col, a); color(buf, m, x1, y2, z1, col, a); color(buf, m, x1, y2, z2, col, a); color(buf, m, x1, y1, z2, col, a);
        color(buf, m, x2, y1, z1, col, a); color(buf, m, x2, y1, z2, col, a); color(buf, m, x2, y2, z2, col, a); color(buf, m, x2, y2, z1, col, a);
        color(buf, m, x1, y2, z1, col, a); color(buf, m, x2, y2, z1, col, a); color(buf, m, x2, y2, z2, col, a); color(buf, m, x1, y2, z2, col, a);
        color(buf, m, x1, y1, z1, col, a); color(buf, m, x1, y1, z2, col, a); color(buf, m, x2, y1, z2, col, a); color(buf, m, x2, y1, z1, col, a);
    }

    private static void outlineBox(BufferBuilder buf, Matrix4f m, int bx, int by, int bz, int col, float a) {
        float x1 = bx, y1 = by, z1 = bz, x2 = bx + 1f, y2 = by + 1f, z2 = bz + 1f;
        edge(buf, m, x1, y1, z1, x2, y1, z1, col, a); edge(buf, m, x2, y1, z1, x2, y1, z2, col, a);
        edge(buf, m, x2, y1, z2, x1, y1, z2, col, a); edge(buf, m, x1, y1, z2, x1, y1, z1, col, a);
        edge(buf, m, x1, y2, z1, x2, y2, z1, col, a); edge(buf, m, x2, y2, z1, x2, y2, z2, col, a);
        edge(buf, m, x2, y2, z2, x1, y2, z2, col, a); edge(buf, m, x1, y2, z2, x1, y2, z1, col, a);
        edge(buf, m, x1, y1, z1, x1, y2, z1, col, a); edge(buf, m, x2, y1, z1, x2, y2, z1, col, a);
        edge(buf, m, x2, y1, z2, x2, y2, z2, col, a); edge(buf, m, x1, y1, z2, x1, y2, z2, col, a);
    }

    private static void edge(BufferBuilder buf, Matrix4f m, float x1, float y1, float z1,
                             float x2, float y2, float z2, int col, float a) {
        color(buf, m, x1, y1, z1, col, a);
        color(buf, m, x2, y2, z2, col, a);
    }
}
