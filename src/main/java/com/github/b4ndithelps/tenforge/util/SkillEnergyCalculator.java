package com.github.b4ndithelps.tenforge.util;

import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.tensura.ability.skill.Skill;

/**
 * Utility class for calculating skill energy values.
 * 
 * Energy is based on:
 * - Skill tier (Common < Extra < Unique < Ultimate)
 * - Mastery level (higher = more energy)
 * - Full vs partial breakdown
 * 
 * This follows Tensura lore where skills have inherent "power" based on their rank.
 */
public class SkillEnergyCalculator {

    // Base energy values by skill tier
    private static final double COMMON_BASE = 500;
    private static final double INTRINSIC_BASE = 1000;
    private static final double EXTRA_BASE = 2500;
    private static final double UNIQUE_BASE = 10000;
    private static final double ULTIMATE_BASE = 50000;
    private static final double RESISTANCE_BASE = 1500;

    // EP costs for creation (multiplier of base energy)
    private static final double EP_COST_MULTIPLIER = 2.0;

    /**
     * Calculate how much energy is yielded when breaking down a skill.
     * 
     * @param skill The skill being broken down
     * @param fullBreakdown True if destroying the skill entirely
     * @return Energy yield
     */
    public static double calculateBreakdownEnergy(ManasSkillInstance skill, boolean fullBreakdown) {
        double baseEnergy = getBaseEnergy(skill.getSkill());
        
        // Mastery bonus (0-100% bonus based on mastery)
        double masteryBonus = 1.0 + (skill.getMastery() / 100.0);
        
        // Full breakdown gives full value, partial gives 25%
        double breakdownMultiplier = fullBreakdown ? 1.0 : 0.25;
        
        return baseEnergy * masteryBonus * breakdownMultiplier;
    }

    /**
     * Calculate energy cost to create/forge a skill.
     * Creating costs more than breaking down yields to prevent infinite loops.
     * 
     * @param skill The skill to create
     * @return Energy cost
     */
    public static double calculateCreationCost(ManasSkill skill) {
        double baseEnergy = getBaseEnergy(skill);
        // Creating costs 1.5x the base value
        return baseEnergy * 1.5;
    }

    /**
     * Calculate EP cost to create a skill.
     * Higher tier skills require more EP.
     * 
     * @param skill The skill to create
     * @return EP cost
     */
    public static double calculateEPCost(ManasSkill skill) {
        double baseEnergy = getBaseEnergy(skill);
        return baseEnergy * EP_COST_MULTIPLIER;
    }

    /**
     * Get the base energy value for a skill based on its type/tier.
     */
    public static double getBaseEnergy(ManasSkill manasSkill) {
        if (manasSkill instanceof Skill skill) {
            return switch (skill.getSkillType()) {
                case COMMON -> COMMON_BASE;
                case INTRINSIC -> INTRINSIC_BASE;
                case EXTRA -> EXTRA_BASE;
                case UNIQUE -> UNIQUE_BASE;
                case ULTIMATE -> ULTIMATE_BASE;
                case RESISTANCE -> RESISTANCE_BASE;
            };
        }
        // Default for unknown skill types
        return COMMON_BASE;
    }

    /**
     * Get a display name for the skill tier.
     */
    public static String getTierName(ManasSkill manasSkill) {
        if (manasSkill instanceof Skill skill) {
            return switch (skill.getSkillType()) {
                case COMMON -> "Common";
                case INTRINSIC -> "Intrinsic";
                case EXTRA -> "Extra";
                case UNIQUE -> "Unique";
                case ULTIMATE -> "Ultimate";
                case RESISTANCE -> "Resistance";
            };
        }
        return "Unknown";
    }

    /**
     * Get color code for skill tier display.
     */
    public static String getTierColor(ManasSkill manasSkill) {
        if (manasSkill instanceof Skill skill) {
            return switch (skill.getSkillType()) {
                case COMMON -> "§7";      // Gray
                case INTRINSIC -> "§a";   // Green
                case EXTRA -> "§b";       // Aqua
                case UNIQUE -> "§e";      // Yellow
                case ULTIMATE -> "§6";    // Gold
                case RESISTANCE -> "§d";  // Light Purple
            };
        }
        return "§f";
    }
}

