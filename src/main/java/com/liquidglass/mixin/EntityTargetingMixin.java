package com.liquidglass.mixin;

import com.liquidglass.module.Module;
import com.liquidglass.module.ModuleManager;
import com.liquidglass.module.modules.Hitboxes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hitboxes: увеличивает «целевой» бокс живых сущностей (используется при
 * рейкасте атаки), поэтому по ним проще попасть. Физику не меняет.
 */
@Mixin(Entity.class)
public class EntityTargetingMixin {

    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true, require = 0)
    private void liquidglass$hitboxes(CallbackInfoReturnable<Float> cir) {
        Entity self = (Entity) (Object) this;
        Module h = ModuleManager.get("Hitboxes");
        if (self instanceof LivingEntity && h != null && h.isEnabled()) {
            cir.setReturnValue((float) ((Hitboxes) h).size.value);
        }
    }
}
