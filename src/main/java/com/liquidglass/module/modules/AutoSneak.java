package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;

/** Постоянное приседание. */
public class AutoSneak extends Module {
    public AutoSneak() {
        super("AutoSneak", "Всегда красться.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.options != null) mc.options.sneakKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.sneakKey.setPressed(false);
    }
}
