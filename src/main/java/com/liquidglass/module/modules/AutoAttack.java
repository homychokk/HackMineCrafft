package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Бьёт цель под прицелом, как только готов кулдаун. */
public class AutoAttack extends Module {
    public AutoAttack() {
        super("AutoAttack", "Авто-удар по тому, на кого смотришь.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;
        HitResult hit = mc.crosshairTarget;
        if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity le && le.isAlive()) {
            mc.interactionManager.attackEntity(mc.player, le);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
