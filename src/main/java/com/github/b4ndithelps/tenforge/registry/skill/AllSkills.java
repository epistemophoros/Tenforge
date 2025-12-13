package com.github.b4ndithelps.tenforge.registry.skill;

import com.github.b4ndithelps.tenforge.Tenforge;
import com.github.b4ndithelps.tenforge.ability.skill.unique.SkillForgeSkill;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Skill registry for Tenforge.
 * Register all custom skills here.
 */
public class AllSkills {

    public static final DeferredRegister<ManasSkill> SKILLS = 
            DeferredRegister.create(SkillAPI.getSkillRegistryKey(), Tenforge.MODID);

    public static void register(IEventBus modEventBus) {
        SKILLS.register(modEventBus);
    }

    // ==================== Unique Skills ====================
    
    /**
     * Skill Forge - Break down and create skills using energy
     * Inspired by Raphael/Great Sage from Tensura
     */
    public static final RegistryObject<SkillForgeSkill> SKILL_FORGE =
            SKILLS.register("skill_forge", SkillForgeSkill::new);
}
