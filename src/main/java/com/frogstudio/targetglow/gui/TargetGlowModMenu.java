package com.frogstudio.targetglow.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu integration entrypoint.
 * Allows opening the TargetGlow settings screen directly from the in-game mods list.
 */
public class TargetGlowModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TargetGlowConfigScreen::new;
    }
}
