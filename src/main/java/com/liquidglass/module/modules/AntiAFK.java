package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import net.minecraft.util.Hand;

public class AntiAFK extends Module {
    private int timer = 0;

    public AntiAFK() {
        super("AntiAFK", "Микро-действия против кика за бездействие.", Category.MISC);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        timer++;
        if (timer % 60 == 0) {
            mc.player.setYaw(mc.player.getYaw() + (timer % 120 == 0 ? 12f : -12f));
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
