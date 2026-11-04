package com.frogstudio.targetglow.detection;

import com.frogstudio.targetglow.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Isolated target detection logic for TargetGlow.
 * Detects whether the crosshair is currently targeted on an entity within melee attack reach.
 */
public final class TargetDetector {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private TargetDetector() {
    }

    /**
     * Finds the entity currently under the player's crosshair within melee attack reach.
     *
     * @param client Minecraft client instance
     * @param config TargetGlow configuration
     * @return the targeted LivingEntity, or null if no valid target is within attack reach
     */
    public static LivingEntity getTarget(Minecraft client, ModConfig config) {
        if (!config.enabled) {
            return null;
        }

        LocalPlayer localPlayer = client.player;
        if (localPlayer == null || client.level == null) {
            return null;
        }

        if (localPlayer.isSpectator() || !localPlayer.isAlive()) {
            return null;
        }

        // Vanilla Minecraft automatically updates crosshairPickEntity on every frame
        // based on the player's exact attack reach (including item range components and block raycasting)
        Entity candidate = client.crosshairPickEntity;
        if (candidate == null && client.hitResult instanceof EntityHitResult entityHit) {
            candidate = entityHit.getEntity();
        }

        if (isValidTarget(localPlayer, candidate, config.targetMobs)) {
            return (LivingEntity) candidate;
        }

        return null;
    }

    /**
     * Checks if a candidate entity meets all TargetGlow criteria:
     * - Is a LivingEntity (and NOT an ArmorStand)
     * - Is alive
     * - Is NOT the local player (prevents self-targeting in F5 3rd person)
     * - If target is a Player:
     *   - Cannot be in Spectator mode
     *   - FAIR PLAY / ANTI-CHEAT RULE:
     *     If invisible without any armor pieces, completely ignored.
     *     If invisible with at least one armor piece, highlighted normally.
     * - If target is a Mob / Non-Player:
     *   - Allowed only if targetMobs is enabled in config.
     *   - If invisible without armor, completely ignored.
     */
    public static boolean isValidTarget(LocalPlayer localPlayer, Entity entity, boolean targetMobs) {
        if (entity == null || entity == localPlayer) {
            return false;
        }

        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        if (!living.isAlive() || living instanceof ArmorStand) {
            return false;
        }

        if (living instanceof Player player) {
            if (player.isSpectator()) {
                return false;
            }

            // Invisible player fair-play check
            if (player.isInvisible() && !hasAnyArmor(player)) {
                return false;
            }

            return true;
        }

        // Non-player living entities (mobs, animals, etc.)
        if (!targetMobs) {
            return false;
        }

        // Invisible mobs fair-play check
        if (living.isInvisible() && !hasAnyArmor(living)) {
            return false;
        }

        return true;
    }

    private static boolean hasAnyArmor(LivingEntity entity) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!entity.getItemBySlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
