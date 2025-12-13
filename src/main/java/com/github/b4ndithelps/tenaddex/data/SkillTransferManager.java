package com.github.b4ndithelps.tenaddex.data;

import com.github.b4ndithelps.tenaddex.capability.SkillEnergyCapability;
import com.github.b4ndithelps.tenaddex.network.ModNetwork;
import com.github.b4ndithelps.tenaddex.network.SyncEnergyPacket;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Handles skill transfers and conversions between players/systems.
 * 
 * Features:
 * - Skill gifting between players
 * - Skill conversion to energy
 * - Cross-mod skill compatibility
 * - Transaction logging for safety
 */
public class SkillTransferManager {

    private static final SkillTransferManager INSTANCE = new SkillTransferManager();
    
    private SkillTransferManager() {}
    
    public static SkillTransferManager get() {
        return INSTANCE;
    }

    /**
     * Result of a skill operation
     */
    public record OperationResult(boolean success, String messageKey, Object... args) {
        public static OperationResult success(String key, Object... args) {
            return new OperationResult(true, key, args);
        }
        
        public static OperationResult failure(String key, Object... args) {
            return new OperationResult(false, key, args);
        }
        
        public Component toComponent() {
            return Component.translatable(messageKey, args);
        }
    }

    /**
     * Transfer a skill from one player to another.
     * The source player loses the skill, target player gains it.
     * 
     * @param source Player giving the skill
     * @param target Player receiving the skill
     * @param skillId The skill to transfer
     * @return Result of the operation
     */
    public OperationResult transferSkill(Player source, Player target, ResourceLocation skillId) {
        if (source.equals(target)) {
            return OperationResult.failure("tenaddex.transfer.same_player");
        }

        SkillStorage sourceStorage = SkillStorage.get(source);
        SkillStorage targetStorage = SkillStorage.get(target);
        
        if (sourceStorage == null || targetStorage == null) {
            return OperationResult.failure("tenaddex.transfer.storage_error");
        }

        // Find the skill instance
        ManasSkillInstance skillInstance = findSkillInstance(sourceStorage, skillId);
        if (skillInstance == null) {
            return OperationResult.failure("tenaddex.transfer.skill_not_found");
        }

        ManasSkill skill = skillInstance.getSkill();

        // Check if target already has this skill
        if (SkillRegistry.get().playerHasSkill(target, skillId)) {
            return OperationResult.failure("tenaddex.transfer.target_has_skill");
        }

        // Perform transfer
        sourceStorage.forgetSkill(skill);
        targetStorage.learnSkill(skill);

        // Invalidate caches
        SkillRegistry.get().invalidatePlayerCache(source);
        SkillRegistry.get().invalidatePlayerCache(target);

        return OperationResult.success("tenaddex.transfer.success", 
                skill.getName().getString(), target.getName().getString());
    }

    /**
     * Convert a skill to energy (full breakdown).
     * 
     * @param player The player breaking down the skill
     * @param skillId The skill to break down
     * @param fullBreakdown True = destroy skill, False = extract (partial)
     * @return Result with energy gained
     */
    public OperationResult breakdownSkill(ServerPlayer player, ResourceLocation skillId, boolean fullBreakdown) {
        SkillStorage storage = SkillStorage.get(player);
        if (storage == null) {
            return OperationResult.failure("tenaddex.breakdown.storage_error");
        }

        ManasSkillInstance skillInstance = findSkillInstance(storage, skillId);
        if (skillInstance == null) {
            return OperationResult.failure("tenaddex.breakdown.skill_not_found");
        }

        ManasSkill skill = skillInstance.getSkill();
        
        // Calculate energy yield
        double energyYield = com.github.b4ndithelps.tenaddex.util.SkillEnergyCalculator
                .calculateBreakdownEnergy(skillInstance, fullBreakdown);

        // Add energy to player
        final double[] added = {0};
        SkillEnergyCapability.get(player).ifPresent(cap -> {
            added[0] = cap.addEnergy(energyYield);
            cap.incrementSkillsAnalyzed();

            if (fullBreakdown) {
                storage.forgetSkill(skill);
            } else {
                // Partial extract - reduce mastery
                int currentMastery = skillInstance.getMastery();
                skillInstance.setMastery(Math.max(0, currentMastery - 25));
            }

            // Sync to client
            ModNetwork.sendToPlayer(new SyncEnergyPacket(
                    cap.getEnergy(), cap.getMaxEnergy(), cap.getSkillsAnalyzed()), player);
        });

        // Invalidate cache
        SkillRegistry.get().invalidatePlayerCache(player);

        if (fullBreakdown) {
            return OperationResult.success("tenaddex.breakdown.full_success",
                    skill.getName().getString(), String.format("%.0f", added[0]));
        } else {
            return OperationResult.success("tenaddex.breakdown.extract_success",
                    skill.getName().getString(), String.format("%.0f", added[0]));
        }
    }

    /**
     * Create/forge a new skill using energy and EP.
     * 
     * @param player The player creating the skill
     * @param skillId The skill to create
     * @return Result of the operation
     */
    public OperationResult forgeSkill(ServerPlayer player, ResourceLocation skillId) {
        // Get skill data
        Optional<SkillRegistry.SkillData> dataOpt = SkillRegistry.get().getSkillData(skillId);
        if (dataOpt.isEmpty()) {
            return OperationResult.failure("tenaddex.forge.unknown_skill");
        }

        SkillRegistry.SkillData data = dataOpt.get();
        
        // Check if player already has this skill
        if (SkillRegistry.get().playerHasSkill(player, skillId)) {
            return OperationResult.failure("tenaddex.forge.already_owned");
        }

        // Check and consume resources
        final OperationResult[] result = {null};
        
        SkillEnergyCapability.get(player).ifPresent(cap -> {
            if (cap.getEnergy() < data.creationCost) {
                result[0] = OperationResult.failure("tenaddex.forge.insufficient_energy",
                        String.format("%.0f", data.creationCost),
                        String.format("%.0f", cap.getEnergy()));
                return;
            }

            // Check EP
            var epStorage = com.github.manasmods.tensura.capability.ep.EPStorage.get(player);
            if (epStorage == null || epStorage.getEp() < data.epCost) {
                result[0] = OperationResult.failure("tenaddex.forge.insufficient_ep",
                        String.format("%.0f", data.epCost));
                return;
            }

            // Consume resources
            cap.consumeEnergy(data.creationCost);
            epStorage.removeEp(data.epCost);

            // Grant skill
            SkillStorage storage = SkillStorage.get(player);
            if (storage != null) {
                storage.learnSkill(data.skill);
            }

            // Sync
            ModNetwork.sendToPlayer(new SyncEnergyPacket(
                    cap.getEnergy(), cap.getMaxEnergy(), cap.getSkillsAnalyzed()), player);

            result[0] = OperationResult.success("tenaddex.forge.success", data.name);
        });

        if (result[0] == null) {
            result[0] = OperationResult.failure("tenaddex.forge.unknown_error");
        }

        // Invalidate cache
        SkillRegistry.get().invalidatePlayerCache(player);

        return result[0];
    }

    /**
     * Helper to find a skill instance in storage by ID
     */
    private ManasSkillInstance findSkillInstance(SkillStorage storage, ResourceLocation skillId) {
        for (ManasSkillInstance instance : storage.getLearnedSkills()) {
            if (instance.getSkill().getRegistryName().equals(skillId)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Calculate total energy value of all player's skills
     */
    public double calculateTotalSkillValue(Player player) {
        SkillStorage storage = SkillStorage.get(player);
        if (storage == null) return 0;

        double total = 0;
        for (ManasSkillInstance instance : storage.getLearnedSkills()) {
            total += com.github.b4ndithelps.tenaddex.util.SkillEnergyCalculator
                    .calculateBreakdownEnergy(instance, true);
        }
        return total;
    }
}

