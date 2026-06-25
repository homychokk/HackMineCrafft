package com.liquidglass;

import com.liquidglass.module.ModuleManager;
import com.liquidglass.render.EspRenderer;
import com.liquidglass.render.OreEspRenderer;
import com.liquidglass.render.StorageEspRenderer;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiquidGlassClient implements ClientModInitializer {
    public static final String MOD_ID = "liquidglass";
    public static final Logger LOGGER = LoggerFactory.getLogger("LiquidGlass");

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        EspRenderer.register();
        OreEspRenderer.register();
        StorageEspRenderer.register();
        LOGGER.info("[PrismGlass] loaded. Open menu with Right Shift.");
    }
}
