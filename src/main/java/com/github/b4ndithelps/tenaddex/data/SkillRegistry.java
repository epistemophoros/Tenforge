package com.github.b4ndithelps.tenaddex.data;

import com.github.b4ndithelps.tenaddex.util.SkillEnergyCalculator;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.tensura.ability.skill.Skill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Centralized skill registry and cache for optimized skill lookups.
 * 
 * Provides:
 * - Cached skill lists by tier/type
 * - Player skill tracking with dirty-checking
 * - Efficient filtering and sorting algorithms
 * - Cross-mod compatibility through ManasCore API
 * 
 * This ensures compatibility with ALL skills from Tensura and any addons.
 */
public class SkillRegistry {

    // Singleton instance
    private static final SkillRegistry INSTANCE = new SkillRegistry();
    
    // Cache for all registered skills (built once, refreshed on registry change)
    private List<SkillData> allSkillsCache = null;
    private Map<Skill.SkillType, List<SkillData>> skillsByTier = null;
    private long lastCacheUpdate = 0;
    private static final long CACHE_LIFETIME = 60000; // 1 minute
    
    // Per-player caches (weak references to allow GC)
    private final Map<UUID, PlayerSkillCache> playerCaches = new ConcurrentHashMap<>();

    private SkillRegistry() {}

    public static SkillRegistry get() {
        return INSTANCE;
    }

    /**
     * Data class for cached skill information
     */
    public static class SkillData implements Comparable<SkillData> {
        public final ManasSkill skill;
        public final ResourceLocation id;
        public final String name;
        public final Skill.SkillType tier;
        public final double baseEnergy;
        public final double creationCost;
        public final double epCost;
        public final String modId;

        public SkillData(ManasSkill skill) {
            this.skill = skill;
            this.id = skill.getRegistryName();
            this.name = skill.getName().getString();
            this.modId = id != null ? id.getNamespace() : "unknown";
            
            if (skill instanceof Skill tensuraSkill) {
                this.tier = tensuraSkill.getSkillType();
            } else {
                this.tier = Skill.SkillType.COMMON; // Default for non-Tensura skills
            }
            
            this.baseEnergy = SkillEnergyCalculator.getBaseEnergy(skill);
            this.creationCost = SkillEnergyCalculator.calculateCreationCost(skill);
            this.epCost = SkillEnergyCalculator.calculateEPCost(skill);
        }

        @Override
        public int compareTo(SkillData other) {
            // Sort by tier (higher first), then by name
            int tierCompare = Integer.compare(getTierOrder(other.tier), getTierOrder(this.tier));
            if (tierCompare != 0) return tierCompare;
            return this.name.compareToIgnoreCase(other.name);
        }

        private int getTierOrder(Skill.SkillType tier) {
            return switch (tier) {
                case COMMON -> 1;
                case INTRINSIC -> 2;
                case EXTRA -> 3;
                case RESISTANCE -> 4;
                case UNIQUE -> 5;
                case ULTIMATE -> 6;
            };
        }
    }

    /**
     * Per-player skill cache with dirty tracking
     */
    public static class PlayerSkillCache {
        private List<ManasSkillInstance> skills = new ArrayList<>();
        private Set<ResourceLocation> skillIds = new HashSet<>();
        private long lastUpdate = 0;
        private boolean dirty = true;

        public void markDirty() {
            dirty = true;
        }

        public boolean isDirty() {
            return dirty || (System.currentTimeMillis() - lastUpdate > 5000); // 5 sec refresh
        }

        public void update(List<ManasSkillInstance> newSkills) {
            this.skills = new ArrayList<>(newSkills);
            this.skillIds = skills.stream()
                    .map(s -> s.getSkill().getRegistryName())
                    .collect(Collectors.toSet());
            this.lastUpdate = System.currentTimeMillis();
            this.dirty = false;
        }

        public List<ManasSkillInstance> getSkills() {
            return skills;
        }

        public boolean hasSkill(ResourceLocation skillId) {
            return skillIds.contains(skillId);
        }
    }

    // ==================== Public API ====================

    /**
     * Get all registered skills (cached)
     */
    public List<SkillData> getAllSkills() {
        ensureCacheValid();
        return Collections.unmodifiableList(allSkillsCache);
    }

    /**
     * Get skills filtered by tier
     */
    public List<SkillData> getSkillsByTier(Skill.SkillType tier) {
        ensureCacheValid();
        return Collections.unmodifiableList(skillsByTier.getOrDefault(tier, Collections.emptyList()));
    }

    /**
     * Get skills filtered by custom predicate
     */
    public List<SkillData> getSkillsFiltered(Predicate<SkillData> filter) {
        ensureCacheValid();
        return allSkillsCache.stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Get skills player can create (doesn't have + can afford)
     */
    public List<SkillData> getCreatableSkills(Player player, double currentEnergy, double currentEP) {
        ensureCacheValid();
        PlayerSkillCache playerCache = getPlayerCache(player);
        
        return allSkillsCache.stream()
                .filter(data -> !playerCache.hasSkill(data.id))
                .filter(data -> data.creationCost <= currentEnergy)
                .filter(data -> data.epCost <= currentEP)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get skills player can afford but doesn't have
     */
    public List<SkillData> getAffordableSkills(Player player, double currentEnergy) {
        ensureCacheValid();
        PlayerSkillCache playerCache = getPlayerCache(player);
        
        return allSkillsCache.stream()
                .filter(data -> !playerCache.hasSkill(data.id))
                .filter(data -> data.creationCost <= currentEnergy)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get all skills player doesn't have
     */
    public List<SkillData> getUnownedSkills(Player player) {
        ensureCacheValid();
        PlayerSkillCache playerCache = getPlayerCache(player);
        
        return allSkillsCache.stream()
                .filter(data -> !playerCache.hasSkill(data.id))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get player's skills with cached data
     */
    public List<ManasSkillInstance> getPlayerSkills(Player player) {
        return getPlayerCache(player).getSkills();
    }

    /**
     * Check if player has a specific skill
     */
    public boolean playerHasSkill(Player player, ResourceLocation skillId) {
        return getPlayerCache(player).hasSkill(skillId);
    }

    /**
     * Search skills by name (case-insensitive, partial match)
     */
    public List<SkillData> searchSkills(String query) {
        ensureCacheValid();
        String lowerQuery = query.toLowerCase();
        return allSkillsCache.stream()
                .filter(data -> data.name.toLowerCase().contains(lowerQuery))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get skill data by ID
     */
    public Optional<SkillData> getSkillData(ResourceLocation id) {
        ensureCacheValid();
        return allSkillsCache.stream()
                .filter(data -> data.id.equals(id))
                .findFirst();
    }

    /**
     * Mark player cache as dirty (call after skill changes)
     */
    public void invalidatePlayerCache(Player player) {
        PlayerSkillCache cache = playerCaches.get(player.getUUID());
        if (cache != null) {
            cache.markDirty();
        }
    }

    /**
     * Force refresh all caches
     */
    public void invalidateAll() {
        allSkillsCache = null;
        skillsByTier = null;
        playerCaches.clear();
    }

    // ==================== Internal Methods ====================

    private PlayerSkillCache getPlayerCache(Player player) {
        PlayerSkillCache cache = playerCaches.computeIfAbsent(
                player.getUUID(), 
                uuid -> new PlayerSkillCache()
        );

        if (cache.isDirty()) {
            SkillStorage storage = SkillStorage.get(player);
            if (storage != null) {
                cache.update(storage.getLearnedSkills());
            }
        }

        return cache;
    }

    private void ensureCacheValid() {
        long now = System.currentTimeMillis();
        if (allSkillsCache == null || (now - lastCacheUpdate > CACHE_LIFETIME)) {
            rebuildCache();
        }
    }

    private void rebuildCache() {
        allSkillsCache = new ArrayList<>();
        skillsByTier = new EnumMap<>(Skill.SkillType.class);

        // Initialize tier lists
        for (Skill.SkillType tier : Skill.SkillType.values()) {
            skillsByTier.put(tier, new ArrayList<>());
        }

        // Build from registry - compatible with ALL registered skills
        for (ManasSkill skill : SkillAPI.getSkillRegistry()) {
            if (skill == null || skill.getRegistryName() == null) continue;
            
            SkillData data = new SkillData(skill);
            allSkillsCache.add(data);
            skillsByTier.get(data.tier).add(data);
        }

        // Sort all lists
        Collections.sort(allSkillsCache);
        for (List<SkillData> list : skillsByTier.values()) {
            Collections.sort(list);
        }

        lastCacheUpdate = System.currentTimeMillis();
    }

    // ==================== Statistics ====================

    /**
     * Get count of skills by tier
     */
    public Map<Skill.SkillType, Integer> getSkillCounts() {
        ensureCacheValid();
        Map<Skill.SkillType, Integer> counts = new EnumMap<>(Skill.SkillType.class);
        for (Map.Entry<Skill.SkillType, List<SkillData>> entry : skillsByTier.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    /**
     * Get count of skills by mod
     */
    public Map<String, Integer> getSkillCountsByMod() {
        ensureCacheValid();
        return allSkillsCache.stream()
                .collect(Collectors.groupingBy(
                        data -> data.modId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    /**
     * Get total number of registered skills
     */
    public int getTotalSkillCount() {
        ensureCacheValid();
        return allSkillsCache.size();
    }
}

