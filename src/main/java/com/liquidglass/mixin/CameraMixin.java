package com.liquidglass.mixin;

import com.liquidglass.module.modules.FreeCam;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"), require = 0)
    private void liquidglass$freecam(BlockView area, Entity focusedEntity, boolean thirdPerson,
                                     boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (FreeCam.active) {
            setRotation(FreeCam.yaw, FreeCam.pitch);
            setPos(FreeCam.x, FreeCam.y, FreeCam.z);
        }
    }
}
