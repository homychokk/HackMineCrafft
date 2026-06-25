package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.BooleanSetting;
import com.liquidglass.module.setting.NumberSetting;

public class Hitboxes extends Module {
    public final NumberSetting size = new NumberSetting("Размер", 0.30, 0.05, 4.00, 0.05);
    public final BooleanSetting show = new BooleanSetting("Показывать бокс", true);

    public Hitboxes() {
        super("Hitboxes", "Увеличенные хитбоксы целей.", Category.COMBAT);
        addSettings(size, show);
    }
}
