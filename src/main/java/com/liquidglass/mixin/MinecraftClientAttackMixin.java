package com.liquidglass.mixin;

import com.liquidglass.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Criticals по тайму: когда игрок реально атакует (клик), отправляем
 * мини-смещение, чтобы сервер засчитал критический удар. Без автокликов.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientAttackMixin {

    @Inject(method = "doAttack", at = @At("HEAD"), require = 0)
    private void liquidglass$criticals(CallbackInfoReturnable<Boolean> cir) {
        if (!ModuleManager.isEnabled("Criticals")) return;

        MinecraftClient mc = (MinecraftClient) (Object) this;
        ClientPlayerEntity p = mc.player;
        if (p == null || p.networkHandler == null) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult)) return;

        if (p.isOnGround() && !p.isTouchingWater() && !p.isInLava()
                && !p.isClimbing() && !p.hasVehicle() && p.getVelocity().y == 0) {
            double x = p.getX(), y = p.getY(), z = p.getZ();
            p.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false));
            p.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
            p.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.1E-5, z, false));
            p.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
        }
    }
}
