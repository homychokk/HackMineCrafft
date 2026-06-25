package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** Авто-сбор созревших грядок вокруг (ломает спелое). */
public class AutoFarm extends Module {
    private final NumberSetting radius = new NumberSetting("Радиус", 4, 2, 6, 1);

    public AutoFarm() {
        super("AutoFarm", "Собирает спелые посевы вокруг.", Category.MISC);
        addSettings(radius);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        int r = (int) radius.value;
        BlockPos base = mc.player.getBlockPos();
        BlockPos.Mutable p = new BlockPos.Mutable();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    p.set(base.getX() + dx, base.getY() + dy, base.getZ() + dz);
                    if (isMature(mc.world.getBlockState(p))) {
                        BlockPos pos = p.toImmutable();
                        mc.interactionManager.attackBlock(pos, Direction.UP);
                        mc.player.swingHand(mc.player.getActiveHand());
                        return; // один блок за тик
                    }
                }
            }
        }
    }

    private boolean isMature(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof CropBlock cb) return cb.isMature(state); // включая свёклу
        if (b instanceof NetherWartBlock) return state.get(NetherWartBlock.AGE) >= 3;
        return false;
    }
}
