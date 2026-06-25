package com.liquidglass.module;

import com.liquidglass.module.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;

    public final List<Setting> settings = new ArrayList<>();
    public int keyBind = -1; // GLFW key, -1 = нет

    // Анимации для меню (хранятся тут для удобства рендера)
    public float toggleAnim = 0f;
    public float hoverAnim = 0f;
    public float expandAnim = 0f;
    public boolean expanded = false;

    protected void addSettings(Setting... s) {
        Collections.addAll(settings, s);
    }

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }
}
