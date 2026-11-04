package com.frogstudio.targetglow;

import com.frogstudio.targetglow.config.ModConfig;
import com.frogstudio.targetglow.gui.TargetGlowConfigScreen;
import com.frogstudio.targetglow.render.VignetteRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TargetGlowClient implements ClientModInitializer {
    public static final String MOD_ID = "targetglow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyMapping openConfigKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[TargetGlow] Initializing TargetGlow mod for Minecraft 26.3...");

        // 1. Load configuration from config/targetglow.json
        ModConfig.getInstance().load();

        // 2. Register keybinding (Default: G)
        openConfigKey = new KeyMapping(
                "key.targetglow.open_config",
                InputConstants.KEY_G,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "general"))
        );
        KeyMappingHelper.registerKeyMapping(openConfigKey);

        // 3. Listen for key press to open settings screen
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                client.setScreenAndShow(new TargetGlowConfigScreen(null));
            }
        });

        // 4. Register vignette overlay to the HUD element pipeline
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(MOD_ID, "vignette"),
                new VignetteRenderer()
        );

        LOGGER.info("[TargetGlow] TargetGlow initialized successfully!");
    }
}
