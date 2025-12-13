package com.github.b4ndithelps.tenaddex.ability.skill.ultimate;

import com.github.b4ndithelps.tenaddex.TensuraAddonExample;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.ability.SkillUtils;
import com.github.manasmods.tensura.ability.skill.Skill;
import com.github.manasmods.tensura.entity.magic.skill.WaterBladeProjectile;
import com.github.manasmods.tensura.registry.skill.CommonSkills;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;


/**
 * This Ultimate Skill is an ultimate version of the waterblade skill.
 * It will scan all the enemies in a radius around the player, and attack them
 * with a blade of water
 */
public class ExampleUltimateSkill extends Skill {

    // Here are some easy to change parameters to configure for the skill
    private final double skillCastCost = 100.0;     // How many magicules it costs to cast
    private final double epUnlockCost = 100000.0;   // EP Level required for unlocking the skill
    private final double learnCost = 4.0;           // When learning the skill, how hard is it. (Higher = harder). Default is 2.0

    /**
     * This is where you define the path to the actual icon image.
     * @return
     */
    public ResourceLocation getSkillIcon() {
        return new ResourceLocation(TensuraAddonExample.MODID, "textures/skill/ultimate/example_ultimate.png");
    }

    public ExampleUltimateSkill() {
        // Pass in the type of the skill here.
        super(SkillType.ULTIMATE);
    }

    /**
     * This function determines the unlock condtions for a skill. In this instance,
     * you need to have mastered the water blade skill, and have 100,000 EP
     *
     * @param entity - Player who has the skill
     * @param curEP - Current EP Value of the skill user
     * @return - True/False depending on if the player meets the conditions to obtain
     */
    public boolean meetEPRequirement(Player entity, double curEP) {
        if (!SkillUtils.isSkillMastered(entity, (ManasSkill) CommonSkills.WATER_BLADE.get())) {
            return false;
        } else {
            return curEP > epUnlockCost;
        }
    }

    public double magiculeCost(LivingEntity entity, ManasSkillInstance instance) { return skillCastCost; }

    public double learningCost() { return learnCost; }

    /**
     * This is the method called when the ability is in the ability bar,
     * and the corresponding ability key is pressed. You can put anything you want in here.
     *
     * As an example skill, this is the ultimate version of "WaterBlade", which shoots
     * multiple water blades at every mob it detects in a 20 block radius.
     *
     * @param instance
     * @param entity
     */
    public void onPressed(ManasSkillInstance instance, LivingEntity entity) {
        // Always good to do a quick check for if the entity casting has enough magicules
        if (SkillHelper.outOfMagicule(entity, instance)) {
            return;
        }

        this.shootBladesAtMobs(instance, entity);
    }

    /**
     * A helper method that scans a radius of 20 blocks for mobs, and then creates water blades that will shoot out
     * in all directions to hit those mobs.
     * @param instance
     * @param entity
     */
    private void shootBladesAtMobs(ManasSkillInstance instance, LivingEntity entity) {
        this.addMasteryPoint(instance, entity);

        // get nearby mobs within a 20-block radius
        AABB searchBox = new AABB(
                entity.getX() - 20, entity.getY() - 20, entity.getZ() - 20,
                entity.getX() + 20, entity.getY() + 20, entity.getZ() + 20
        );

        var nearbyMobs = entity.getLevel().getEntitiesOfClass(Mob.class, searchBox);

        for (Mob mob : nearbyMobs) {
            // Create a new water blade projectile for each mob
            WaterBladeProjectile waterBlade = new WaterBladeProjectile(entity.getLevel(), entity);
            waterBlade.setSpeed(7.0f);
            waterBlade.setDamage(40.0f);
            waterBlade.setMpCost(this.magiculeCost(entity, instance));
            waterBlade.setSkill(instance);

            // Set the water blade's position and shoot it towards the mob
            waterBlade.setPos(entity.getX(), entity.getEyeY(), entity.getZ());
            waterBlade.shoot(
                    mob.getX() - entity.getX(),
                    mob.getEyeY() - entity.getEyeY(),
                    mob.getZ() - entity.getZ(),
                    1.5f, // Speed
                    0.0f // Accuracy
            );

            // add the blade to the world
            entity.getLevel().addFreshEntity(waterBlade);
        }

        // Play the sound for shooting
        entity.getLevel().playSound(
                null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS,
                1.0F, 1.0F
        );

    }





}
