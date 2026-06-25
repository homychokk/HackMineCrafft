package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoBridge / Scaffold: ставит блок под игроком, опираясь на соседний блок.
 * Встань на край и иди — мост будет достраиваться (нужен блок в руке).
 */
public class Scaffold extends Module {
    public Scaffold() {
        super("Scaffold", "Авто-мост: ставит блоки под ноги (блок в руке).", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        ItemStack stack = mc.player.getMainHandStack();
        if (!(stack.getItem() instanceof BlockItem) || stack.isEmpty()) return;

        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) return;

        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos neighbor = below.offset(dir);
            if (mc.world.getBlockState(neighbor).isAir()) continue;
            if (!mc.world.getBlockState(neighbor).isSolidBlock(mc.world, neighbor)) continue;

            Direction face = dir.getOpposite();
            Vec3d hit = Vec3d.ofCenter(neighbor)
                    .add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
            BlockHitResult result = new BlockHitResult(hit, face, neighbor, false);

            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
            mc.player.swingHand(Hand.MAIN_HAND);
            break;
        }
    }
}
