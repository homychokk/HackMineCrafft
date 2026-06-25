package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;

public class Fullbright extends Module {
    private double previousGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "Максимальная яркость, видно в темноте.", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            previousGamma = mc.options.getGamma().getValue();
        }
    }

    @Override
    public void onTick() {
        if (mc.options != null) {
            mc.options.getGamma().setValue(1.0);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getGamma().setValue(previousGamma);
        }
    }
}
