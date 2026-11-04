package com.frogstudio.targetglow.render;

import com.frogstudio.targetglow.config.ModConfig;
import com.frogstudio.targetglow.detection.TargetDetector;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Handles smooth fade-in/fade-out interpolation and rendering of the target vignette.
 * Separated cleanly from detection and config for easy backporting to 1.20.1 / 1.21.x.
 */
public class VignetteRenderer implements HudElement {
    public static final Identifier VIGNETTE_TEXTURE =
            Identifier.fromNamespaceAndPath("targetglow", "textures/gui/vignette.png");

    private float currentAlpha = 0.0F;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        ModConfig config = ModConfig.getInstance();

        if (!config.enabled) {
            currentAlpha = 0.0F;
            return;
        }

        boolean hasTarget = TargetDetector.getTarget(client, config) != null;
        float targetAlpha = hasTarget ? 1.0F : 0.0F;

        // Smooth time-based interpolation (lerp)
        float dt = (deltaTracker != null) ? (deltaTracker.getRealtimeDeltaTicks() / 20.0F) : 0.05F;
        if (dt <= 0.0F || dt > 0.25F) {
            dt = 0.05F;
        }

        float speed = config.fadeSpeed;
        currentAlpha += (targetAlpha - currentAlpha) * Math.min(1.0F, dt * speed);

        // When nearly invisible, skip rendering to save performance
        if (currentAlpha < 0.002F) {
            currentAlpha = 0.0F;
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        float effectiveAlpha = currentAlpha * config.maxOpacity;
        int color = ARGB.colorFromFloat(
                effectiveAlpha,
                config.red / 255.0F,
                config.green / 255.0F,
                config.blue / 255.0F
        );

        // Render vignette using the modern GUI_TEXTURED pipeline (SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VIGNETTE_TEXTURE,
                0, 0,
                0.0F, 0.0F,
                width, height,
                width, height,
                color
        );

        // Safe GL state reset to prevent leaking colors or blend modes to chat/hotbar
        resetGlState();
    }

    /**
     * Resets rendering state to defaults.
     * On 26.3, this resets the pipeline state.
     * On 1.20.1 backports, this maps to RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F) & disableBlend.
     */
    public static void resetGlState() {
        try {
            RenderSystem.setupDefaultState();
        } catch (Throwable ignored) {
        }
    }

    public float getCurrentAlpha() {
        return currentAlpha;
    }
}
