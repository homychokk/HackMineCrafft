package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;
import com.liquidglass.module.setting.BooleanSetting;
import com.liquidglass.module.setting.ModeSetting;
import com.liquidglass.module.setting.NumberSetting;

public class XRay extends Module {
    public final ModeSetting mode = new ModeSetting("Просвет", "Только руды", "Только ценное", "Всё видно", "Всё прозрачно");
    public final BooleanSetting glow = new BooleanSetting("Светящиеся боксы", true);
    public final ModeSetting espMode = new ModeSetting("Подсветка", "Все руды", "Только ценные");
    public final BooleanSetting fill = new BooleanSetting("Заливка", true);
    public final ModeSetting colors = new ModeSetting("Цвета", "По руде", "Радуга", "Один цвет");
    public final NumberSetting radius = new NumberSetting("Радиус", 16, 6, 40, 1);

    public XRay() {
        super("XRay", "Просвет блоков + светящиеся руды.", Category.RENDER);
        addSettings(mode, glow, espMode, fill, colors, radius);
    }

    @Override
    public void onEnable() {
        reload();
    }

    @Override
    public void onDisable() {
        reload();
    }

    private void reload() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }
}
