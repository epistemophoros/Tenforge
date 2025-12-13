package com.github.b4ndithelps.tenaddex.ability.skill.unique;

import com.github.b4ndithelps.tenaddex.TensuraAddonExample;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.ability.SkillUtils;
import com.github.manasmods.tensura.ability.TensuraSkillInstance;
import com.github.manasmods.tensura.ability.skill.Skill;
import com.github.manasmods.tensura.entity.magic.skill.WaterBladeProjectile;
import com.github.manasmods.tensura.registry.skill.CommonSkills;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;


/**
 * Unique Skill: Blow up
 *
 * This unique skill shows you how to use different modes
 *
 * (Make sure your life insurance is up to date...)
 */
public class ExampleUniqueSkill extends Skill {

    // Here are some easy to change parameters to configure for the skill
    private final double learnCost = 4.0;              // When learning the skill, how hard is it. (Higher = harder). Default is 2.0
    private final int numModes = 2;                    // The number of skill modes there are.

    /**
     * This is where you define the path to the actual icon image.
     * @return
     */
    public ResourceLocation getSkillIcon() {
        return new ResourceLocation(TensuraAddonExample.MODID, "textures/skill/unique/example_unique.png");
    }

    public ExampleUniqueSkill() {
        // Pass in the type of the skill here.
        super(SkillType.UNIQUE);
    }

    public int modes() { return numModes; }

    /**
     * This is a generic method that toggles the mode.
     * To update it, you need to simply change the "modes" variable above.
     * @param entity
     * @param instance
     * @param reverse
     * @return
     */
    public int nextMode(LivingEntity entity, TensuraSkillInstance instance, boolean reverse) {
        if (reverse) {
            return instance.getMode() == 1 ? numModes : instance.getMode() - 1;
        } else {
            return instance.getMode() == numModes ? 1 : instance.getMode() + 1;
        }
    }

    /**
     * This method handles the names of the different modes. Pulls directly from the translation files.
     * If you want more modes, simply add them to the switch case.
     * @param curMode
     * @return
     */
    public Component getModeName(int curMode) {
        MutableComponent name;
        switch (curMode) {
            case 1 -> name = Component.translatable("tenaddesx.skill.mode.example_unique.boom");
            case 2 -> name = Component.translatable("tenaddesx.skill.mode.example_unique.big_boom");
            default -> name = Component.empty();
        }

        return name;
    }

    /**
     * Because we have different modes, we can make them each cost a different amount of magicules.
     * @param entity
     * @param instance
     * @return
     */
    public double magiculeCost(LivingEntity entity, ManasSkillInstance instance) {
        double cost;
        switch (instance.getMode()) {
            case 1 -> cost = 500.0;
            case 2 -> cost = 1000.0;
            default -> cost = 0.0;
        }

        return cost;
    }

    public double learningCost() { return learnCost; }

    /**
     * This is the method called when the ability is in the ability bar,
     * and the corresponding ability key is pressed. You can put anything you want in here.
     *
     * For this skill, if the mode is 1, so a small explosion with no cooldown.
     * If the mode is 2, do a large explosion with a cooldown
     *
     * @param instance
     * @param entity
     */
    public void onPressed(ManasSkillInstance instance, LivingEntity entity) {
        // Always good to do a quick check for if the entity casting has enough magicules
        if (SkillHelper.outOfMagicule(entity, instance)) {
            return;
        }

        switch (instance.getMode()) {
            case 1: // Small explosion
                createExplosion(entity, 2.0F); // Explosion radius: 2.0F
                this.addMasteryPoint(instance, entity);
                break;
            case 2: // Bigger explosion
                createExplosion(entity, 10.0F); // Explosion radius: 5.0F

                // Add a cooldown to the big explosion to not spam
                if (instance.isMastered(entity)) {
                    instance.setCoolDown(3);
                } else {
                    instance.setCoolDown(10);
                }

                this.addMasteryPoint(instance, entity);
                break;
            default:
                break;
        }
    }

    /**
     * Helper method that just makes an explosion at the location of the passed in entity.
     * @param entity
     * @param explosionRadius
     */
    private void createExplosion(LivingEntity entity, float explosionRadius) {
        // Create an explosion at the entity's position
        entity.getLevel().explode(
                entity,                // The entity causing the explosion
                entity.getX(),         // X position
                entity.getY(),         // Y position
                entity.getZ(),         // Z position
                explosionRadius,       // Explosion radius
                Explosion.BlockInteraction.DESTROY // Explosion behavior (e.g., destroy blocks)
        );
    }
}
