package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import net.minecraft.util.math.Vec3d;

/** Ускоренное падение (лучше включить с NoFall). */
public class FastFall extends Module {
    public FastFall() {
        super("FastFall", "Падай быстрее. Включи с NoFall.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.isOnGround() && mc.player.getVelocity().y < 0 && !mc.player.isTouchingWater()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, v.y - 0.4, v.z);
        }
    }
}
