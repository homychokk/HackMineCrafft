package com.liquidglass.module;

import com.liquidglass.module.modules.AntiAFK;
import com.liquidglass.module.modules.AutoAttack;
import com.liquidglass.module.modules.AutoCrit;
import com.liquidglass.module.modules.AutoFarm;
import com.liquidglass.module.modules.AutoSneak;
import com.liquidglass.module.modules.AutoSprint;
import com.liquidglass.module.modules.AutoTool;
import com.liquidglass.module.modules.AutoWalk;
import com.liquidglass.module.modules.FastFall;
import com.liquidglass.module.modules.Fly;
import com.liquidglass.module.modules.FreeCam;
import com.liquidglass.module.modules.Fullbright;
import com.liquidglass.module.modules.Glide;
import com.liquidglass.module.modules.HandTweaks;
import com.liquidglass.module.modules.HighJump;
import com.liquidglass.module.modules.Hitboxes;
import com.liquidglass.module.modules.JumpCircle;
import com.liquidglass.module.modules.KillAura;
import com.liquidglass.module.modules.NoFall;
import com.liquidglass.module.modules.Nuker;
import com.liquidglass.module.modules.Scaffold;
import com.liquidglass.module.modules.SimpleToggle;
import com.liquidglass.module.modules.Speed;
import com.liquidglass.module.modules.Spider;
import com.liquidglass.module.modules.Sprint;
import com.liquidglass.module.modules.StorageESP;
import com.liquidglass.module.modules.XRay;
import com.liquidglass.module.modules.Zoom;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    public static void init() {
        if (!MODULES.isEmpty()) return;

        // --- COMBAT ---
        register(new KillAura());
        register(new AutoAttack());
        register(new AutoCrit());
        register(new SimpleToggle("Criticals", "Крит по тайму при твоей атаке.", Category.COMBAT));
        register(new Hitboxes());
        register(new SimpleToggle("Velocity", "Меньше отбрасывания от ударов.", Category.COMBAT));

        // --- MOVEMENT ---
        register(new Fly());
        register(new Speed());
        register(new Scaffold());
        register(new AutoSprint());
        register(new Sprint());
        register(new NoFall());
        register(new Spider());
        register(new HighJump());
        register(new Glide());
        register(new FastFall());
        register(new AutoWalk());
        register(new AutoSneak());

        // --- RENDER ---
        register(new Fullbright());
        register(new Zoom());
        register(new FreeCam());
        register(new XRay());
        register(new HandTweaks());
        register(new JumpCircle());
        register(new StorageESP());
        register(new SimpleToggle("ESP", "Бокс вокруг сущностей.", Category.RENDER));
        register(new SimpleToggle("Tracers", "Линии до сущностей.", Category.RENDER));
        register(new SimpleToggle("Glow", "Подсветка-обводка врагов и мобов.", Category.RENDER));
        register(new SimpleToggle("NoHurtCam", "Убрать тряску камеры при уроне.", Category.RENDER));

        // --- PLAYER ---
        register(new AutoTool());
        register(new SimpleToggle("AutoArmor", "Авто-надевание брони.", Category.PLAYER));

        // --- HUD ---
        register(new SimpleToggle("Watermark", "Логотип клиента на экране.", Category.HUD));
        register(new SimpleToggle("FPS", "Счётчик FPS.", Category.HUD));
        register(new SimpleToggle("Coords", "Координаты игрока.", Category.HUD));
        register(new SimpleToggle("ArrayList", "Список активных модулей.", Category.HUD));
        register(new SimpleToggle("Keystrokes", "Отображение нажатий клавиш.", Category.HUD));
        register(new SimpleToggle("PotionHUD", "Активные эффекты.", Category.HUD));

        // --- MISC ---
        register(new AntiAFK());
        register(new AutoFarm());
        register(new Nuker());

        // По умолчанию включим HUD, чтобы сразу было видно
        setEnabled("Watermark", true);
        setEnabled("FPS", true);
        setEnabled("ArrayList", true);
    }

    private static void register(Module module) {
        MODULES.add(module);
    }

    public static List<Module> getModules() {
        return MODULES;
    }

    public static List<Module> getByCategory(Category category) {
        List<Module> list = new ArrayList<>();
        for (Module m : MODULES) {
            if (m.getCategory() == category) list.add(m);
        }
        return list;
    }

    public static Module get(String name) {
        for (Module m : MODULES) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public static boolean isEnabled(String name) {
        Module m = get(name);
        return m != null && m.isEnabled();
    }

    private static void setEnabled(String name, boolean enabled) {
        Module m = get(name);
        if (m != null) m.setEnabled(enabled);
    }

    public static void onTick() {
        for (Module m : MODULES) {
            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }
}
