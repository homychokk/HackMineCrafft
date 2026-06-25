package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;

public class Zoom extends Module {
    private int previousFov = 70;

    public Zoom() {
        super("Zoom", "Приближение обзора (уменьшает FOV).", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            previousFov = mc.options.getFov().getValue();
            mc.options.getFov().setValue(30);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getFov().setValue(previousFov);
        }
    }
}
