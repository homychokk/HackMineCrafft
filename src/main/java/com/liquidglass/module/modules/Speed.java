package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.ModeSetting;
import com.liquidglass.module.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "BHop", "Strafe", "Плавный");
    private final NumberSetting speed = new NumberSetting("Скорость", 0.34, 0.20, 0.80, 0.01);

    public Speed() {
        super("Speed", "Ускоренное передвижение по земле.", Category.MOVEMENT);
        addSettings(mode, speed);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0f && side == 0f) return;
        if (mc.player.isSneaking() || mc.player.isTouchingWater() || mc.player.isInLava()) return;

        double mul = speed.value;
        if (mode.is("Плавный")) mul *= 0.75;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        double mx = (fwd * -sin + side * cos) * mul;
        double mz = (fwd * cos + side * sin) * mul;

        Vec3d v = mc.player.getVelocity();
        double vy = v.y;
        if (mode.is("BHop") && mc.player.isOnGround()) {
            vy = 0.42;
        }
        mc.player.setVelocity(mx, vy, mz);
    }
}
