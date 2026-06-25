package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/** Авто-выбор лучшего инструмента под блок при добыче. */
public class AutoTool extends Module {
    public AutoTool() {
        super("AutoTool", "Сам берёт лучший инструмент при копке.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.options == null) return;
        if (!mc.options.attackKey.isPressed()) return;
        HitResult hit = mc.crosshairTarget;
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockState state = mc.world.getBlockState(bhr.getBlockPos());
        int best = mc.player.getInventory().selectedSlot;
        float bestSpeed = speed(mc.player.getInventory().getStack(best), state);

        for (int i = 0; i < 9; i++) {
            float s = speed(mc.player.getInventory().getStack(i), state);
            if (s > bestSpeed) {
                bestSpeed = s;
                best = i;
            }
        }
        if (best != mc.player.getInventory().selectedSlot) {
            mc.player.getInventory().selectedSlot = best;
        }
    }

    private float speed(ItemStack stack, BlockState state) {
        if (stack.isEmpty()) return 1f;
        return stack.getMiningSpeedMultiplier(state);
    }
}
