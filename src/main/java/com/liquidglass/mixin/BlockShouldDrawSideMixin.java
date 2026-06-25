package com.liquidglass.mixin;

import com.liquidglass.module.Module;
import com.liquidglass.module.ModuleManager;
import com.liquidglass.module.modules.XRay;
import com.liquidglass.render.XRayBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockShouldDrawSideMixin {

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true, require = 0)
    private static void liquidglass$xray(BlockState state, BlockView world, BlockPos pos, Direction side,
                                         BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
        Module xray = ModuleManager.get("XRay");
        if (xray == null || !xray.isEnabled()) return;

        String mode = ((XRay) xray).mode.get();
        if (mode.equals("Всё видно")) return;
        if (mode.equals("Всё прозрачно")) {
            cir.setReturnValue(false);
            return;
        }
        if (mode.equals("Только ценное")) {
            cir.setReturnValue(XRayBlocks.isValuable(state.getBlock()));
            return;
        }
        // Только руды
        cir.setReturnValue(XRayBlocks.isOre(state.getBlock()));
    }
}
