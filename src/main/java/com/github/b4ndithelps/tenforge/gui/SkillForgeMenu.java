package com.github.b4ndithelps.tenforge.gui;

import com.github.b4ndithelps.tenforge.capability.SkillEnergyCapability;
import com.github.b4ndithelps.tenforge.registry.ModMenuTypes;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Container/Menu for the Skill Forge GUI.
 * Handles data synchronization between server and client.
 */
public class SkillForgeMenu extends AbstractContainerMenu {

    private final Player player;
    private final ContainerData data;
    
    // Cached data for client display
    private double skillEnergy = 0;
    private double maxEnergy = 100000;
    private int skillsAnalyzed = 0;

    public SkillForgeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player);
    }

    public SkillForgeMenu(int containerId, Inventory playerInventory, Player player) {
        super(ModMenuTypes.SKILL_FORGE_MENU.get(), containerId);
        this.player = player;
        
        // Container data for syncing energy values (using int representation)
        this.data = new SimpleContainerData(6);
        this.addDataSlots(data);
        
        // Load initial data
        refreshData();
    }

    /**
     * Refresh cached data from capability
     */
    public void refreshData() {
        SkillEnergyCapability.get(player).ifPresent(cap -> {
            this.skillEnergy = cap.getEnergy();
            this.maxEnergy = cap.getMaxEnergy();
            this.skillsAnalyzed = cap.getSkillsAnalyzed();
            
            // Store in container data (split doubles into two ints)
            data.set(0, (int) skillEnergy);
            data.set(1, (int) (skillEnergy * 100) % 100); // Decimal part
            data.set(2, (int) maxEnergy);
            data.set(3, (int) (maxEnergy * 100) % 100);
            data.set(4, skillsAnalyzed);
        });
    }

    /**
     * Get current skill energy
     */
    public double getSkillEnergy() {
        return data.get(0) + (data.get(1) / 100.0);
    }

    /**
     * Get max skill energy
     */
    public double getMaxEnergy() {
        return data.get(2) + (data.get(3) / 100.0);
    }

    /**
     * Get number of skills analyzed
     */
    public int getSkillsAnalyzed() {
        return data.get(4);
    }

    /**
     * Get energy fill percentage (0-1)
     */
    public float getEnergyPercentage() {
        double max = getMaxEnergy();
        return max > 0 ? (float) (getSkillEnergy() / max) : 0f;
    }

    /**
     * Get list of player's skills (for display)
     */
    public List<ManasSkillInstance> getPlayerSkills() {
        SkillStorage storage = SkillStorage.get(player);
        if (storage != null) {
            return storage.getLearnedSkills();
        }
        return new ArrayList<>();
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // No item slots
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // Always valid since it's skill-based
    }

    /**
     * Menu provider for opening the GUI
     */
    public static class Provider implements MenuProvider {
        
        @Override
        public Component getDisplayName() {
            return Component.translatable("tenforge.gui.skill_forge.title");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return new SkillForgeMenu(containerId, inventory, player);
        }
    }
}

