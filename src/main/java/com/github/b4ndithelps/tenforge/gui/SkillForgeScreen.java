package com.github.b4ndithelps.tenforge.gui;

import com.github.b4ndithelps.tenforge.Tenforge;
import com.github.b4ndithelps.tenforge.data.SkillRegistry;
import com.github.b4ndithelps.tenforge.network.ModNetwork;
import com.github.b4ndithelps.tenforge.network.SkillBreakdownPacket;
import com.github.b4ndithelps.tenforge.network.SkillCreatePacket;
import com.github.b4ndithelps.tenforge.util.SkillEnergyCalculator;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.tensura.ability.skill.Skill;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Skill Forge GUI Screen - Optimized Version
 * 
 * Features:
 * - Cached skill lists via SkillRegistry
 * - Search functionality
 * - Tier filtering
 * - Smooth scrolling
 * - Cross-mod compatibility
 */
public class SkillForgeScreen extends AbstractContainerScreen<SkillForgeMenu> {

    // GUI dimensions
    private static final int GUI_WIDTH = 280;
    private static final int GUI_HEIGHT = 220;
    
    // Scroll and selection state
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SKILLS_PER_PAGE = 6;
    private static final int SKILL_ENTRY_HEIGHT = 22;
    
    // Selection
    private ManasSkillInstance selectedBreakdownSkill = null;
    private SkillRegistry.SkillData selectedCreateSkill = null;
    private int tabIndex = 0; // 0 = Breakdown, 1 = Create
    
    // Filter state
    private Skill.SkillType tierFilter = null; // null = all tiers
    private String searchQuery = "";
    
    // Cached lists (updated when needed)
    private List<ManasSkillInstance> breakdownList = new ArrayList<>();
    private List<SkillRegistry.SkillData> createList = new ArrayList<>();
    private long lastListUpdate = 0;
    
    // UI components
    private EditBox searchBox;
    private Button breakdownTab;
    private Button createTab;
    private Button fullBreakdownBtn;
    private Button extractBtn;
    private Button forgeBtn;

    public SkillForgeScreen(SkillForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = this.imageHeight + 100; // Hide default label
        this.titleLabelY = -100; // Hide default title
    }

    @Override
    protected void init() {
        super.init();
        
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        // Search box
        searchBox = new EditBox(font, leftPos + 10, topPos + 24, 120, 14, Component.literal("Search"));
        searchBox.setMaxLength(32);
        searchBox.setResponder(this::onSearchChanged);
        searchBox.setBordered(true);
        addRenderableWidget(searchBox);
        
        // Tab buttons
        breakdownTab = addRenderableWidget(Button.builder(Component.literal("§c⚔ Breakdown"), btn -> {
            tabIndex = 0;
            selectedBreakdownSkill = null;
            scrollOffset = 0;
            refreshLists();
        }).bounds(leftPos + 10, topPos + 5, 70, 16).build());
        
        createTab = addRenderableWidget(Button.builder(Component.literal("§a✦ Create"), btn -> {
            tabIndex = 1;
            selectedCreateSkill = null;
            scrollOffset = 0;
            refreshLists();
        }).bounds(leftPos + 85, topPos + 5, 60, 16).build());
        
        // Tier filter buttons (right side)
        int filterX = leftPos + 150;
        addRenderableWidget(Button.builder(Component.literal("All"), btn -> {
            tierFilter = null;
            scrollOffset = 0;
            refreshLists();
        }).bounds(filterX, topPos + 5, 25, 16).build());
        
        addRenderableWidget(Button.builder(Component.literal("§7C"), btn -> {
            tierFilter = Skill.SkillType.COMMON;
            scrollOffset = 0;
            refreshLists();
        }).bounds(filterX + 27, topPos + 5, 18, 16).build());
        
        addRenderableWidget(Button.builder(Component.literal("§eU"), btn -> {
            tierFilter = Skill.SkillType.UNIQUE;
            scrollOffset = 0;
            refreshLists();
        }).bounds(filterX + 47, topPos + 5, 18, 16).build());
        
        addRenderableWidget(Button.builder(Component.literal("§6Ult"), btn -> {
            tierFilter = Skill.SkillType.ULTIMATE;
            scrollOffset = 0;
            refreshLists();
        }).bounds(filterX + 67, topPos + 5, 25, 16).build());
        
        // Action buttons
        fullBreakdownBtn = addRenderableWidget(Button.builder(
                Component.literal("§c✗ Full Breakdown"), 
                btn -> handleFullBreakdown()
        ).bounds(leftPos + 10, topPos + GUI_HEIGHT - 28, 90, 20).build());
        
        extractBtn = addRenderableWidget(Button.builder(
                Component.literal("§e◈ Extract"), 
                btn -> handleExtract()
        ).bounds(leftPos + 105, topPos + GUI_HEIGHT - 28, 60, 20).build());
        
        forgeBtn = addRenderableWidget(Button.builder(
                Component.literal("§a⚒ Forge"), 
                btn -> handleForge()
        ).bounds(leftPos + 170, topPos + GUI_HEIGHT - 28, 100, 20).build());
        
        // Initial data load
        refreshLists();
    }

    private void onSearchChanged(String query) {
        this.searchQuery = query.toLowerCase();
        scrollOffset = 0;
        refreshLists();
    }

    private void refreshLists() {
        SkillRegistry registry = SkillRegistry.get();
        
        if (tabIndex == 0) {
            // Breakdown list - player's skills
            breakdownList = new ArrayList<>(menu.getPlayerSkills());
            
            // Apply search filter
            if (!searchQuery.isEmpty()) {
                breakdownList.removeIf(s -> 
                        !s.getSkill().getName().getString().toLowerCase().contains(searchQuery));
            }
            
            // Apply tier filter
            if (tierFilter != null) {
                breakdownList.removeIf(s -> {
                    if (s.getSkill() instanceof Skill skill) {
                        return skill.getSkillType() != tierFilter;
                    }
                    return true;
                });
            }
            
            maxScroll = Math.max(0, breakdownList.size() - SKILLS_PER_PAGE);
        } else {
            // Create list - skills player doesn't have
            List<SkillRegistry.SkillData> allUnowned = registry.getUnownedSkills(menu.getPlayer());
            createList = new ArrayList<>();
            
            for (SkillRegistry.SkillData data : allUnowned) {
                // Apply search filter
                if (!searchQuery.isEmpty() && !data.name.toLowerCase().contains(searchQuery)) {
                    continue;
                }
                // Apply tier filter
                if (tierFilter != null && data.tier != tierFilter) {
                    continue;
                }
                createList.add(data);
            }
            
            maxScroll = Math.max(0, createList.size() - SKILLS_PER_PAGE);
        }
        
        lastListUpdate = System.currentTimeMillis();
    }

    private void handleFullBreakdown() {
        if (selectedBreakdownSkill != null) {
            ModNetwork.sendToServer(new SkillBreakdownPacket(
                    selectedBreakdownSkill.getSkill().getRegistryName().toString(), true));
            selectedBreakdownSkill = null;
            // Delay refresh to allow server to process
            lastListUpdate = 0;
        }
    }

    private void handleExtract() {
        if (selectedBreakdownSkill != null) {
            ModNetwork.sendToServer(new SkillBreakdownPacket(
                    selectedBreakdownSkill.getSkill().getRegistryName().toString(), false));
            // Delay refresh
            lastListUpdate = 0;
        }
    }

    private void handleForge() {
        if (selectedCreateSkill != null) {
            ModNetwork.sendToServer(new SkillCreatePacket(selectedCreateSkill.id.toString()));
            selectedCreateSkill = null;
            lastListUpdate = 0;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        searchBox.tick();
        
        // Auto-refresh lists periodically
        if (System.currentTimeMillis() - lastListUpdate > 2000) {
            refreshLists();
        }
        
        // Update button states
        fullBreakdownBtn.active = tabIndex == 0 && selectedBreakdownSkill != null;
        extractBtn.active = tabIndex == 0 && selectedBreakdownSkill != null;
        forgeBtn.active = tabIndex == 1 && selectedCreateSkill != null;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        // Main background - dark theme
        fill(poseStack, leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xEE0f0f1a);
        
        // Outer border - gradient effect
        drawGradientBorder(poseStack, leftPos, topPos, imageWidth, imageHeight);
        
        // Energy bar area
        int barX = leftPos + 10;
        int barY = topPos + 42;
        int barWidth = 260;
        int barHeight = 12;
        
        // Bar background
        fill(poseStack, barX, barY, barX + barWidth, barY + barHeight, 0xFF0a0a14);
        drawBorder(poseStack, barX, barY, barWidth, barHeight, 0xFF2a2a4a);
        
        // Bar fill
        float energyPct = menu.getEnergyPercentage();
        int fillWidth = (int) ((barWidth - 2) * energyPct);
        if (fillWidth > 0) {
            // Cyan to purple gradient
            fillGradient(poseStack, barX + 1, barY + 1, barX + 1 + fillWidth, barY + barHeight - 1, 
                    0xFF00ffff, 0xFF8800ff);
        }
        
        // List area
        int listX = leftPos + 10;
        int listY = topPos + 58;
        int listWidth = 155;
        int listHeight = SKILLS_PER_PAGE * SKILL_ENTRY_HEIGHT + 4;
        fill(poseStack, listX, listY, listX + listWidth, listY + listHeight, 0xFF12121f);
        drawBorder(poseStack, listX, listY, listWidth, listHeight, 0xFF3a3a5a);
        
        // Details panel
        int detailX = leftPos + 170;
        int detailY = topPos + 58;
        int detailWidth = 100;
        int detailHeight = 125;
        fill(poseStack, detailX, detailY, detailX + detailWidth, detailY + detailHeight, 0xFF12121f);
        drawBorder(poseStack, detailX, detailY, detailWidth, detailHeight, 0xFF3a3a5a);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Title
        drawString(poseStack, font, "§6§lSKILL FORGE", 10, -14, 0xFFFFFF);
        
        // Energy display
        double energy = menu.getSkillEnergy();
        double maxEnergy = menu.getMaxEnergy();
        String energyText = String.format("§bEnergy: §f%.0f §7/ §f%.0f", energy, maxEnergy);
        drawString(poseStack, font, energyText, 12, 46, 0xFFFFFF);
        
        // Skills analyzed count
        drawString(poseStack, font, "§7Analyzed: §e" + menu.getSkillsAnalyzed(), 180, 46, 0xAAAAAA);
        
        // Tab indicator
        String tabIndicator = tabIndex == 0 ? "§c▶ Breaking Down Skills" : "§a▶ Creating New Skills";
        drawString(poseStack, font, tabIndicator, 12, 60, 0xFFFFFF);
        
        // Render skill list
        int listY = 72;
        if (tabIndex == 0) {
            renderBreakdownList(poseStack, listY, mouseX, mouseY);
        } else {
            renderCreateList(poseStack, listY, mouseX, mouseY);
        }
        
        // Render details panel
        renderDetailsPanel(poseStack);
        
        // Scroll indicator
        int totalItems = tabIndex == 0 ? breakdownList.size() : createList.size();
        if (totalItems > SKILLS_PER_PAGE) {
            String scrollText = String.format("§8[%d/%d]", scrollOffset + 1, maxScroll + 1);
            drawString(poseStack, font, scrollText, 130, 60, 0x888888);
        }
    }

    private void renderBreakdownList(PoseStack poseStack, int startY, int mouseX, int mouseY) {
        for (int i = 0; i < SKILLS_PER_PAGE && (i + scrollOffset) < breakdownList.size(); i++) {
            ManasSkillInstance skill = breakdownList.get(i + scrollOffset);
            int y = startY + (i * SKILL_ENTRY_HEIGHT);
            
            boolean isSelected = skill == selectedBreakdownSkill;
            boolean isHovered = isMouseOverEntry(mouseX - leftPos, mouseY - topPos, 10, y, 155, SKILL_ENTRY_HEIGHT);
            
            // Background with selection/hover effect
            int bgColor = isSelected ? 0xFF4a2a2a : (isHovered ? 0xFF2a2020 : 0xFF1a1a24);
            fill(poseStack, 10, y, 165, y + SKILL_ENTRY_HEIGHT - 2, bgColor);
            
            if (isSelected) {
                drawBorder(poseStack, 10, y, 155, SKILL_ENTRY_HEIGHT - 2, 0xFFff4444);
            }
            
            // Tier indicator
            String tierColor = SkillEnergyCalculator.getTierColor(skill.getSkill());
            String tierSymbol = getTierSymbol(skill.getSkill());
            drawString(poseStack, font, tierColor + tierSymbol, 14, y + 3, 0xFFFFFF);
            
            // Skill name
            String name = skill.getSkill().getName().getString();
            if (name.length() > 16) name = name.substring(0, 14) + "..";
            drawString(poseStack, font, tierColor + name, 26, y + 3, 0xFFFFFF);
            
            // Mastery bar
            int masteryWidth = (int) (40 * (skill.getMastery() / 100.0));
            fill(poseStack, 14, y + 13, 14 + 40, y + 17, 0xFF0a0a14);
            if (masteryWidth > 0) {
                fill(poseStack, 14, y + 13, 14 + masteryWidth, y + 17, 0xFF44ff44);
            }
            drawString(poseStack, font, "§7" + skill.getMastery() + "%", 58, y + 11, 0xAAAAAA);
            
            // Energy value preview
            double energyValue = SkillEnergyCalculator.calculateBreakdownEnergy(skill, true);
            drawString(poseStack, font, "§b+" + formatNumber(energyValue), 110, y + 7, 0x00FFFF);
        }
    }

    private void renderCreateList(PoseStack poseStack, int startY, int mouseX, int mouseY) {
        // Note: mouseX/mouseY are already local coords (relative to GUI) from renderLabels
        double currentEnergy = menu.getSkillEnergy();
        
        for (int i = 0; i < SKILLS_PER_PAGE && (i + scrollOffset) < createList.size(); i++) {
            SkillRegistry.SkillData data = createList.get(i + scrollOffset);
            int y = startY + (i * SKILL_ENTRY_HEIGHT);
            
            boolean isSelected = data == selectedCreateSkill;
            boolean isHovered = isMouseOverEntry(mouseX, mouseY, 10, y, 155, SKILL_ENTRY_HEIGHT);
            boolean canAfford = currentEnergy >= data.creationCost;
            
            // Background
            int bgColor;
            if (isSelected) {
                bgColor = canAfford ? 0xFF2a4a2a : 0xFF4a2a2a;
            } else if (isHovered) {
                bgColor = 0xFF202a20;
            } else {
                bgColor = 0xFF1a1a24;
            }
            fill(poseStack, 10, y, 165, y + SKILL_ENTRY_HEIGHT - 2, bgColor);
            
            if (isSelected) {
                drawBorder(poseStack, 10, y, 155, SKILL_ENTRY_HEIGHT - 2, 
                        canAfford ? 0xFF44ff44 : 0xFFff4444);
            }
            
            // Tier indicator
            String tierColor = SkillEnergyCalculator.getTierColor(data.skill);
            String tierSymbol = getTierSymbolFromType(data.tier);
            drawString(poseStack, font, tierColor + tierSymbol, 14, y + 3, 0xFFFFFF);
            
            // Skill name
            String name = data.name;
            if (name.length() > 14) name = name.substring(0, 12) + "..";
            drawString(poseStack, font, tierColor + name, 26, y + 3, 0xFFFFFF);
            
            // Cost
            String costColor = canAfford ? "§a" : "§c";
            drawString(poseStack, font, costColor + formatNumber(data.creationCost), 14, y + 12, 0xFFFFFF);
            
            // Mod source
            drawString(poseStack, font, "§8" + data.modId, 90, y + 12, 0x666666);
        }
    }

    private void renderDetailsPanel(PoseStack poseStack) {
        int x = 173;
        int y = 62;
        
        if (tabIndex == 0 && selectedBreakdownSkill != null) {
            ManasSkillInstance skill = selectedBreakdownSkill;
            
            drawString(poseStack, font, "§e§lSelected", x, y, 0xFFFFFF);
            
            // Skill name
            String name = skill.getSkill().getName().getString();
            if (name.length() > 12) name = name.substring(0, 10) + "..";
            drawString(poseStack, font, "§f" + name, x, y + 12, 0xFFFFFF);
            
            // Tier
            String tier = SkillEnergyCalculator.getTierName(skill.getSkill());
            String tierColor = SkillEnergyCalculator.getTierColor(skill.getSkill());
            drawString(poseStack, font, "§7Tier: " + tierColor + tier, x, y + 26, 0xAAAAAA);
            
            // Mastery
            drawString(poseStack, font, "§7Mastery: §f" + skill.getMastery() + "%", x, y + 38, 0xAAAAAA);
            
            drawString(poseStack, font, "§7─────────", x, y + 48, 0x444444);
            
            // Energy yields
            double fullEnergy = SkillEnergyCalculator.calculateBreakdownEnergy(skill, true);
            double partialEnergy = SkillEnergyCalculator.calculateBreakdownEnergy(skill, false);
            
            drawString(poseStack, font, "§cFull:", x, y + 58, 0xFF4444);
            drawString(poseStack, font, "§b+" + formatNumber(fullEnergy), x + 30, y + 58, 0x00FFFF);
            
            drawString(poseStack, font, "§eExtract:", x, y + 70, 0xFFFF44);
            drawString(poseStack, font, "§b+" + formatNumber(partialEnergy), x + 45, y + 70, 0x00FFFF);
            
            drawString(poseStack, font, "§8-25% mastery", x, y + 82, 0x666666);
            
        } else if (tabIndex == 1 && selectedCreateSkill != null) {
            SkillRegistry.SkillData data = selectedCreateSkill;
            double currentEnergy = menu.getSkillEnergy();
            boolean canAfford = currentEnergy >= data.creationCost;
            
            drawString(poseStack, font, "§a§lForge", x, y, 0xFFFFFF);
            
            // Skill name
            String name = data.name;
            if (name.length() > 12) name = name.substring(0, 10) + "..";
            drawString(poseStack, font, "§f" + name, x, y + 12, 0xFFFFFF);
            
            // Tier
            String tierColor = SkillEnergyCalculator.getTierColor(data.skill);
            drawString(poseStack, font, "§7Tier: " + tierColor + SkillEnergyCalculator.getTierName(data.skill), x, y + 26, 0xAAAAAA);
            
            // Source mod
            drawString(poseStack, font, "§7Mod: §8" + data.modId, x, y + 38, 0xAAAAAA);
            
            drawString(poseStack, font, "§7─────────", x, y + 48, 0x444444);
            
            // Costs
            String costColor = canAfford ? "§a" : "§c";
            drawString(poseStack, font, "§7Energy:", x, y + 58, 0xAAAAAA);
            drawString(poseStack, font, costColor + formatNumber(data.creationCost), x + 45, y + 58, 0xFFFFFF);
            
            drawString(poseStack, font, "§7EP:", x, y + 70, 0xAAAAAA);
            drawString(poseStack, font, "§e" + formatNumber(data.epCost), x + 20, y + 70, 0xFFFF00);
            
            // Affordability
            String status = canAfford ? "§a✓ Ready" : "§c✗ Need more";
            drawString(poseStack, font, status, x, y + 86, 0xFFFFFF);
            
        } else {
            drawString(poseStack, font, "§7Select a", x + 15, y + 40, 0x888888);
            drawString(poseStack, font, "§7skill...", x + 20, y + 52, 0x888888);
        }
    }

    private String getTierSymbol(com.github.manasmods.manascore.api.skills.ManasSkill skill) {
        if (skill instanceof Skill s) {
            return getTierSymbolFromType(s.getSkillType());
        }
        return "○";
    }

    private String getTierSymbolFromType(Skill.SkillType tier) {
        return switch (tier) {
            case COMMON -> "○";
            case INTRINSIC -> "◇";
            case EXTRA -> "◆";
            case RESISTANCE -> "◈";
            case UNIQUE -> "★";
            case ULTIMATE -> "✦";
        };
    }

    private String formatNumber(double value) {
        if (value >= 1000000) {
            return String.format("%.1fM", value / 1000000);
        } else if (value >= 1000) {
            return String.format("%.1fK", value / 1000);
        }
        return String.format("%.0f", value);
    }

    private boolean isMouseOverEntry(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void drawBorder(PoseStack poseStack, int x, int y, int width, int height, int color) {
        fill(poseStack, x, y, x + width, y + 1, color);
        fill(poseStack, x, y + height - 1, x + width, y + height, color);
        fill(poseStack, x, y, x + 1, y + height, color);
        fill(poseStack, x + width - 1, y, x + width, y + height, color);
    }

    private void drawGradientBorder(PoseStack poseStack, int x, int y, int width, int height) {
        // Outer glow effect
        fill(poseStack, x - 1, y - 1, x + width + 1, y, 0xFF3a3a6a);
        fill(poseStack, x - 1, y + height, x + width + 1, y + height + 1, 0xFF3a3a6a);
        fill(poseStack, x - 1, y, x, y + height, 0xFF3a3a6a);
        fill(poseStack, x + width, y, x + width + 1, y + height, 0xFF3a3a6a);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = leftPos + 10;
        int listY = topPos + 72;
        int listWidth = 155;
        
        if (mouseX >= listX && mouseX < listX + listWidth) {
            int relY = (int) (mouseY - listY);
            if (relY >= 0 && relY < SKILLS_PER_PAGE * SKILL_ENTRY_HEIGHT) {
                int index = relY / SKILL_ENTRY_HEIGHT + scrollOffset;
                
                if (tabIndex == 0 && index < breakdownList.size()) {
                    selectedBreakdownSkill = breakdownList.get(index);
                } else if (tabIndex == 1 && index < createList.size()) {
                    selectedCreateSkill = createList.get(index);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox.isFocused()) {
            return searchBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
}