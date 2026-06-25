package com.liquidglass.module.setting;

public class BooleanSetting extends Setting {
    public boolean value;

    public BooleanSetting(String name, boolean def) {
        super(name);
        this.value = def;
    }

    public void toggle() {
        value = !value;
    }
}
