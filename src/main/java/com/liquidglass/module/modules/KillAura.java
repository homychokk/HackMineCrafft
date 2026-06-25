package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.BooleanSetting;
import com.liquidglass.module.setting.ModeSetting;
import com.liquidglass.module.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class KillAura extends Module {
    private final NumberSetting range = new NumberSetting("Дистанция", 4.2, 3.0, 6.0, 0.1);
    private final ModeSetting targets = new ModeSetting("Цели", "Все", "Мобы", "Животные", "Игроки");
    private final ModeSetting rotate = new ModeSetting("Поворот", "Сервер (тихо)", "Нет", "Клиент");
    private final BooleanSetting requireSee = new BooleanSetting("Сквозь стены выкл", true);

    public KillAura() {
        super("KillAura", "Меч сам бьёт ближних целей. Смотри куда угодно.", Category.COMBAT);
        addSettings(range, targets, rotate, requireSee);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        LivingEntity target = null;
        double best = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (!matches(le)) continue;

            double d = mc.player.distanceTo(le);
            if (d > range.value) continue;
            if (requireSee.value && !mc.player.canSee(le)) continue;
            if (d < best) {
                best = d;
                target = le;
            }
        }

        if (target == null) return;

        switch (rotate.get()) {
            case "Клиент" -> faceTo(target, false);
            case "Сервер (тихо)" -> faceTo(target, true);
            default -> {
            }
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean matches(LivingEntity le) {
        boolean isPlayer = le instanceof PlayerEntity;
        boolean isMonster = le instanceof Monster;
        boolean isAnimal = le instanceof AnimalEntity || le instanceof PassiveEntity;
        return switch (targets.get()) {
            case "Мобы" -> isMonster;
            case "Животные" -> isAnimal && !isMonster;
            case "Игроки" -> isPlayer;
            default -> isPlayer || isMonster || isAnimal; // Все
        };
    }

    private void faceTo(LivingEntity target, boolean silent) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d t = target.getBoundingBox().getCenter();
        double dx = t.x - eye.x;
        double dy = t.y - eye.y;
        double dz = t.z - eye.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) Math.max(-90f, Math.min(90f, (float) -Math.toDegrees(Math.atan2(dy, dist))));

        if (silent) {
            // Поворот уходит только на сервер — камера игрока не двигается
            if (mc.player.networkHandler != null) {
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
            }
        } else {
            float curYaw = mc.player.getYaw();
            float curPitch = mc.player.getPitch();
            mc.player.setYaw(curYaw + wrap(yaw - curYaw) * 0.5f);
            mc.player.setPitch(curPitch + (pitch - curPitch) * 0.5f);
        }
    }

    private static float wrap(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }
}
