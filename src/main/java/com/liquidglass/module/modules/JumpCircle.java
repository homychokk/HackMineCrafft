package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.BooleanSetting;
import com.liquidglass.module.setting.ModeSetting;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.Random;

/** Эффекты персонажа: разные паттерны при прыжке и приземлении. */
public class JumpCircle extends Module {
    private boolean wasOnGround = true;
    private final Random rnd = new Random();

    private final ModeSetting style = new ModeSetting("Стиль", "Случайно", "End Rod", "Огонь души",
            "Искры", "Облако", "Крит", "Портал", "Душа");
    private final ModeSetting landFx = new ModeSetting("Приземление", "Купол", "Кольцо", "Спираль", "Взрыв");
    private final BooleanSetting onJump = new BooleanSetting("При прыжке", true);
    private final BooleanSetting onLand = new BooleanSetting("При приземлении", true);

    private static final ParticleEffect[] RANDOM_SET = {
            ParticleTypes.END_ROD, ParticleTypes.CLOUD, ParticleTypes.SOUL_FIRE_FLAME,
            ParticleTypes.ELECTRIC_SPARK, ParticleTypes.CRIT, ParticleTypes.FIREWORK,
            ParticleTypes.PORTAL, ParticleTypes.SOUL
    };

    public JumpCircle() {
        super("PlayerFX", "Эффекты при прыжке и приземлении.", Category.RENDER);
        addSettings(style, landFx, onJump, onLand);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        boolean onGround = mc.player.isOnGround();

        if (wasOnGround && !onGround && mc.player.getVelocity().y > 0 && onJump.value) {
            ring(0.7, 0.04);
        }
        if (!wasOnGround && onGround && onLand.value) {
            landEffect();
        }
        wasOnGround = onGround;
    }

    private void landEffect() {
        switch (landFx.get()) {
            case "Кольцо" -> { ring(1.1, 0.0); ring(0.55, 0.0); }
            case "Спираль" -> spiral();
            case "Взрыв" -> burst();
            default -> dome();
        }
    }

    private void ring(double radius, double upVel) {
        ParticleEffect effect = pick();
        double px = mc.player.getX(), py = mc.player.getY() + 0.05, pz = mc.player.getZ();
        int count = 30;
        for (int i = 0; i < count; i++) {
            double ang = (Math.PI * 2 * i) / count;
            double dx = Math.cos(ang), dz = Math.sin(ang);
            mc.world.addParticle(effect, px + dx * radius, py, pz + dz * radius, dx * 0.08, upVel, dz * 0.08);
        }
    }

    private void dome() {
        ParticleEffect effect = pick();
        double px = mc.player.getX(), py = mc.player.getY() + 0.05, pz = mc.player.getZ();
        for (int i = 0; i < 60; i++) {
            double theta = rnd.nextDouble() * Math.PI * 2;
            double phi = rnd.nextDouble() * Math.PI / 2;
            double r = 1.1;
            double dx = Math.cos(theta) * Math.sin(phi);
            double dz = Math.sin(theta) * Math.sin(phi);
            double dy = Math.cos(phi);
            mc.world.addParticle(effect, px, py, pz, dx * 0.25 * r, dy * 0.22, dz * 0.25 * r);
        }
    }

    private void spiral() {
        ParticleEffect effect = pick();
        double px = mc.player.getX(), py = mc.player.getY() + 0.05, pz = mc.player.getZ();
        for (int i = 0; i < 50; i++) {
            double t = i / 50.0;
            double ang = t * Math.PI * 6;
            double r = 0.2 + t * 1.0;
            double dx = Math.cos(ang) * r, dz = Math.sin(ang) * r;
            mc.world.addParticle(effect, px + dx, py + t * 1.6, pz + dz, dx * 0.02, 0.05, dz * 0.02);
        }
    }

    private void burst() {
        ParticleEffect effect = pick();
        double px = mc.player.getX(), py = mc.player.getY() + 0.2, pz = mc.player.getZ();
        for (int i = 0; i < 70; i++) {
            double vx = (rnd.nextDouble() - 0.5) * 0.6;
            double vy = rnd.nextDouble() * 0.5;
            double vz = (rnd.nextDouble() - 0.5) * 0.6;
            mc.world.addParticle(effect, px, py, pz, vx, vy, vz);
        }
    }

    private ParticleEffect pick() {
        return switch (style.get()) {
            case "End Rod" -> ParticleTypes.END_ROD;
            case "Огонь души" -> ParticleTypes.SOUL_FIRE_FLAME;
            case "Искры" -> ParticleTypes.ELECTRIC_SPARK;
            case "Облако" -> ParticleTypes.CLOUD;
            case "Крит" -> ParticleTypes.CRIT;
            case "Портал" -> ParticleTypes.PORTAL;
            case "Душа" -> ParticleTypes.SOUL;
            default -> RANDOM_SET[rnd.nextInt(RANDOM_SET.length)];
        };
    }
}
