package com.github.b4ndithelps.tenaddex.gui;

import com.github.b4ndithelps.tenaddex.TensuraAddonExample;
import com.github.b4ndithelps.tenaddex.capability.SkillEnergyCapability;
import com.github.b4ndithelps.tenaddex.network.ModNetwork;
import com.github.b4ndithelps.tenaddex.network.SkillBreakdownPacket;
import com.github.b4ndithelps.tenaddex.network.SkillCreatePacket;
import com.github.b4ndithelps.tenaddex.util.SkillEnergyCalculator;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.tensura.ability.skill.Skill;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Skill Forge GUI Screen
 * 
 * A modern interface for breaking down and creating skills.
 * Features:
 * - Energy bar display
 * - Scrollable list of owned skills
 * - Skill creation panel
 * - Breakdown/Extract buttons
 */
public class SkillForgeScreen extends AbstractContainerScreen<SkillForgeMenu> {

    private static final ResourceLocation TEXTURE = 
            new ResourceLocation(TensuraAddonExample.MODID, "textures/gui/skill_forge.png");

    // GUI dimensions
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 200;
    
    // Scroll state
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SKILLS_PER_PAGE = 5;
    private static final int SKILL_ENTRY_HEIGHT = 24;
    
    // Selection state
    private ManasSkillInstance selectedSkill = null;
    private ManasSkill selectedCreateSkill = null;
    private int tabIndex = 0; // 0 = Breakdown, 1 = Create
    
    // Cached skill lists
    private List<ManasSkillInstance> playerSkills = new ArrayList<>();
    private List<ManasSkill> creatableSkills = new ArrayList<>();

    public SkillForgeScreen(SkillForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        
        // Center the GUI
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        // Tab buttons
        addRenderableWidget(Button.builder(Component.literal("Breakdown"), btn -> {
            tabIndex = 0;
            selectedSkill = null;
            scrollOffset = 0;
            refreshSkillList();
        }).bounds(leftPos + 10, topPos + 5, 60, 16).build());
        
        addRenderableWidget(Button.builder(Component.literal("Create"), btn -> {
            tabIndex = 1;
            selectedCreateSkill = null;
            scrollOffset = 0;
            refreshCreatableList();
        }).bounds(leftPos + 75, topPos + 5, 60, 16).build());
        
        // Action buttons (bottom)
        addRenderableWidget(Button.builder(Component.literal("Full Breakdown"), btn -> {
            if (selectedSkill != null) {
                ModNetwork.sendToServer(new SkillBreakdownPacket(
                        selectedSkill.getSkill().getRegistryName().toString(), true));
                selectedSkill = null;
                refreshSkillList();
            }
        }).bounds(leftPos + 10, topPos + GUI_HEIGHT - 30, 80, 20).build());
        
        addRenderableWidget(Button.builder(Component.literal("Extract"), btn -> {
            if (selectedSkill != null) {
                ModNetwork.sendToServer(new SkillBreakdownPacket(
                        selectedSkill.getSkill().getRegistryName().toString(), false));
                refreshSkillList();
            }
        }).bounds(leftPos + 95, topPos + GUI_HEIGHT - 30, 60, 20).build());
        
        addRenderableWidget(Button.builder(Component.literal("Forge Skill"), btn -> {
            if (selectedCreateSkill != null) {
                ModNetwork.sendToServer(new SkillCreatePacket(
                        selectedCreateSkill.getRegistryName().toString()));
                selectedCreateSkill = null;
                refreshCreatableList();
            }
        }).bounds(leftPos + 160, topPos + GUI_HEIGHT - 30, 80, 20).build());
        
        // Initial data load
        refreshSkillList();
        refreshCreatableList();
    }

    private void refreshSkillList() {
        playerSkills = new ArrayList<>(menu.getPlayerSkills());
        maxScroll = Math.max(0, playerSkills.size() - SKILLS_PER_PAGE);
    }

    private void refreshCreatableList() {
        creatableSkills = new ArrayList<>();
        // Get all registered skills that player doesn't have
        for (ManasSkill skill : SkillAPI.getSkillRegistry()) {
            boolean hasSkill = false;
            for (ManasSkillInstance owned : menu.getPlayerSkills()) {
                if (owned.getSkill().getRegistryName().equals(skill.getRegistryName())) {
                    hasSkill = true;
                    break;
                }
            }
            if (!hasSkill && skill instanceof Skill) {
                creatableSkills.add(skill);
            }
        }
        maxScroll = Math.max(0, creatableSkills.size() - SKILLS_PER_PAGE);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // Draw dark background
        fill(poseStack, leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xDD1a1a2e);
        
        // Draw border
        drawBorder(poseStack, leftPos, topPos, imageWidth, imageHeight, 0xFF4a4a6a);
        
        // Draw title area
        fill(poseStack, leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + 22, 0xFF2d2d44);
        
        // Draw energy bar background
        int barX = leftPos + 145;
        int barY = topPos + 5;
        int barWidth = 100;
        int barHeight = 14;
        fill(poseStack, barX, barY, barX + barWidth, barY + barHeight, 0xFF0a0a14);
        
        // Draw energy bar fill
        float energyPct = menu.getEnergyPercentage();
        int fillWidth = (int) (barWidth * energyPct);
        if (fillWidth > 0) {
            // Gradient from cyan to purple
            fill(poseStack, barX + 1, barY + 1, barX + 1 + fillWidth, barY + barHeight - 1, 0xFF00d4ff);
        }
        
        // Draw skill list area
        int listX = leftPos + 10;
        int listY = topPos + 28;
        int listWidth = 130;
        int listHeight = SKILLS_PER_PAGE * SKILL_ENTRY_HEIGHT;
        fill(poseStack, listX, listY, listX + listWidth, listY + listHeight, 0xFF16162a);
        drawBorder(poseStack, listX, listY, listWidth, listHeight, 0xFF3a3a5a);
        
        // Draw details area
        int detailX = leftPos + 145;
        int detailY = topPos + 28;
        int detailWidth = 100;
        int detailHeight = 100;
        fill(poseStack, detailX, detailY, detailX + detailWidth, detailY + detailHeight, 0xFF16162a);
        drawBorder(poseStack, detailX, detailY, detailWidth, detailHeight, 0xFF3a3a5a);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Title
        drawString(poseStack, font, "§6§lSKILL FORGE", 10, -12, 0xFFFFFF);
        
        // Energy display
        double energy = menu.getSkillEnergy();
        double maxEnergy = menu.getMaxEnergy();
        String energyText = String.format("%.0f / %.0f", energy, maxEnergy);
        drawCenteredString(poseStack, font, energyText, 195, 8, 0x00d4ff);
        
        // Tab indicator
        String tabName = tabIndex == 0 ? "§e▶ Breakdown" : "§a▶ Create";
        drawString(poseStack, font, tabName, 10, 28, 0xFFFFFF);
        
        // Render skill list
        int listY = 40;
        if (tabIndex == 0) {
            renderSkillList(poseStack, listY, mouseX, mouseY);
        } else {
            renderCreatableList(poseStack, listY, mouseX, mouseY);
        }
        
        // Render details panel
        renderDetailsPanel(poseStack);
    }

    private void renderSkillList(PoseStack poseStack, int startY, int mouseX, int mouseY) {
        for (int i = 0; i < SKILLS_PER_PAGE && (i + scrollOffset) < playerSkills.size(); i++) {
            ManasSkillInstance skill = playerSkills.get(i + scrollOffset);
            int y = startY + (i * SKILL_ENTRY_HEIGHT);
            
            boolean isSelected = skill == selectedSkill;
            boolean isHovered = isMouseOverEntry(mouseX - leftPos, mouseY - topPos, 10, y, 130, SKILL_ENTRY_HEIGHT);
            
            // Background
            int bgColor = isSelected ? 0xFF3a5a8a : (isHovered ? 0xFF2a3a5a : 0xFF1a2a3a);
            fill(poseStack, 10, y, 140, y + SKILL_ENTRY_HEIGHT - 2, bgColor);
            
            // Skill name with tier color
            String tierColor = SkillEnergyCalculator.getTierColor(skill.getSkill());
            drawString(poseStack, font, tierColor + skill.getSkill().getName().getString(), 14, y + 3, 0xFFFFFF);
            
            // Mastery
            drawString(poseStack, font, "§7Mastery: §f" + skill.getMastery() + "%", 14, y + 12, 0xAAAAAA);
        }
        
        // Scroll indicator
        if (playerSkills.size() > SKILLS_PER_PAGE) {
            drawString(poseStack, font, "§7[Scroll: " + (scrollOffset + 1) + "/" + (maxScroll + 1) + "]", 10, startY + (SKILLS_PER_PAGE * SKILL_ENTRY_HEIGHT) + 2, 0x888888);
        }
    }

    private void renderCreatableList(PoseStack poseStack, int startY, int mouseX, int mouseY) {
        for (int i = 0; i < SKILLS_PER_PAGE && (i + scrollOffset) < creatableSkills.size(); i++) {
            ManasSkill skill = creatableSkills.get(i + scrollOffset);
            int y = startY + (i * SKILL_ENTRY_HEIGHT);
            
            boolean isSelected = skill == selectedCreateSkill;
            boolean isHovered = isMouseOverEntry(mouseX - leftPos, mouseY - topPos, 10, y, 130, SKILL_ENTRY_HEIGHT);
            
            // Background
            int bgColor = isSelected ? 0xFF5a3a8a : (isHovered ? 0xFF3a2a5a : 0xFF2a1a3a);
            fill(poseStack, 10, y, 140, y + SKILL_ENTRY_HEIGHT - 2, bgColor);
            
            // Skill name
            String tierColor = SkillEnergyCalculator.getTierColor(skill);
            drawString(poseStack, font, tierColor + skill.getName().getString(), 14, y + 3, 0xFFFFFF);
            
            // Cost preview
            double cost = SkillEnergyCalculator.calculateCreationCost(skill);
            drawString(poseStack, font, "§bCost: §f" + String.format("%.0f", cost), 14, y + 12, 0xAAAAAA);
        }
        
        // Scroll indicator
        if (creatableSkills.size() > SKILLS_PER_PAGE) {
            drawString(poseStack, font, "§7[Scroll: " + (scrollOffset + 1) + "/" + (maxScroll + 1) + "]", 10, startY + (SKILLS_PER_PAGE * SKILL_ENTRY_HEIGHT) + 2, 0x888888);
        }
    }

    private void renderDetailsPanel(PoseStack poseStack) {
        int x = 148;
        int y = 32;
        
        if (tabIndex == 0 && selectedSkill != null) {
            // Breakdown details
            drawString(poseStack, font, "§e§lSelected:", x, y, 0xFFFFFF);
            drawString(poseStack, font, selectedSkill.getSkill().getName().getString(), x, y + 12, 0xFFFFFF);
            
            String tier = SkillEnergyCalculator.getTierName(selectedSkill.getSkill());
            drawString(poseStack, font, "§7Tier: §f" + tier, x, y + 26, 0xAAAAAA);
            drawString(poseStack, font, "§7Mastery: §f" + selectedSkill.getMastery() + "%", x, y + 38, 0xAAAAAA);
            
            double fullEnergy = SkillEnergyCalculator.calculateBreakdownEnergy(selectedSkill, true);
            double partialEnergy = SkillEnergyCalculator.calculateBreakdownEnergy(selectedSkill, false);
            
            drawString(poseStack, font, "§aFull: §b+" + String.format("%.0f", fullEnergy), x, y + 54, 0x00FF00);
            drawString(poseStack, font, "§eExtract: §b+" + String.format("%.0f", partialEnergy), x, y + 66, 0xFFFF00);
            
        } else if (tabIndex == 1 && selectedCreateSkill != null) {
            // Creation details
            drawString(poseStack, font, "§a§lForge:", x, y, 0xFFFFFF);
            drawString(poseStack, font, selectedCreateSkill.getName().getString(), x, y + 12, 0xFFFFFF);
            
            String tier = SkillEnergyCalculator.getTierName(selectedCreateSkill);
            drawString(poseStack, font, "§7Tier: §f" + tier, x, y + 26, 0xAAAAAA);
            
            double energyCost = SkillEnergyCalculator.calculateCreationCost(selectedCreateSkill);
            double epCost = SkillEnergyCalculator.calculateEPCost(selectedCreateSkill);
            
            drawString(poseStack, font, "§bEnergy: §f" + String.format("%.0f", energyCost), x, y + 42, 0x00FFFF);
            drawString(poseStack, font, "§eEP: §f" + String.format("%.0f", epCost), x, y + 54, 0xFFFF00);
            
            // Affordability check
            double currentEnergy = menu.getSkillEnergy();
            String afford = currentEnergy >= energyCost ? "§a✓ Affordable" : "§c✗ Need more energy";
            drawString(poseStack, font, afford, x, y + 70, 0xFFFFFF);
            
        } else {
            drawString(poseStack, font, "§7Select a skill", x, y + 30, 0x888888);
        }
    }

    private boolean isMouseOverEntry(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void drawBorder(PoseStack poseStack, int x, int y, int width, int height, int color) {
        // Top
        fill(poseStack, x, y, x + width, y + 1, color);
        // Bottom
        fill(poseStack, x, y + height - 1, x + width, y + height, color);
        // Left
        fill(poseStack, x, y, x + 1, y + height, color);
        // Right
        fill(poseStack, x + width - 1, y, x + width, y + height, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle skill selection
        int listX = leftPos + 10;
        int listY = topPos + 40;
        int listWidth = 130;
        
        if (mouseX >= listX && mouseX < listX + listWidth) {
            int relY = (int) (mouseY - listY);
            if (relY >= 0 && relY < SKILLS_PER_PAGE * SKILL_ENTRY_HEIGHT) {
                int index = relY / SKILL_ENTRY_HEIGHT + scrollOffset;
                
                if (tabIndex == 0 && index < playerSkills.size()) {
                    selectedSkill = playerSkills.get(index);
                } else if (tabIndex == 1 && index < creatableSkills.size()) {
                    selectedCreateSkill = creatableSkills.get(index);
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && scrollOffset > 0) {
            scrollOffset--;
        } else if (delta < 0 && scrollOffset < maxScroll) {
            scrollOffset++;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
}

