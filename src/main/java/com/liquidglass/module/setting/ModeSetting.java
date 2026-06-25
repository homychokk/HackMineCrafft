package com.liquidglass.module.setting;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {
    public final List<String> modes;
    public int index;

    public ModeSetting(String name, String... modes) {
        super(name);
        this.modes = Arrays.asList(modes);
        this.index = 0;
    }

    public String get() {
        return modes.get(index);
    }

    public boolean is(String mode) {
        return get().equals(mode);
    }

    public void cycle(int dir) {
        index = (index + dir + modes.size()) % modes.size();
    }
}
