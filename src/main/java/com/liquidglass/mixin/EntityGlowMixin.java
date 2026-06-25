package com.liquidglass.mixin;

import com.liquidglass.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Glow: обводка-подсветка врагов и мобов. */
@Mixin(Entity.class)
public class EntityGlowMixin {

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true, require = 0)
    private void liquidglass$glow(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self == MinecraftClient.getInstance().player) return;
        if ((self instanceof Monster || self instanceof PlayerEntity) && ModuleManager.isEnabled("Glow")) {
            cir.setReturnValue(true);
        }
    }
}
