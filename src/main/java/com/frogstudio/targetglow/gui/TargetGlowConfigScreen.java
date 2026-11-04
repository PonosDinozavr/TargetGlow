package com.frogstudio.targetglow.gui;

import com.frogstudio.targetglow.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Modern, clean configuration screen for TargetGlow.
 * Features stylish pure-color swatch tiles instead of text buttons,
 * along with RGB sliders, opacity, fade speed, and target mobs toggles.
 */
public class TargetGlowConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    private static final int WIDGET_WIDTH = 210;
    private static final int WIDGET_HEIGHT = 20;

    // 8 sleek modern color presets (Lime, Emerald, Cyan, Blue, Purple, Red, Amber, White)
    private static final int[][] PRESET_COLORS = {
            {0x39, 0xFF, 0x14}, // Neon Lime (Default)
            {0x00, 0xE6, 0x76}, // Emerald Green
            {0x00, 0xE5, 0xFF}, // Cyber Cyan
            {0x29, 0x79, 0xFF}, // Electric Blue
            {0xD5, 0x00, 0xF9}, // Neon Purple
            {0xFF, 0x17, 0x44}, // Crimson Red
            {0xFF, 0xAB, 0x00}, // Amber Gold
            {0xFF, 0xFF, 0xFF}  // Pure White
    };

    public TargetGlowConfigScreen(Screen parent) {
        super(Component.translatable("targetglow.title"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 32;
        int spacing = 24;

        // 1. Toggle Mod Enabled
        addRenderableWidget(Button.builder(
                getEnabledMessage(),
                button -> {
                    config.enabled = !config.enabled;
                    button.setMessage(getEnabledMessage());
                    config.save();
                }
        ).bounds(centerX - WIDGET_WIDTH / 2, startY, WIDGET_WIDTH, WIDGET_HEIGHT).build());

        // 2. Toggle Target Mobs
        addRenderableWidget(Button.builder(
                getTargetMobsMessage(),
                button -> {
                    config.targetMobs = !config.targetMobs;
                    button.setMessage(getTargetMobsMessage());
                    config.save();
                }
        ).bounds(centerX - WIDGET_WIDTH / 2, startY + spacing, WIDGET_WIDTH, WIDGET_HEIGHT).build());

        // 3. Opacity Slider (10% to 100%)
        addRenderableWidget(new AbstractSliderButton(
                centerX - WIDGET_WIDTH / 2,
                startY + spacing * 2,
                WIDGET_WIDTH,
                WIDGET_HEIGHT,
                getOpacityMessage(),
                (config.maxOpacity - 0.10) / 0.90
        ) {
            @Override
            protected void updateMessage() {
                setMessage(getOpacityMessage());
            }

            @Override
            protected void applyValue() {
                config.maxOpacity = (float) (0.10 + this.value * 0.90);
                config.save();
            }
        });

        // 4. Smoothness / Fade Speed Button
        addRenderableWidget(Button.builder(
                getSmoothnessMessage(),
                button -> {
                    cycleSmoothness();
                    button.setMessage(getSmoothnessMessage());
                    config.save();
                }
        ).bounds(centerX - WIDGET_WIDTH / 2, startY + spacing * 3, WIDGET_WIDTH, WIDGET_HEIGHT).build());

        // 5. RGB Sliders for fine-tuning
        int rgbY = startY + spacing * 4;
        addRenderableWidget(new AbstractSliderButton(
                centerX - WIDGET_WIDTH / 2,
                rgbY,
                WIDGET_WIDTH,
                WIDGET_HEIGHT,
                Component.translatable("targetglow.color_r", config.red),
                config.red / 255.0
        ) {
            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("targetglow.color_r", config.red));
            }

            @Override
            protected void applyValue() {
                config.red = (int) Math.round(this.value * 255.0);
                config.save();
            }
        });

        addRenderableWidget(new AbstractSliderButton(
                centerX - WIDGET_WIDTH / 2,
                rgbY + spacing,
                WIDGET_WIDTH,
                WIDGET_HEIGHT,
                Component.translatable("targetglow.color_g", config.green),
                config.green / 255.0
        ) {
            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("targetglow.color_g", config.green));
            }

            @Override
            protected void applyValue() {
                config.green = (int) Math.round(this.value * 255.0);
                config.save();
            }
        });

        addRenderableWidget(new AbstractSliderButton(
                centerX - WIDGET_WIDTH / 2,
                rgbY + spacing * 2,
                WIDGET_WIDTH,
                WIDGET_HEIGHT,
                Component.translatable("targetglow.color_b", config.blue),
                config.blue / 255.0
        ) {
            @Override
            protected void updateMessage() {
                setMessage(Component.translatable("targetglow.color_b", config.blue));
            }

            @Override
            protected void applyValue() {
                config.blue = (int) Math.round(this.value * 255.0);
                config.save();
            }
        });

        // 6. Color Presets: Pure Color Swatch Tiles (no text)
        int presetY = rgbY + spacing * 3;
        int swatchSize = 20;
        int gap = 6;
        int totalSwatchesW = PRESET_COLORS.length * swatchSize + (PRESET_COLORS.length - 1) * gap;
        int startSwatchX = centerX - totalSwatchesW / 2;

        for (int i = 0; i < PRESET_COLORS.length; i++) {
            int[] rgb = PRESET_COLORS[i];
            int sx = startSwatchX + i * (swatchSize + gap);
            addRenderableWidget(new ColorSwatchWidget(sx, presetY, swatchSize, rgb[0], rgb[1], rgb[2], () -> {
                config.setRgbColor(rgb[0], rgb[1], rgb[2]);
                config.save();
                this.rebuildWidgets();
            }));
        }

        // 7. Bottom controls: Reset & Done
        int bottomY = this.height - 28;
        addRenderableWidget(Button.builder(
                Component.translatable("targetglow.reset"),
                button -> {
                    config.resetDefaults();
                    config.save();
                    this.rebuildWidgets();
                }
        ).bounds(centerX - 105, bottomY, 100, WIDGET_HEIGHT).build());

        addRenderableWidget(Button.builder(
                Component.translatable("targetglow.done"),
                button -> onClose()
        ).bounds(centerX + 5, bottomY, 100, WIDGET_HEIGHT).build());
    }

    private Component getEnabledMessage() {
        Component state = config.enabled
                ? Component.literal("§a").append(Component.translatable("targetglow.on"))
                : Component.literal("§c").append(Component.translatable("targetglow.off"));
        return Component.translatable("targetglow.enabled", state);
    }

    private Component getTargetMobsMessage() {
        Component state = config.targetMobs
                ? Component.literal("§a").append(Component.translatable("targetglow.on"))
                : Component.literal("§c").append(Component.translatable("targetglow.off"));
        return Component.translatable("targetglow.target_mobs", state);
    }

    private Component getOpacityMessage() {
        return Component.translatable("targetglow.opacity", (int) Math.round(config.maxOpacity * 100));
    }

    private Component getSmoothnessMessage() {
        String key;
        if (config.fadeSpeed <= 5.0F) {
            key = "targetglow.smoothness.smooth";
        } else if (config.fadeSpeed <= 10.0F) {
            key = "targetglow.smoothness.balanced";
        } else if (config.fadeSpeed <= 20.0F) {
            key = "targetglow.smoothness.fast";
        } else {
            key = "targetglow.smoothness.instant";
        }
        return Component.translatable("targetglow.smoothness", Component.translatable(key));
    }

    private void cycleSmoothness() {
        if (config.fadeSpeed <= 5.0F) {
            config.fadeSpeed = 8.0F;   // Balanced
        } else if (config.fadeSpeed <= 10.0F) {
            config.fadeSpeed = 16.0F;  // Fast
        } else if (config.fadeSpeed <= 20.0F) {
            config.fadeSpeed = 30.0F;  // Instant
        } else {
            config.fadeSpeed = 4.0F;   // Smooth
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Semi-transparent dark background
        graphics.fill(0, 0, this.width, this.height, 0xC010141C);

        // Centered screen title
        graphics.centeredText(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);

        // Color preview swatch box next to the RGB sliders
        int centerX = this.width / 2;
        int swatchX = centerX + WIDGET_WIDTH / 2 + 12;
        int swatchY = 32 + 24 * 4;
        int swatchSize = 44;

        if (swatchX + swatchSize <= this.width - 10) {
            // White border
            graphics.fill(swatchX - 1, swatchY - 1, swatchX + swatchSize + 1, swatchY + swatchSize + 1, 0xFFFFFFFF);
            // Current color preview
            int previewColor = 0xFF000000 | config.getRgbColor();
            graphics.fill(swatchX, swatchY, swatchX + swatchSize, swatchY + swatchSize, previewColor);
            // Hex string indicator
            String hex = String.format("#%02X%02X%02X", config.red, config.green, config.blue);
            graphics.text(this.font, hex, swatchX - 2, swatchY + swatchSize + 4, 0xFFE0E0E0);
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        config.save();
        Minecraft client = (this.minecraft != null) ? this.minecraft : Minecraft.getInstance();
        if (this.parent != null) {
            client.setScreenAndShow(this.parent);
        } else if (client.level != null) {
            client.setScreenAndShow(null);
        } else {
            client.setScreenAndShow(new TitleScreen());
        }
    }

    /**
     * Pure-color swatch tile widget without text.
     * Displays a colored square with hover effects and selected-state highlights.
     */
    private static class ColorSwatchWidget extends AbstractWidget {
        private final int r, g, b;
        private final Runnable onSelect;

        public ColorSwatchWidget(int x, int y, int size, int r, int g, int b, Runnable onSelect) {
            super(x, y, size, size, Component.empty());
            this.r = r;
            this.g = g;
            this.b = b;
            this.onSelect = onSelect;
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            onSelect.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            int x = getX();
            int y = getY();
            int w = getWidth();
            int h = getHeight();

            ModConfig cfg = ModConfig.getInstance();
            boolean isSelected = (cfg.red == r && cfg.green == g && cfg.blue == b);

            // Double border highlight if selected; light gray if hovered; dark slate if normal
            if (isSelected) {
                graphics.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0xFFFFFFFF);
                graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
            } else if (isHoveredOrFocused()) {
                graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFFE5E7EB);
            } else {
                graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF374151);
            }

            // Fill with pure preset color
            int color = 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
            graphics.fill(x, y, x + w, y + h, color);
        }
    }
}
