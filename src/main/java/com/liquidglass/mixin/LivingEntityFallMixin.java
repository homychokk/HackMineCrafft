package com.liquidglass.mixin;

import com.liquidglass.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NoFall: полностью отменяет урон от падения для игрока (клиент/интегрированный сервер). */
@Mixin(LivingEntity.class)
public class LivingEntityFallMixin {

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true, require = 0)
    private void liquidglass$noFall(float fallDistance, float damageMultiplier, DamageSource damageSource,
                                    CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (self == MinecraftClient.getInstance().player && ModuleManager.isEnabled("NoFall")) {
            cir.setReturnValue(false);
        }
    }
}
