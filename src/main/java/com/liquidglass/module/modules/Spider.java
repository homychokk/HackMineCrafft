package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import net.minecraft.util.math.Vec3d;

/** Лазание по стенам как паук. */
public class Spider extends Module {
    public Spider() {
        super("Spider", "Карабкайся по стенам.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.horizontalCollision) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.2, v.z);
        }
    }
}
