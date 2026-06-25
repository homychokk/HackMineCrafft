package com.liquidglass.mixin;

import com.liquidglass.gui.ClickGuiScreen;
import com.liquidglass.module.Module;
import com.liquidglass.module.ModuleManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void liquidglass$onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getWindow() == null || window != mc.getWindow().getHandle()) return;
        if (action != GLFW.GLFW_PRESS) return;
        if (mc.world == null) return;

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (mc.currentScreen == null) {
                mc.setScreen(new ClickGuiScreen());
            } else if (mc.currentScreen instanceof ClickGuiScreen) {
                mc.setScreen(null);
            }
            return;
        }

        // Бинды модулей работают только в игре (когда не открыт экран)
        if (mc.currentScreen == null) {
            for (Module m : ModuleManager.getModules()) {
                if (m.keyBind == key) {
                    m.toggle();
                }
            }
        }
    }
}
