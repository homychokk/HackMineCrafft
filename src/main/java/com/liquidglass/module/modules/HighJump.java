package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

/** Высокий прыжок. */
public class HighJump extends Module {
    private final NumberSetting power = new NumberSetting("Сила", 0.6, 0.45, 1.5, 0.05);

    public HighJump() {
        super("HighJump", "Прыгай выше.", Category.MOVEMENT);
        addSettings(power);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        if (mc.options.jumpKey.isPressed() && mc.player.isOnGround()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, power.value, v.z);
        }
    }
}
