package com.frogstudio.targetglow.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration holder for TargetGlow with auto-saving to config/targetglow.json.
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("targetglow.json");
    private static final ModConfig INSTANCE = new ModConfig();

    // Settings
    public boolean enabled = true;
    public boolean targetMobs = false;     // Target mobs as well as players
    public float maxOpacity = 0.65f;       // 10% to 100% (default 65%)
    public int red = 0x39;                 // Neon Lime Green (#39FF14)
    public int green = 0xFF;
    public int blue = 0x14;
    public float fadeSpeed = 8.0f;         // 4.0 = smooth, 8.0 = balanced, 16.0 = fast, 30.0 = instant

    public static ModConfig getInstance() {
        return INSTANCE;
    }

    public int getRgbColor() {
        return ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    public void setRgbColor(int r, int g, int b) {
        this.red = Math.max(0, Math.min(255, r));
        this.green = Math.max(0, Math.min(255, g));
        this.blue = Math.max(0, Math.min(255, b));
    }

    public void resetDefaults() {
        this.enabled = true;
        this.targetMobs = false;
        this.maxOpacity = 0.65f;
        this.red = 0x39;
        this.green = 0xFF;
        this.blue = 0x14;
        this.fadeSpeed = 8.0f;
    }

    public void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
            if (loaded != null) {
                this.enabled = loaded.enabled;
                this.targetMobs = loaded.targetMobs;
                this.maxOpacity = Math.max(0.10f, Math.min(1.00f, loaded.maxOpacity));
                this.red = Math.max(0, Math.min(255, loaded.red));
                this.green = Math.max(0, Math.min(255, loaded.green));
                this.blue = Math.max(0, Math.min(255, loaded.blue));
                this.fadeSpeed = Math.max(1.0f, Math.min(30.0f, loaded.fadeSpeed));
            }
        } catch (Exception e) {
            System.err.println("[TargetGlow] Failed to load configuration: " + e.getMessage());
        }
    }

    public void save() {
        try {
            if (CONFIG_PATH.getParent() != null) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            System.err.println("[TargetGlow] Failed to save configuration: " + e.getMessage());
        }
    }
}
