package com.github.b4ndithelps.tenaddex.ability.skill.common;

import com.github.b4ndithelps.tenaddex.TensuraAddonExample;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.ability.skill.Skill;
import com.github.manasmods.tensura.registry.effects.TensuraMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.extensions.IForgeMobEffect;


/**
 * This Common Skill is a simple one to show toggleable skills.
 * When toggled, it will activate an absorption effect for the player, giving them
 * a little more health.
 *
 *
 */
public class ExampleCommonSkill extends Skill {

    /**
     * This is where you define the path to the actual icon image.
     * @return
     */
    public ResourceLocation getSkillIcon() {
        return new ResourceLocation(TensuraAddonExample.MODID, "textures/skill/common/example_common.png");
    }

    // Here are some easy to change parameters to configure for the skill
    private final double skillCastCost = 100.0;     // How many magicules it costs to cast
    private final double epUnlockCost = 10000.0;   // EP Level required for unlocking the skill
    private final double learnCost = 100.0;           // When learning the skill, how hard is it. (Higher = harder). Default is 2.0

    public ExampleCommonSkill() {
        super(SkillType.COMMON);
    }

    /**
     * This function determines whether or not the player has the skill unlocked.
     * In this case, I will make the requirements be that you have to kill 10 vanilla slimes, and have at least 10,000 ep
     *
     * @param entity
     * @param curEP
     * @return
     */
    public boolean meetEPRequirement(Player entity, double curEP) {
        if (entity instanceof ServerPlayer player) {
            int slimesKilled = player.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.SLIME));

            return slimesKilled >= 10 && curEP >= epUnlockCost;
        } else {
            return false;
        }
    }

    /**
     * A function that determines the cost of using the skill. In this case, it will cost 100.0 a tick
     * @param entity
     * @param instance
     * @return
     */
    public double magiculeCost(LivingEntity entity, ManasSkillInstance instance) {
        return skillCastCost;
    }

    public double learningCost() {
        return learnCost;
    }

    public boolean canBeToggled(ManasSkillInstance instance, LivingEntity living) {
        return true;
    }

    public boolean canBeSlotted(ManasSkillInstance instance) {
        return instance.getMastery() < 0;
    }

    /**
     * Determines whether the tick method will fire. In this case, only when it is toggled
     * @param instance
     * @param entity
     * @return
     */
    public boolean canTick(ManasSkillInstance instance, LivingEntity entity) {
        return instance.isToggled();
    }

    /**
     * When toggled on, apply the Absorption effect to the player. If the skill is mastered, increase the level of it.
     * This only applies the first tick, then the onTick() method takes over
     * @param instance
     * @param entity
     */
    public void onToggleOn(ManasSkillInstance instance, LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, instance.isMastered(entity) ? 3 : 1, false, false, false));
    }

    /**
     * Simply remove the absorbtion effect when toggled off.
     * @param instance
     * @param entity
     */
    public void onToggleOff(ManasSkillInstance instance, LivingEntity entity) {
        entity.removeEffect(MobEffects.ABSORPTION);
    }


    public void onTick(ManasSkillInstance instance, LivingEntity entity) {
        // If they are out of magicules, we need to toggle off and remove the effect.
        if (SkillHelper.outOfMagicule(entity, instance)) {
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("tensura.skill.lack_magicule.toggled_off", new Object[]{instance.getSkill().getName()}).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)), false);
            }

            instance.setToggled(false);
            instance.onToggleOff(entity);
            instance.markDirty();
            return;
        }

        // Otherwise, add the ABSORBTION effect, and make the mastery go up after 10 (seconds)? have passed
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, instance.isMastered(entity) ? 3 : 1, false, false, false));

        CompoundTag tag = instance.getOrCreateTag();
        int time = tag.getInt("activatedTimes");
        if (time % 10 == 0) {
            this.addMasteryPoint(instance, entity);
        }

        tag.putInt("activatedTimes", time + 1);
    }


}
