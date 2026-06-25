package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** Быстро ломает блоки вокруг (по одному за тик, ближайший). */
public class Nuker extends Module {
    private final NumberSetting radius = new NumberSetting("Радиус", 3, 1, 5, 1);

    public Nuker() {
        super("Nuker", "Ломает блоки вокруг.", Category.MISC);
        addSettings(radius);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        int r = (int) radius.value;
        BlockPos base = mc.player.getBlockPos();
        BlockPos target = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos.Mutable p = new BlockPos.Mutable();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    p.set(base.getX() + dx, base.getY() + dy, base.getZ() + dz);
                    BlockState st = mc.world.getBlockState(p);
                    if (st.isAir() || st.getHardness(mc.world, p) < 0) continue;
                    double d = p.getSquaredDistance(mc.player.getEyePos());
                    if (d < bestDist) {
                        bestDist = d;
                        target = p.toImmutable();
                    }
                }
            }
        }
        if (target != null) {
            mc.interactionManager.attackBlock(target, Direction.UP);
            mc.player.swingHand(mc.player.getActiveHand());
        }
    }
}
