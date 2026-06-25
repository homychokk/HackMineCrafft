package com.liquidglass.render;

import com.liquidglass.module.Module;
import com.liquidglass.module.ModuleManager;
import com.liquidglass.module.modules.Hitboxes;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** ESP (бокс вокруг сущностей) и Tracers (линии к ним) поверх мира. */
public final class EspRenderer {
    private EspRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EspRenderer::onRender);
    }

    private static void onRender(WorldRenderContext context) {
        boolean esp = ModuleManager.isEnabled("ESP");
        boolean tracers = ModuleManager.isEnabled("Tracers");
        Module hb = ModuleManager.get("Hitboxes");
        boolean hbShow = hb != null && hb.isEnabled() && ((Hitboxes) hb).show.value;
        double hbSize = hb != null ? ((Hitboxes) hb).size.value : 0.3;
        if (!esp && !tracers && !hbShow) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        MatrixStack ms = context.matrixStack();
        if (ms == null) return;
        Vec3d cam = context.camera().getPos();

        // Точка старта трейсеров — у низа экрана (перед камерой, чуть вниз)
        float cyaw = context.camera().getYaw();
        float cpitch = context.camera().getPitch();
        Vec3d look = Vec3d.fromPolar(cpitch, cyaw);
        Vec3d down = Vec3d.fromPolar(cpitch + 90f, cyaw);
        Vec3d start = cam.add(look.multiply(0.45)).add(down.multiply(0.35));

        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f m = ms.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.lineWidth(1.3f);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity le) || !le.isAlive()) continue;

            float r, g, b;
            if (e instanceof PlayerEntity) {
                r = 1f; g = 0.32f; b = 0.42f;
            } else if (e instanceof Monster) {
                r = 1f; g = 0.62f; b = 0.16f;
            } else {
                r = 0.36f; g = 0.80f; b = 1f;
            }

            Box box = e.getBoundingBox();
            if (esp) drawBox(buf, m, box, r, g, b, 0.85f);
            if (hbShow) drawBox(buf, m, box.expand(hbSize), 1f, 0.85f, 0.2f, 0.85f);
            if (tracers) {
                Vec3d c = box.getCenter();
                // у точки схода полностью прозрачно — никакого «пятна», яркость растёт к цели
                buf.vertex(m, (float) start.x, (float) start.y, (float) start.z).color(r, g, b, 0.0f).next();
                buf.vertex(m, (float) c.x, (float) c.y, (float) c.z).color(r, g, b, 0.8f).next();
            }
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

    private static void drawBox(BufferBuilder b, Matrix4f m, Box box, float r, float g, float bl, float a) {
        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        // низ
        line(b, m, x1, y1, z1, x2, y1, z1, r, g, bl, a);
        line(b, m, x2, y1, z1, x2, y1, z2, r, g, bl, a);
        line(b, m, x2, y1, z2, x1, y1, z2, r, g, bl, a);
        line(b, m, x1, y1, z2, x1, y1, z1, r, g, bl, a);
        // верх
        line(b, m, x1, y2, z1, x2, y2, z1, r, g, bl, a);
        line(b, m, x2, y2, z1, x2, y2, z2, r, g, bl, a);
        line(b, m, x2, y2, z2, x1, y2, z2, r, g, bl, a);
        line(b, m, x1, y2, z2, x1, y2, z1, r, g, bl, a);
        // стойки
        line(b, m, x1, y1, z1, x1, y2, z1, r, g, bl, a);
        line(b, m, x2, y1, z1, x2, y2, z1, r, g, bl, a);
        line(b, m, x2, y1, z2, x2, y2, z2, r, g, bl, a);
        line(b, m, x1, y1, z2, x1, y2, z2, r, g, bl, a);
    }

    private static void line(BufferBuilder b, Matrix4f m, float x1, float y1, float z1,
                             float x2, float y2, float z2, float r, float g, float bl, float a) {
        b.vertex(m, x1, y1, z1).color(r, g, bl, a).next();
        b.vertex(m, x2, y2, z2).color(r, g, bl, a).next();
    }
}
