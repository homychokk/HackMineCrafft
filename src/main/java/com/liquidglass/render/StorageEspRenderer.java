package com.liquidglass.render;

import com.liquidglass.module.Module;
import com.liquidglass.module.ModuleManager;
import com.liquidglass.module.modules.StorageESP;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
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

/** Боксы вокруг сундуков/шалкеров/эндеров и спавнеров. */
public final class StorageEspRenderer {
    private StorageEspRenderer() {}

    private static final List<int[]> CACHE = new ArrayList<>(); // {x,y,z,color}
    private static long lastScan = 0;

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(StorageEspRenderer::onRender);
    }

    private static void onRender(WorldRenderContext context) {
        Module m = ModuleManager.get("StorageESP");
        if (m == null || !m.isEnabled()) return;
        StorageESP s = (StorageESP) m;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastScan > 700) {
            scan(mc, (int) s.radius.value, s);
            lastScan = now;
        }
        if (CACHE.isEmpty()) return;

        MatrixStack ms = context.matrixStack();
        if (ms == null) return;
        Vec3d cam = context.camera().getPos();

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

        if (s.fill.value) {
            buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            for (int[] o : CACHE) fillBox(buf, mat, o[0], o[1], o[2], o[3], 0.20f);
            tess.draw();
        }

        buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (int[] o : CACHE) outlineBox(buf, mat, o[0], o[1], o[2], o[3], 1.0f);
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

    private static void scan(MinecraftClient mc, int radius, StorageESP s) {
        CACHE.clear();
        BlockPos center = mc.player.getBlockPos();
        BlockPos.Mutable p = new BlockPos.Mutable();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    p.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    Block b = mc.world.getBlockState(p).getBlock();
                    int col = colorFor(b, s);
                    if (col != 0) CACHE.add(new int[]{p.getX(), p.getY(), p.getZ(), col});
                }
            }
        }
    }

    private static int colorFor(Block b, StorageESP s) {
        if (s.spawners.value && b == Blocks.SPAWNER) return 0xFFFF3030;       // данж — красный
        if (s.chests.value && (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST || b == Blocks.BARREL))
            return 0xFFFFB033;                                                 // лут — оранжевый
        if (s.chests.value && b == Blocks.ENDER_CHEST) return 0xFF35E0D0;      // эндер — бирюзовый
        if (s.shulkers.value && b instanceof ShulkerBoxBlock) return 0xFFB060FF; // шалкер — фиолетовый
        return 0;
    }

    private static void vert(BufferBuilder buf, Matrix4f m, float x, float y, float z, int col, float a) {
        float r = ((col >> 16) & 0xFF) / 255f, g = ((col >> 8) & 0xFF) / 255f, b = (col & 0xFF) / 255f;
        buf.vertex(m, x, y, z).color(r, g, b, a).next();
    }

    private static void fillBox(BufferBuilder buf, Matrix4f m, int bx, int by, int bz, int col, float a) {
        float x1 = bx, y1 = by, z1 = bz, x2 = bx + 1f, y2 = by + 1f, z2 = bz + 1f;
        vert(buf, m, x1, y1, z1, col, a); vert(buf, m, x2, y1, z1, col, a); vert(buf, m, x2, y2, z1, col, a); vert(buf, m, x1, y2, z1, col, a);
        vert(buf, m, x1, y1, z2, col, a); vert(buf, m, x1, y2, z2, col, a); vert(buf, m, x2, y2, z2, col, a); vert(buf, m, x2, y1, z2, col, a);
        vert(buf, m, x1, y1, z1, col, a); vert(buf, m, x1, y2, z1, col, a); vert(buf, m, x1, y2, z2, col, a); vert(buf, m, x1, y1, z2, col, a);
        vert(buf, m, x2, y1, z1, col, a); vert(buf, m, x2, y1, z2, col, a); vert(buf, m, x2, y2, z2, col, a); vert(buf, m, x2, y2, z1, col, a);
        vert(buf, m, x1, y2, z1, col, a); vert(buf, m, x2, y2, z1, col, a); vert(buf, m, x2, y2, z2, col, a); vert(buf, m, x1, y2, z2, col, a);
        vert(buf, m, x1, y1, z1, col, a); vert(buf, m, x1, y1, z2, col, a); vert(buf, m, x2, y1, z2, col, a); vert(buf, m, x2, y1, z1, col, a);
    }

    private static void outlineBox(BufferBuilder buf, Matrix4f m, int bx, int by, int bz, int col, float a) {
        float x1 = bx, y1 = by, z1 = bz, x2 = bx + 1f, y2 = by + 1f, z2 = bz + 1f;
        e(buf, m, x1, y1, z1, x2, y1, z1, col, a); e(buf, m, x2, y1, z1, x2, y1, z2, col, a);
        e(buf, m, x2, y1, z2, x1, y1, z2, col, a); e(buf, m, x1, y1, z2, x1, y1, z1, col, a);
        e(buf, m, x1, y2, z1, x2, y2, z1, col, a); e(buf, m, x2, y2, z1, x2, y2, z2, col, a);
        e(buf, m, x2, y2, z2, x1, y2, z2, col, a); e(buf, m, x1, y2, z2, x1, y2, z1, col, a);
        e(buf, m, x1, y1, z1, x1, y2, z1, col, a); e(buf, m, x2, y1, z1, x2, y2, z1, col, a);
        e(buf, m, x2, y1, z2, x2, y2, z2, col, a); e(buf, m, x1, y1, z2, x1, y2, z2, col, a);
    }

    private static void e(BufferBuilder buf, Matrix4f m, float x1, float y1, float z1,
                          float x2, float y2, float z2, int col, float a) {
        vert(buf, m, x1, y1, z1, col, a);
        vert(buf, m, x2, y2, z2, col, a);
    }
}
