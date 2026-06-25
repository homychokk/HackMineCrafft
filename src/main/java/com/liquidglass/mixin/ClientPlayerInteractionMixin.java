package com.liquidglass.mixin;

import com.liquidglass.module.modules.FreeCam;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** FreeCam: дистанция до блока считается от камеры — можно открыть сундук. */
@Mixin(net.minecraft.client.network.ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionMixin {

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
}
