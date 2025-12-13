package com.github.b4ndithelps.tenforge.capability;

import com.github.b4ndithelps.tenforge.Tenforge;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Capability system for storing Skill Energy on players.
 * 
 * Skill Energy is obtained by breaking down skills and used to forge new ones.
 * This follows the Tensura lore where skills can be analyzed and their essence extracted.
 */
@Mod.EventBusSubscriber(modid = Tenforge.MODID)
public class SkillEnergyCapability {

    public static final Capability<ISkillEnergy> SKILL_ENERGY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation ID = new ResourceLocation(Tenforge.MODID, "skill_energy");

    /**
     * Get skill energy capability from a player
     */
    public static LazyOptional<ISkillEnergy> get(Player player) {
        return player.getCapability(SKILL_ENERGY);
    }

    /**
     * Interface defining skill energy operations
     */
    public interface ISkillEnergy extends INBTSerializable<CompoundTag> {
        
        /** Current stored skill energy */
        double getEnergy();
        
        /** Maximum energy capacity */
        double getMaxEnergy();
        
        /** Add energy (returns actual amount added) */
        double addEnergy(double amount);
        
        /** Remove energy (returns true if successful) */
        boolean consumeEnergy(double amount);
        
        /** Set energy directly */
        void setEnergy(double amount);
        
        /** Set max energy capacity */
        void setMaxEnergy(double max);
        
        /** Get number of skills analyzed */
        int getSkillsAnalyzed();
        
        /** Increment skills analyzed counter */
        void incrementSkillsAnalyzed();
        
        /** Get total energy ever collected (for stats) */
        double getTotalEnergyCollected();
        
        /** Sync to client */
        void sync(Player player);
    }

    /**
     * Default implementation of skill energy storage
     */
    public static class SkillEnergyStorage implements ISkillEnergy {
        
        private double energy = 0;
        private double maxEnergy = 100000; // Base max, increases with mastery
        private int skillsAnalyzed = 0;
        private double totalCollected = 0;

        @Override
        public double getEnergy() {
            return energy;
        }

        @Override
        public double getMaxEnergy() {
            return maxEnergy;
        }

        @Override
        public double addEnergy(double amount) {
            double space = maxEnergy - energy;
            double added = Math.min(amount, space);
            energy += added;
            totalCollected += added;
            return added;
        }

        @Override
        public boolean consumeEnergy(double amount) {
            if (energy >= amount) {
                energy -= amount;
                return true;
            }
            return false;
        }

        @Override
        public void setEnergy(double amount) {
            this.energy = Math.max(0, Math.min(amount, maxEnergy));
        }

        @Override
        public void setMaxEnergy(double max) {
            this.maxEnergy = max;
            if (energy > maxEnergy) {
                energy = maxEnergy;
            }
        }

        @Override
        public int getSkillsAnalyzed() {
            return skillsAnalyzed;
        }

        @Override
        public void incrementSkillsAnalyzed() {
            skillsAnalyzed++;
            // Increase max energy with each skill analyzed
            maxEnergy += 5000;
        }

        @Override
        public double getTotalEnergyCollected() {
            return totalCollected;
        }

        @Override
        public void sync(Player player) {
            // Network sync handled by packet system
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("Energy", energy);
            tag.putDouble("MaxEnergy", maxEnergy);
            tag.putInt("SkillsAnalyzed", skillsAnalyzed);
            tag.putDouble("TotalCollected", totalCollected);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            energy = tag.getDouble("Energy");
            maxEnergy = tag.getDouble("MaxEnergy");
            skillsAnalyzed = tag.getInt("SkillsAnalyzed");
            totalCollected = tag.getDouble("TotalCollected");
        }
    }

    /**
     * Capability provider for attaching to players
     */
    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        
        private final SkillEnergyStorage storage = new SkillEnergyStorage();
        private final LazyOptional<ISkillEnergy> optional = LazyOptional.of(() -> storage);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == SKILL_ENERGY ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return storage.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            storage.deserializeNBT(nbt);
        }
        
        public void invalidate() {
            optional.invalidate();
        }
    }

    // Event handlers for capability attachment and persistence

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(SKILL_ENERGY).isPresent()) {
                event.addCapability(ID, new Provider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Preserve skill energy on death/respawn
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(SKILL_ENERGY).ifPresent(oldCap -> {
                event.getEntity().getCapability(SKILL_ENERGY).ifPresent(newCap -> {
                    newCap.deserializeNBT(oldCap.serializeNBT());
                });
            });
            event.getOriginal().invalidateCaps();
        }
    }

    /**
     * Register the capability - called during mod setup
     */
    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(ISkillEnergy.class);
    }
}

