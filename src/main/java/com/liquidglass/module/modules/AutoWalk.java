package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;

/** Автоматическое движение вперёд. */
public class AutoWalk extends Module {
    public AutoWalk() {
        super("AutoWalk", "Идти вперёд автоматически.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        mc.options.forwardKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.forwardKey.setPressed(false);
    }
}
