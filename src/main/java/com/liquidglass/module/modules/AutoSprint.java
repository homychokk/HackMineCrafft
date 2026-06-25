package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;

public class AutoSprint extends Module {
    public AutoSprint() {
        super("AutoSprint", "Автоматический спринт при движении вперёд.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        if (mc.options.forwardKey.isPressed() && !mc.player.horizontalCollision && !mc.player.isSneaking()) {
            mc.player.setSprinting(true);
        }
    }
}
