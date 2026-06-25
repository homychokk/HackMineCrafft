package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.NumberSetting;

/** Смещение/масштаб/поворот модели руки от первого лица. */
public class HandTweaks extends Module {
    public static double offX, offY, offZ;
    public static float scale = 1f;
    public static float rotX, rotY, rotZ;
    public static boolean on;

    private final NumberSetting x = new NumberSetting("X", 0.0, -1.0, 1.0, 0.02);
    private final NumberSetting y = new NumberSetting("Y", 0.0, -1.0, 1.0, 0.02);
    private final NumberSetting z = new NumberSetting("Z", 0.0, -1.0, 1.0, 0.02);
    private final NumberSetting sc = new NumberSetting("Масштаб", 1.0, 0.3, 2.5, 0.05);
    private final NumberSetting rx = new NumberSetting("Поворот X", 0, -180, 180, 1);
    private final NumberSetting ry = new NumberSetting("Поворот Y", 0, -180, 180, 1);
    private final NumberSetting rz = new NumberSetting("Поворот Z", 0, -180, 180, 1);

    public HandTweaks() {
        super("HandTweaks", "Положение/масштаб/поворот руки.", Category.RENDER);
        addSettings(x, y, z, sc, rx, ry, rz);
    }

    @Override
    public void onEnable() {
        push();
    }

    @Override
    public void onDisable() {
        on = false;
    }

    @Override
    public void onTick() {
        push();
    }

    private void push() {
        offX = x.value;
        offY = y.value;
        offZ = z.value;
        scale = (float) sc.value;
        rotX = (float) rx.value;
        rotY = (float) ry.value;
        rotZ = (float) rz.value;
        on = isEnabled();
    }
}
