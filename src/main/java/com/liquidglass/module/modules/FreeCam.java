package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Свободная камера. WASD — движение, Space/Shift — вверх/вниз, мышь — обзор.
 * ПКМ по сундуку — открыть и лутать (прицел от камеры).
 */
public class FreeCam extends Module {
    public static boolean active = false;
    public static double x, y, z;
    public static float yaw, pitch;

    private double velX, velY, velZ;

    public FreeCam() {
        super("FreeCam", "Камера летает. ПКМ — лут сундуков.", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            setEnabled(false);
            return;
        }
        x = mc.player.getX();
        y = mc.player.getY() + mc.player.getStandingEyeHeight();
        z = mc.player.getZ();
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
        velX = velY = velZ = 0;
        active = true;
    }

    @Override
    public void onDisable() {
        active = false;
        velX = velY = velZ = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null || mc.world == null) return;

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        double accel = 0.55;
        double damping = 0.72;

        double f = 0, s = 0, up = 0;
        if (mc.options.forwardKey.isPressed()) f += 1;
        if (mc.options.backKey.isPressed()) f -= 1;
        if (mc.options.rightKey.isPressed()) s += 1;
        if (mc.options.leftKey.isPressed()) s -= 1;
        if (mc.options.jumpKey.isPressed()) up += 1;
        if (mc.options.sneakKey.isPressed()) up -= 1;

        Vec3d fwd = Vec3d.fromPolar(0f, yaw);
        Vec3d str = Vec3d.fromPolar(0f, yaw + 90f);

        double tx = (fwd.x * f + str.x * s) * accel;
        double tz = (fwd.z * f + str.z * s) * accel;
        double ty = up * accel;

        velX = velX * damping + tx * (1 - damping);
        velY = velY * damping + ty * (1 - damping);
        velZ = velZ * damping + tz * (1 - damping);

        x += velX;
        y += velY;
        z += velZ;

        mc.player.setVelocity(0, 0, 0);
        mc.player.input.movementForward = 0;
        mc.player.input.movementSideways = 0;
        mc.player.input.jumping = false;
        mc.player.input.sneaking = false;

        updateCrosshair();
    }

    /** Прицел и raycast от камеры — можно навести на сундук и открыть ПКМ. */
    private void updateCrosshair() {
        if (mc.interactionManager == null) return;

        Vec3d eye = new Vec3d(x, y, z);
        Vec3d dir = Vec3d.fromPolar(pitch, yaw);
        double reach = mc.interactionManager.getReachDistance();
        Vec3d end = eye.add(dir.multiply(reach));

        BlockHitResult blockHit = mc.world.raycast(new RaycastContext(
                eye, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        Box search = mc.player.getBoundingBox().stretch(dir.multiply(reach)).expand(1.0);
        EntityHitResult entityHit = ProjectileUtil.raycast(
                mc.player, eye, end, search,
                e -> !e.isSpectator() && e.canHit(),
                reach * reach
        );

        double blockDist = blockHit.getType() != HitResult.Type.MISS
                ? eye.squaredDistanceTo(blockHit.getPos()) : Double.MAX_VALUE;
        double entityDist = entityHit != null
                ? eye.squaredDistanceTo(entityHit.getPos()) : Double.MAX_VALUE;

        if (entityHit != null && entityDist < blockDist) {
            mc.crosshairTarget = entityHit;
            mc.targetedEntity = entityHit.getEntity();
        } else {
            mc.crosshairTarget = blockHit;
            mc.targetedEntity = null;
        }
    }
}
