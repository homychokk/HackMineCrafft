package com.liquidglass.module.setting;

public class NumberSetting extends Setting {
    public double value;
    public final double min;
    public final double max;
    public final double step;

    public NumberSetting(String name, double def, double min, double max, double step) {
        super(name);
        this.value = def;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public void setFromFraction(double fraction) {
        fraction = Math.max(0, Math.min(1, fraction));
        double raw = min + (max - min) * fraction;
        double snapped = Math.round(raw / step) * step;
        value = Math.max(min, Math.min(max, snapped));
    }

    public double getFraction() {
        return (value - min) / (max - min);
    }

    public String display() {
        if (step >= 1) return String.valueOf((int) Math.round(value));
        return String.format("%.2f", value);
    }
}
