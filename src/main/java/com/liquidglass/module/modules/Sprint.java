package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Постоянный спринт в любую сторону.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.input != null && mc.player.input.movementForward != 0f && !mc.player.isSneaking()) {
            mc.player.setSprinting(true);
        }
    }
}
