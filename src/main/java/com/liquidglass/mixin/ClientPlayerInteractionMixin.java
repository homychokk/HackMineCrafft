package com.liquidglass.mixin;

import com.liquidglass.module.modules.FreeCam;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * FreeCam:
 * 1) дистанция до блока считается от камеры — можно открыть сундук.
 * 2) Позиция игрока подменяется на позицию камеры на время interactBlock,
 *    чтобы серверная проверка дальности не мешала.
 */
@Mixin(net.minecraft.client.network.ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionMixin {

    // ── Считаем дистанцию от позиции камеры ─────────────────────────────────
    @Redirect(
            method = "interactBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;squaredDistanceTo(Lnet/minecraft/util/math/Vec3d;)D"),
            require = 0
    )
    private double liquidglass$freecamDist(ClientPlayerEntity player, Vec3d pos) {
        if (FreeCam.active) {
            Vec3d eye = new Vec3d(FreeCam.x, FreeCam.y, FreeCam.z);
            return eye.squaredDistanceTo(pos);
        }
        return player.squaredDistanceTo(pos);
    }

    private double liquidglass$origX, liquidglass$origY, liquidglass$origZ;

    // ── Временно смещаем позицию игрока перед interactBlock ─────────────────
    @Inject(
            method = "interactBlock",
            at = @At("HEAD"),
            require = 0
    )
    private void liquidglass$beforeInteract(ClientPlayerEntity player, Hand hand,
                                             BlockHitResult hitResult,
                                             CallbackInfoReturnable<ActionResult> cir) {
        if (FreeCam.active) {
            liquidglass$origX = player.getX();
            liquidglass$origY = player.getY();
            liquidglass$origZ = player.getZ();

            // Спофаем позицию для сервера, чтобы пройти проверку дистанции
            player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround(
                    FreeCam.x, FreeCam.y, FreeCam.z, player.isOnGround()
            ));

            // Временно смещаем игрока в позицию камеры, чтобы локальный пакет прошёл
            player.setPosition(FreeCam.x, FreeCam.y, FreeCam.z);
        }
    }

    @Inject(
            method = "interactBlock",
            at = @At("RETURN"),
            require = 0
    )
    private void liquidglass$afterInteract(ClientPlayerEntity player, Hand hand,
                                            BlockHitResult hitResult,
                                            CallbackInfoReturnable<ActionResult> cir) {
        if (FreeCam.active) {
            // Возвращаем игрока обратно на сервере
            player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround(
                    liquidglass$origX, liquidglass$origY, liquidglass$origZ, player.isOnGround()
            ));

            // Возвращаем локально
            player.setPosition(liquidglass$origX, liquidglass$origY, liquidglass$origZ);
        }
    }
}
