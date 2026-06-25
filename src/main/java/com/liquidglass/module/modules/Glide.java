package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

/** Плавное планирование вниз. */
public class Glide extends Module {
    private final NumberSetting fall = new NumberSetting("Скорость падения", 0.05, 0.0, 0.2, 0.01);

    public Glide() {
        super("Glide", "Медленное планирование.", Category.MOVEMENT);
        addSettings(fall);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.isOnGround() && mc.player.getVelocity().y < 0 && !mc.player.isTouchingWater()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, -fall.value, v.z);
        }
    }
}
