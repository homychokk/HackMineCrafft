package com.liquidglass.module.modules;

import com.liquidglass.module.Category;
import com.liquidglass.module.Module;

/**
 * Простой модуль-переключатель без логики (для демонстрации меню).
 */
public class SimpleToggle extends Module {
    public SimpleToggle(String name, String description, Category category) {
        super(name, description, category);
    }
}
