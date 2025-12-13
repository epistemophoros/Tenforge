package com.github.b4ndithelps.tenforge.registry.skill;


import com.github.b4ndithelps.tenforge.Tenforge;
import com.github.b4ndithelps.tenforge.ability.skill.common.ExampleCommonSkill;
import com.github.b4ndithelps.tenforge.ability.skill.extra.ExampleExtraSkill;
import com.github.b4ndithelps.tenforge.ability.skill.intrinsic.ExampleIntrinsicSkill;
import com.github.b4ndithelps.tenforge.ability.skill.ultimate.ExampleUltimateSkill;
import com.github.b4ndithelps.tenforge.ability.skill.unique.ExampleUniqueSkill;
import com.github.b4ndithelps.tenforge.ability.skill.unique.SkillForgeSkill;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * This file is responsible for registering all the skills with the official Tensura Mod.
 * It works by using a single DeferredRegister, which is based off of the ManasCore Skill Registry.
 * Any skill you create must go into that registry.
 *
 * All Skill types can go in here.
 */
public class AllSkills {

    // Here is that deferred register I was talking about. You don't need to change it at all
    public static DeferredRegister<ManasSkill> skillRegistry = DeferredRegister.create(SkillAPI.getSkillRegistryKey(), Tenforge.MODID);

    public static void register(IEventBus modEventBus) {
        skillRegistry.register(modEventBus);
    }

    // Here is where the skills are registered. To add another skill, simply duplicate the RegistryObject, and change it to match your skill defined in ../ability/skill/

    //   =================
    //   | Common Skills |
    //   =================
    public static final RegistryObject<ExampleCommonSkill> EXAMPLE_COMMON =
            skillRegistry.register("example_common", ExampleCommonSkill::new);

    //   ====================
    //   | Intrinsic Skills |
    //   ====================
    public static final RegistryObject<ExampleIntrinsicSkill> EXAMPLE_INTRINSIC =
            skillRegistry.register("example_intrinsic", ExampleIntrinsicSkill::new);

    //   ================
    //   | Extra Skills |
    //   ================
    public static final RegistryObject<ExampleExtraSkill> EXAMPLE_EXTRA =
            skillRegistry.register("example_extra", ExampleExtraSkill::new);

    //   =================
    //   | Unique Skills |
    //   =================
    public static final RegistryObject<ExampleUniqueSkill> EXAMPLE_UNIQUE =
            skillRegistry.register("example_unique", ExampleUniqueSkill::new);
    
    // Skill Forge - Analyze, breakdown, and synthesize skills (inspired by Raphael/Great Sage from Tensura)
    public static final RegistryObject<SkillForgeSkill> SKILL_FORGE =
            skillRegistry.register("skill_forge", SkillForgeSkill::new);

    //   =====================
    //   | Resistance Skills |
    //   =====================


    //   ===================
    //   | Ultimate Skills |
    //   ===================
    public static final RegistryObject<ExampleUltimateSkill> EXAMPLE_ULTIMATE =
            skillRegistry.register("example_ultimate", ExampleUltimateSkill::new);
}
