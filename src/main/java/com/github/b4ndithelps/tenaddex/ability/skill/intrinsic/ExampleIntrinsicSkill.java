package com.github.b4ndithelps.tenaddex.ability.skill.intrinsic;

import com.github.b4ndithelps.tenaddex.TensuraAddonExample;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.tensura.ability.SkillHelper;
import com.github.manasmods.tensura.ability.skill.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;


/**
 * This Intrinsic Skill is given to humans to make you faster.
 * That is all it does.
 */
public class ExampleIntrinsicSkill extends Skill {
    // A random UUID to help reference the Attribute Modifier in order to toggle it on and off
    protected static final UUID EXAMPLE_SPEED = UUID.fromString("711ec836-4b27-48d8-8def-ace742dbb83c");

    public ExampleIntrinsicSkill() {
        // Pass in the type of the skill here.
        super(SkillType.INTRINSIC);
    }

    /**
     * This is where you define the path to the actual icon image.
     * @return
     */
    public ResourceLocation getSkillIcon() {
        return new ResourceLocation(TensuraAddonExample.MODID, "textures/skill/intrinsic/example_intrinsic.png");
    }

    /**
     * This method will allow the skill to be toggled at any time, since it is intrinsic.
     * @param instance
     * @param entity
     * @return
     */
    public boolean canBeToggled(ManasSkillInstance instance, LivingEntity entity) {
        return true;
    }

    /**
     * The tick method is controlled by this, since it is intrinsic, we can always activate it.
     *
     * @param instance
     * @param entity
     * @return
     */
    public boolean canTick(ManasSkillInstance instance, LivingEntity entity) {
        return true;
    }

    /**
     * This onTick method is for when the skill is toggled on. All we need to do is apply the mastery.
     * This is because the toggle takes care of the speed modifier
     * @param instance
     * @param entity
     */
    public void onTick(ManasSkillInstance instance, LivingEntity entity) {
        CompoundTag tag = instance.getOrCreateTag();
        int time = tag.getInt("activatedTimes");
        if (time % 6 == 0) {
            this.addMasteryPoint(instance, entity);
        }

        tag.putInt("activatedTimes", time + 1);
    }

    /**
     * When we toggle on, we use an Attribute Modifier to permanently increase the movement speed of the entity.
     * @param instance
     * @param entity
     */
    public void onToggleOn(ManasSkillInstance instance, LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeModifier speedModifier = new AttributeModifier(EXAMPLE_SPEED, "Example Speed", 5.0, AttributeModifier.Operation.ADDITION);
        if (speed != null && !speed.hasModifier(speedModifier)) {
            speed.addPermanentModifier(speedModifier);
        }

        entity.getLevel().playSound((Player)null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ARMOR_EQUIP_CHAIN, SoundSource.PLAYERS, 5.0F, 1.0F);
    }

    /**
     * When we toggle it off, find the attribute modifier using the UUID, and then remove it
     * @param instance
     * @param entity
     */
    public void onToggleOff(ManasSkillInstance instance, LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removePermanentModifier(EXAMPLE_SPEED);
        }

    }







}
