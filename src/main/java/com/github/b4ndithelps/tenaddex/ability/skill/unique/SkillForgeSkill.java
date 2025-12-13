package com.github.b4ndithelps.tenaddex.ability.skill.unique;

import com.github.b4ndithelps.tenaddex.TensuraAddonExample;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.ability.SkillUtils;
import com.github.manasmods.tensura.ability.TensuraSkillInstance;
import com.github.manasmods.tensura.ability.skill.Skill;
import com.github.manasmods.tensura.capability.ep.EPStorage;
import com.github.manasmods.tensura.registry.skill.CommonSkills;
import com.github.manasmods.tensura.registry.skill.ExtraSkills;
import com.github.manasmods.tensura.registry.skill.UniqueSkills;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Unique Skill: Skill Forge
 * 
 * Inspired by skills like Raphael and Great Sage from "That Time I Got Reincarnated as a Slime",
 * this skill allows the user to analyze, break down, and synthesize skills.
 * 
 * Mode 1 - ANALYZE: Target a skill in your possession to extract its essence components
 * Mode 2 - SYNTHESIZE: Combine stored essences to create or upgrade skills
 * Mode 3 - REPLICATE: Create a temporary manifestation of an analyzed skill's power
 * 
 * The skill works with an "essence" system where breaking down skills yields various
 * essence types that can be recombined to forge new abilities.
 */
public class SkillForgeSkill extends Skill {

    // Skill configuration
    private final double learnCost = 8.0;
    private final int numModes = 3;
    
    // Essence types that can be extracted from skills
    public static final String ESSENCE_OFFENSIVE = "offensive";
    public static final String ESSENCE_DEFENSIVE = "defensive";
    public static final String ESSENCE_UTILITY = "utility";
    public static final String ESSENCE_ELEMENTAL = "elemental";
    public static final String ESSENCE_SPIRITUAL = "spiritual";
    public static final String ESSENCE_CHAOS = "chaos";
    
    // NBT keys
    private static final String TAG_ESSENCES = "ForgeEssences";
    private static final String TAG_ANALYZED_SKILLS = "AnalyzedSkills";
    private static final String TAG_SYNTHESIS_PROGRESS = "SynthesisProgress";
    private static final String TAG_LAST_ANALYZED = "LastAnalyzedSkill";
    
    // Synthesis recipes (essence requirements for creating skills)
    private static final Map<String, Map<String, Integer>> SYNTHESIS_RECIPES = new HashMap<>();
    
    static {
        // Define synthesis recipes: skill_id -> {essence_type -> count}
        // These are example recipes - more can be added
        Map<String, Integer> waterBladeRecipe = new HashMap<>();
        waterBladeRecipe.put(ESSENCE_OFFENSIVE, 3);
        waterBladeRecipe.put(ESSENCE_ELEMENTAL, 2);
        SYNTHESIS_RECIPES.put("tensura:water_blade", waterBladeRecipe);
        
        Map<String, Integer> fireballRecipe = new HashMap<>();
        fireballRecipe.put(ESSENCE_OFFENSIVE, 2);
        fireballRecipe.put(ESSENCE_ELEMENTAL, 3);
        fireballRecipe.put(ESSENCE_CHAOS, 1);
        SYNTHESIS_RECIPES.put("tensura:fire_manipulation", fireballRecipe);
        
        Map<String, Integer> barrierRecipe = new HashMap<>();
        barrierRecipe.put(ESSENCE_DEFENSIVE, 4);
        barrierRecipe.put(ESSENCE_SPIRITUAL, 2);
        SYNTHESIS_RECIPES.put("tensura:barrier", barrierRecipe);
        
        Map<String, Integer> thoughtAccelRecipe = new HashMap<>();
        thoughtAccelRecipe.put(ESSENCE_UTILITY, 5);
        thoughtAccelRecipe.put(ESSENCE_SPIRITUAL, 3);
        thoughtAccelRecipe.put(ESSENCE_CHAOS, 2);
        SYNTHESIS_RECIPES.put("tensura:thought_acceleration", thoughtAccelRecipe);
    }

    @Override
    public ResourceLocation getSkillIcon() {
        return new ResourceLocation(TensuraAddonExample.MODID, "textures/skill/unique/skill_forge.png");
    }

    public SkillForgeSkill() {
        super(SkillType.UNIQUE);
    }

    @Override
    public int modes() {
        return numModes;
    }

    @Override
    public int nextMode(LivingEntity entity, TensuraSkillInstance instance, boolean reverse) {
        if (reverse) {
            return instance.getMode() == 1 ? numModes : instance.getMode() - 1;
        } else {
            return instance.getMode() == numModes ? 1 : instance.getMode() + 1;
        }
    }

    @Override
    public Component getModeName(int curMode) {
        MutableComponent name;
        switch (curMode) {
            case 1 -> name = Component.translatable("tenaddex.skill.mode.skill_forge.analyze");
            case 2 -> name = Component.translatable("tenaddex.skill.mode.skill_forge.synthesize");
            case 3 -> name = Component.translatable("tenaddex.skill.mode.skill_forge.replicate");
            default -> name = Component.empty();
        }
        return name;
    }

    @Override
    public double magiculeCost(LivingEntity entity, ManasSkillInstance instance) {
        double cost;
        switch (instance.getMode()) {
            case 1 -> cost = 200.0;   // Analyze cost
            case 2 -> cost = 500.0;   // Synthesize cost
            case 3 -> cost = 300.0;   // Replicate cost
            default -> cost = 0.0;
        }
        return cost;
    }

    @Override
    public double learningCost() {
        return learnCost;
    }

    /**
     * Requires high EP and mastery of at least one skill to unlock
     */
    @Override
    public boolean meetEPRequirement(Player entity, double curEP) {
        // Requires 50,000+ EP to unlock this skill
        return curEP >= 50000.0;
    }

    @Override
    public void onPressed(ManasSkillInstance instance, LivingEntity entity) {
        if (SkillHelper.outOfMagicule(entity, instance)) {
            return;
        }

        switch (instance.getMode()) {
            case 1 -> handleAnalyzeMode(instance, entity);
            case 2 -> handleSynthesizeMode(instance, entity);
            case 3 -> handleReplicateMode(instance, entity);
        }
    }

    /**
     * Mode 1: ANALYZE
     * Analyzes skills the player possesses and extracts essence components.
     * The skill breakdown depends on the skill type and mastery level.
     */
    private void handleAnalyzeMode(ManasSkillInstance instance, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }

        CompoundTag tag = instance.getOrCreateTag();
        
        // Get player's skill storage to find skills to analyze
        SkillStorage skillStorage = SkillStorage.get(player);
        if (skillStorage == null) {
            sendMessage(player, "tenaddex.skill.skill_forge.no_skills", ChatFormatting.RED);
            return;
        }

        List<ManasSkillInstance> playerSkills = skillStorage.getLearnedSkills();
        List<String> analyzedSkills = getAnalyzedSkills(tag);
        
        // Find a mastered skill that hasn't been analyzed yet
        ManasSkillInstance targetSkill = null;
        for (ManasSkillInstance skill : playerSkills) {
            if (skill.getSkill() != this && skill.isMastered(entity)) {
                String skillId = skill.getSkill().getRegistryName().toString();
                if (!analyzedSkills.contains(skillId)) {
                    targetSkill = skill;
                    break;
                }
            }
        }

        if (targetSkill == null) {
            // Try to find any skill that's at least 50% mastered
            for (ManasSkillInstance skill : playerSkills) {
                if (skill.getSkill() != this && skill.getMastery() >= 50) {
                    String skillId = skill.getSkill().getRegistryName().toString();
                    if (!analyzedSkills.contains(skillId)) {
                        targetSkill = skill;
                        break;
                    }
                }
            }
        }

        if (targetSkill == null) {
            sendMessage(player, "tenaddex.skill.skill_forge.nothing_to_analyze", ChatFormatting.YELLOW);
            return;
        }

        // Perform analysis - extract essences based on skill properties
        String skillId = targetSkill.getSkill().getRegistryName().toString();
        Map<String, Integer> extractedEssences = analyzeSkillForEssence(targetSkill);
        
        // Store the extracted essences
        addEssences(tag, extractedEssences);
        
        // Mark skill as analyzed
        addAnalyzedSkill(tag, skillId);
        tag.putString(TAG_LAST_ANALYZED, skillId);
        
        // Visual and audio feedback
        playAnalyzeEffects(entity);
        
        // Build message showing extracted essences
        StringBuilder essenceMsg = new StringBuilder();
        for (Map.Entry<String, Integer> entry : extractedEssences.entrySet()) {
            if (essenceMsg.length() > 0) {
                essenceMsg.append(", ");
            }
            essenceMsg.append(entry.getValue()).append("x ").append(formatEssenceName(entry.getKey()));
        }
        
        player.displayClientMessage(
            Component.translatable("tenaddex.skill.skill_forge.analyzed", 
                targetSkill.getSkill().getName(), 
                essenceMsg.toString())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)),
            false
        );
        
        this.addMasteryPoint(instance, entity);
        instance.setCoolDown(5);
    }

    /**
     * Mode 2: SYNTHESIZE
     * Combines stored essences to create or enhance skills.
     */
    private void handleSynthesizeMode(ManasSkillInstance instance, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }

        CompoundTag tag = instance.getOrCreateTag();
        Map<String, Integer> currentEssences = getEssences(tag);
        
        // Display current essence inventory
        if (currentEssences.isEmpty()) {
            sendMessage(player, "tenaddex.skill.skill_forge.no_essences", ChatFormatting.RED);
            return;
        }
        
        // Try to find a recipe we can complete
        String craftableSkill = null;
        Map<String, Integer> recipe = null;
        
        for (Map.Entry<String, Map<String, Integer>> entry : SYNTHESIS_RECIPES.entrySet()) {
            if (canCraftRecipe(currentEssences, entry.getValue())) {
                // Check if player already has this skill
                SkillStorage storage = SkillStorage.get(player);
                boolean hasSkill = false;
                if (storage != null) {
                    for (ManasSkillInstance skill : storage.getLearnedSkills()) {
                        if (skill.getSkill().getRegistryName().toString().equals(entry.getKey())) {
                            hasSkill = true;
                            break;
                        }
                    }
                }
                
                if (!hasSkill) {
                    craftableSkill = entry.getKey();
                    recipe = entry.getValue();
                    break;
                }
            }
        }
        
        if (craftableSkill != null && recipe != null) {
            // Consume essences
            consumeEssences(tag, recipe);
            
            // Grant the synthesized skill (this would need integration with Tensura's skill granting system)
            // For now, we'll just notify the player
            playSynthesizeEffects(entity);
            
            player.displayClientMessage(
                Component.translatable("tenaddex.skill.skill_forge.synthesized", craftableSkill)
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)),
                false
            );
            
            // In a real implementation, you would grant the skill here
            // SkillUtils.grantSkill(player, craftableSkill);
            
            this.addMasteryPoint(instance, entity);
            this.addMasteryPoint(instance, entity); // Double mastery for synthesis
            instance.setCoolDown(10);
        } else {
            // Display current essences and what's needed
            StringBuilder essenceList = new StringBuilder();
            for (Map.Entry<String, Integer> entry : currentEssences.entrySet()) {
                if (essenceList.length() > 0) {
                    essenceList.append(", ");
                }
                essenceList.append(entry.getValue()).append("x ").append(formatEssenceName(entry.getKey()));
            }
            
            player.displayClientMessage(
                Component.translatable("tenaddex.skill.skill_forge.essence_inventory", essenceList.toString())
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)),
                false
            );
            
            sendMessage(player, "tenaddex.skill.skill_forge.cannot_synthesize", ChatFormatting.YELLOW);
        }
    }

    /**
     * Mode 3: REPLICATE
     * Temporarily manifests the power of an analyzed skill.
     */
    private void handleReplicateMode(ManasSkillInstance instance, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }

        CompoundTag tag = instance.getOrCreateTag();
        List<String> analyzedSkills = getAnalyzedSkills(tag);
        
        if (analyzedSkills.isEmpty()) {
            sendMessage(player, "tenaddex.skill.skill_forge.no_analyzed_skills", ChatFormatting.RED);
            return;
        }
        
        // Get the last analyzed skill or a random one
        String lastAnalyzed = tag.getString(TAG_LAST_ANALYZED);
        String targetSkillId = lastAnalyzed.isEmpty() ? 
            analyzedSkills.get(new Random().nextInt(analyzedSkills.size())) : 
            lastAnalyzed;
        
        // Apply a temporary buff based on the skill type
        applyReplicationEffect(player, targetSkillId, instance.isMastered(entity));
        
        playReplicateEffects(entity);
        
        player.displayClientMessage(
            Component.translatable("tenaddex.skill.skill_forge.replicated", targetSkillId)
                .setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)),
            false
        );
        
        this.addMasteryPoint(instance, entity);
        instance.setCoolDown(instance.isMastered(entity) ? 15 : 30);
    }

    /**
     * Analyzes a skill instance and determines what essences it contains.
     */
    private Map<String, Integer> analyzeSkillForEssence(ManasSkillInstance skillInstance) {
        Map<String, Integer> essences = new HashMap<>();
        ManasSkill skill = skillInstance.getSkill();
        
        if (skill instanceof Skill tensuraSkill) {
            // Determine essences based on skill type
            switch (tensuraSkill.getSkillType()) {
                case COMMON -> {
                    essences.put(ESSENCE_UTILITY, 1);
                }
                case INTRINSIC -> {
                    essences.put(ESSENCE_SPIRITUAL, 2);
                }
                case EXTRA -> {
                    essences.put(ESSENCE_UTILITY, 2);
                    essences.put(ESSENCE_OFFENSIVE, 1);
                }
                case UNIQUE -> {
                    essences.put(ESSENCE_SPIRITUAL, 2);
                    essences.put(ESSENCE_CHAOS, 1);
                }
                case ULTIMATE -> {
                    essences.put(ESSENCE_CHAOS, 3);
                    essences.put(ESSENCE_SPIRITUAL, 2);
                    essences.put(ESSENCE_OFFENSIVE, 2);
                }
                case RESISTANCE -> {
                    essences.put(ESSENCE_DEFENSIVE, 3);
                }
            }
        }
        
        // Bonus essences for mastered skills
        if (skillInstance.getMastery() >= 100) {
            essences.put(ESSENCE_CHAOS, essences.getOrDefault(ESSENCE_CHAOS, 0) + 1);
        }
        
        // Add some randomness based on skill name (for variety)
        String skillName = skill.getRegistryName().toString().toLowerCase();
        if (skillName.contains("fire") || skillName.contains("flame") || skillName.contains("burn")) {
            essences.put(ESSENCE_ELEMENTAL, essences.getOrDefault(ESSENCE_ELEMENTAL, 0) + 2);
            essences.put(ESSENCE_OFFENSIVE, essences.getOrDefault(ESSENCE_OFFENSIVE, 0) + 1);
        }
        if (skillName.contains("water") || skillName.contains("ice") || skillName.contains("frost")) {
            essences.put(ESSENCE_ELEMENTAL, essences.getOrDefault(ESSENCE_ELEMENTAL, 0) + 2);
        }
        if (skillName.contains("barrier") || skillName.contains("shield") || skillName.contains("guard")) {
            essences.put(ESSENCE_DEFENSIVE, essences.getOrDefault(ESSENCE_DEFENSIVE, 0) + 2);
        }
        if (skillName.contains("speed") || skillName.contains("haste") || skillName.contains("thought")) {
            essences.put(ESSENCE_UTILITY, essences.getOrDefault(ESSENCE_UTILITY, 0) + 2);
        }
        
        // Ensure at least 1 essence
        if (essences.isEmpty()) {
            essences.put(ESSENCE_UTILITY, 1);
        }
        
        return essences;
    }

    /**
     * Applies temporary effects based on the replicated skill type.
     */
    private void applyReplicationEffect(Player player, String skillId, boolean mastered) {
        int duration = mastered ? 600 : 200; // 30 seconds or 10 seconds
        int amplifier = mastered ? 1 : 0;
        
        String lowerSkillId = skillId.toLowerCase();
        
        // Apply effects based on skill keywords
        if (lowerSkillId.contains("fire") || lowerSkillId.contains("flame")) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, duration, amplifier, false, true, true));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, duration, amplifier, false, true, true));
        } else if (lowerSkillId.contains("water") || lowerSkillId.contains("ice")) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WATER_BREATHING, duration, amplifier, false, true, true));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE, duration, amplifier, false, true, true));
        } else if (lowerSkillId.contains("barrier") || lowerSkillId.contains("shield")) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, duration, amplifier, false, true, true));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.ABSORPTION, duration, amplifier + 1, false, true, true));
        } else if (lowerSkillId.contains("speed") || lowerSkillId.contains("thought")) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, duration, amplifier + 1, false, true, true));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DIG_SPEED, duration, amplifier + 1, false, true, true));
        } else {
            // Default generic buff
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.LUCK, duration, amplifier, false, true, true));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.REGENERATION, duration / 2, amplifier, false, true, true));
        }
    }

    // ==================== Helper Methods ====================

    private Map<String, Integer> getEssences(CompoundTag tag) {
        Map<String, Integer> essences = new HashMap<>();
        if (tag.contains(TAG_ESSENCES)) {
            CompoundTag essenceTag = tag.getCompound(TAG_ESSENCES);
            for (String key : essenceTag.getAllKeys()) {
                essences.put(key, essenceTag.getInt(key));
            }
        }
        return essences;
    }

    private void addEssences(CompoundTag tag, Map<String, Integer> newEssences) {
        CompoundTag essenceTag = tag.contains(TAG_ESSENCES) ? tag.getCompound(TAG_ESSENCES) : new CompoundTag();
        for (Map.Entry<String, Integer> entry : newEssences.entrySet()) {
            int current = essenceTag.getInt(entry.getKey());
            essenceTag.putInt(entry.getKey(), current + entry.getValue());
        }
        tag.put(TAG_ESSENCES, essenceTag);
    }

    private void consumeEssences(CompoundTag tag, Map<String, Integer> recipe) {
        CompoundTag essenceTag = tag.getCompound(TAG_ESSENCES);
        for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
            int current = essenceTag.getInt(entry.getKey());
            essenceTag.putInt(entry.getKey(), Math.max(0, current - entry.getValue()));
        }
        tag.put(TAG_ESSENCES, essenceTag);
    }

    private boolean canCraftRecipe(Map<String, Integer> currentEssences, Map<String, Integer> recipe) {
        for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
            if (currentEssences.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private List<String> getAnalyzedSkills(CompoundTag tag) {
        List<String> skills = new ArrayList<>();
        if (tag.contains(TAG_ANALYZED_SKILLS)) {
            ListTag list = tag.getList(TAG_ANALYZED_SKILLS, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                skills.add(list.getString(i));
            }
        }
        return skills;
    }

    private void addAnalyzedSkill(CompoundTag tag, String skillId) {
        ListTag list = tag.contains(TAG_ANALYZED_SKILLS) ? 
            tag.getList(TAG_ANALYZED_SKILLS, Tag.TAG_STRING) : new ListTag();
        list.add(StringTag.valueOf(skillId));
        tag.put(TAG_ANALYZED_SKILLS, list);
    }

    private String formatEssenceName(String essenceType) {
        return essenceType.substring(0, 1).toUpperCase() + essenceType.substring(1);
    }

    private void sendMessage(Player player, String translationKey, ChatFormatting color) {
        player.displayClientMessage(
            Component.translatable(translationKey).setStyle(Style.EMPTY.withColor(color)),
            false
        );
    }

    // ==================== Visual/Audio Effects ====================

    private void playAnalyzeEffects(LivingEntity entity) {
        entity.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
        
        if (entity.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                entity.getX(), entity.getY() + 1.0, entity.getZ(),
                30, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private void playSynthesizeEffects(LivingEntity entity) {
        entity.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
        entity.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        
        if (entity.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                entity.getX(), entity.getY() + 1.0, entity.getZ(),
                50, 0.5, 1.0, 0.5, 0.3);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                entity.getX(), entity.getY() + 0.5, entity.getZ(),
                20, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private void playReplicateEffects(LivingEntity entity) {
        entity.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
            SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.PLAYERS, 1.0F, 1.3F);
        
        if (entity.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL,
                entity.getX(), entity.getY() + 1.0, entity.getZ(),
                15, 0.3, 0.5, 0.3, 0.02);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                entity.getX(), entity.getY() + 0.5, entity.getZ(),
                25, 0.4, 0.4, 0.4, 0.1);
        }
    }
}


