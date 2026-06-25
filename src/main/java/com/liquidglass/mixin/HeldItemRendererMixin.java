package com.liquidglass.mixin;

import com.liquidglass.module.modules.HandTweaks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), require = 0)
    private void liquidglass$handOffset(AbstractClientPlayerEntity player, float tickDelta, float pitch,
                                        Hand hand, float swingProgress, ItemStack item, float equipProgress,
                                        MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                        CallbackInfo ci) {
        if (HandTweaks.on) {
            matrices.translate(HandTweaks.offX, HandTweaks.offY, HandTweaks.offZ);
            if (HandTweaks.scale != 1f) matrices.scale(HandTweaks.scale, HandTweaks.scale, HandTweaks.scale);
            if (HandTweaks.rotX != 0f) matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(HandTweaks.rotX));
            if (HandTweaks.rotY != 0f) matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(HandTweaks.rotY));
            if (HandTweaks.rotZ != 0f) matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(HandTweaks.rotZ));
        }
    }
}
