package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.ModeSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

/**
 * Зажми ЛКМ на цели — модуль бьёт по тайму с критом.
 * "Прыжок" — реальный микро-прыжок (настоящий крит со звёздами).
 * "Пакет" — крит через пакеты движения (без видимого прыжка).
 * "Тайминг" — бьёт только когда ты уже падаешь.
 */
public class AutoCrit extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "Прыжок", "Пакет", "Тайминг");

    public AutoCrit() {
        super("AutoCrit", "Крит при зажатой ЛКМ по цели.", Category.COMBAT);
        addSettings(mode);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null || mc.options == null) return;
        if (!mc.options.attackKey.isPressed()) return;

        LivingEntity target = targetUnderCrosshair();
        if (target == null) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        boolean canCrit = !mc.player.isOnGround() && mc.player.getVelocity().y < 0
                && !mc.player.isTouchingWater() && !mc.player.isInLava()
                && !mc.player.isClimbing() && !mc.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.LEVITATION);

        switch (mode.get()) {
            case "Тайминг" -> {
                if (canCrit) attack(target);
            }
            case "Пакет" -> {
                packetHop();
                attack(target);
            }
            default -> { // Прыжок
                if (canCrit) {
                    attack(target);
                } else if (mc.player.isOnGround()) {
                    Vec3d v = mc.player.getVelocity();
                    mc.player.setVelocity(v.x, 0.42, v.z);
                }
            }
        }
    }

    private LivingEntity targetUnderCrosshair() {
        HitResult hit = mc.crosshairTarget;
        if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity le && le.isAlive()) {
            return le;
        }
        return null;
    }

    private void attack(LivingEntity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void packetHop() {
        if (mc.player.networkHandler == null || !mc.player.isOnGround()) return;
        double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
        var nh = mc.player.networkHandler;
        nh.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false));
        nh.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
        nh.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.1E-5, z, false));
        nh.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
    }
}
