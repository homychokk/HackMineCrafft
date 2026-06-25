package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "Защита от урона при падении.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.networkHandler == null) return;
        // На реальных серверах: говорим что стоим на земле, пока падаем
        if (mc.player.fallDistance > 1.2f && !mc.player.isOnGround() && mc.player.getVelocity().y < 0) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
        mc.player.fallDistance = 0f;
    }
}
