package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import net.minecraft.entity.player.PlayerAbilities;

public class Fly extends Module {
    public Fly() {
        super("Fly", "Свободный полёт (как в креативе).", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        PlayerAbilities ab = mc.player.getAbilities();
        ab.allowFlying = true;
        ab.flying = true;
        ab.setFlySpeed(0.06f);
        mc.player.fallDistance = 0f;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        PlayerAbilities ab = mc.player.getAbilities();
        ab.flying = false;
        ab.allowFlying = ab.creativeMode;
        ab.setFlySpeed(0.05f);
    }
}
