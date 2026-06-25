package com.liquidglass.mixin;

import com.liquidglass.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Velocity: убирает отбрасывание от ударов для игрока. */
@Mixin(LivingEntity.class)
public class VelocityMixin {

    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true, require = 0)
    private void liquidglass$velocity(double strength, double x, double z, CallbackInfo ci) {
        Object self = this;
        if (self == MinecraftClient.getInstance().player && ModuleManager.isEnabled("Velocity")) {
            ci.cancel();
        }
    }
}
