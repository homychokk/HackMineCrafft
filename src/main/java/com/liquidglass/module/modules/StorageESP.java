package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.BooleanSetting;
import com.liquidglass.module.setting.NumberSetting;

/** Подсветка сундуков/шалкеров/эндеров и спавнеров (поиск лута и данжей). */
public class StorageESP extends Module {
    public final BooleanSetting chests = new BooleanSetting("Сундуки/бочки", true);
    public final BooleanSetting shulkers = new BooleanSetting("Шалкеры", true);
    public final BooleanSetting spawners = new BooleanSetting("Спавнеры (данжи)", true);
    public final BooleanSetting fill = new BooleanSetting("Заливка", true);
    public final NumberSetting radius = new NumberSetting("Радиус", 24, 8, 48, 1);

    public StorageESP() {
        super("StorageESP", "Подсветка хранилищ и спавнеров.", Category.RENDER);
        addSettings(chests, shulkers, spawners, fill, radius);
    }
}
