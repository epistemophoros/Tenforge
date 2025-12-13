package com.github.b4ndithelps.tenaddex.ability.skill.unique;

import com.github.b4ndithelps.tenaddex.TensuraAddonExample;
import com.github.b4ndithelps.tenaddex.capability.SkillEnergyCapability;
import com.github.b4ndithelps.tenaddex.gui.SkillForgeMenu;
import com.github.b4ndithelps.tenaddex.network.ModNetwork;
import com.github.b4ndithelps.tenaddex.network.OpenSkillForgePacket;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.ability.TensuraSkillInstance;
import com.github.manasmods.tensura.ability.skill.Skill;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

/**
 * Unique Skill: Skill Forge
 * 
 * Inspired by skills like Raphael, Great Sage, and Creator from 
 * "That Time I Got Reincarnated as a Slime".
 * 
 * This skill allows the user to:
 * - Break down skills into pure Skill Energy
 * - Use that energy (plus EP) to forge new skills
 * 
 * Unlike recipe-based systems, this uses pure energy conversion:
 * - Higher tier skills yield more energy when broken down
 * - Higher tier skills cost more energy to create
 * - Mastery level affects energy yield
 * 
 * Opens a GUI when activated for full control over the forging process.
 */
public class SkillForgeSkill extends Skill {

    // Skill configuration
    private final double learnCost = 8.0;

    @Override
    public ResourceLocation getSkillIcon() {
        return new ResourceLocation(TensuraAddonExample.MODID, "textures/skill/unique/skill_forge.png");
    }

    public SkillForgeSkill() {
        super(SkillType.UNIQUE);
    }

    @Override
    public double magiculeCost(LivingEntity entity, ManasSkillInstance instance) {
        // Small cost to open the forge
        return 50.0;
    }

    @Override
    public double learningCost() {
        return learnCost;
    }

    /**
     * Requires 50,000+ EP to unlock this skill
     */
    @Override
    public boolean meetEPRequirement(Player entity, double curEP) {
        return curEP >= 50000.0;
    }

    /**
     * When pressed, opens the Skill Forge GUI
     */
    @Override
    public void onPressed(ManasSkillInstance instance, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }

        // Check magicule cost
        if (SkillHelper.outOfMagicule(entity, instance)) {
            return;
        }

        // Play activation effect
        playActivationEffects(entity);

        // Open GUI (server-side for ServerPlayer, client sends packet otherwise)
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SkillForgeMenu.Provider(), buf -> {});
            
            // Sync energy data
            SkillEnergyCapability.get(player).ifPresent(cap -> {
                ModNetwork.sendToPlayer(
                    new com.github.b4ndithelps.tenaddex.network.SyncEnergyPacket(
                        cap.getEnergy(), cap.getMaxEnergy(), cap.getSkillsAnalyzed()
                    ), 
                    serverPlayer
                );
            });
        } else {
            // Client-side: send packet to server to open GUI
            ModNetwork.sendToServer(new OpenSkillForgePacket());
        }

        // Add mastery for using the skill
        this.addMasteryPoint(instance, entity);
    }

    /**
     * Display current energy when switching to this skill
     */
    @Override
    public Component getModeName(int curMode) {
        return Component.translatable("tenaddex.skill.mode.skill_forge.forge");
    }

    private void playActivationEffects(LivingEntity entity) {
        entity.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.5F);
        entity.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.5F, 2.0F);

        if (entity.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    entity.getX(), entity.getY() + 1.5, entity.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    entity.getX(), entity.getY() + 1.0, entity.getZ(),
                    10, 0.3, 0.3, 0.3, 0.05);
        }
    }
}
