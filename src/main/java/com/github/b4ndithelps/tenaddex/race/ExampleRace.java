package com.github.b4ndithelps.tenaddex.race;

import com.github.b4ndithelps.tenaddex.ability.skill.intrinsic.ExampleIntrinsicSkill;
import com.github.b4ndithelps.tenaddex.registry.skill.AllSkills;
import com.github.manasmods.tensura.ability.TensuraSkill;
import com.github.manasmods.tensura.race.Race;
import com.github.manasmods.tensura.registry.skill.IntrinsicSkills;
import com.github.manasmods.tensura.util.JumpPowerHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a class that shows a short example of creating a custom race. For more information, check out the existing Race files in the mod.
 */
public class ExampleRace extends Race {

    // Here are listed all the main characteristics of the race for easy changing
    private double baseHealth = 12.0;
    private double baseAttackDamage = 1.0;
    private double baseAttackSpeed = 3.0;
    private double knockbackResistance = 0.0;
    private double jumpHeight = 1.0;
    private double movementSpeed = 0.1;
    private double sprintSpeed = 0.15;
    private double auraMin = 800.0;
    private double auraMax = 1211.0;
    private double startingMagiculeMin = 80.0;
    private double startingMagiculeMax = 120.0;

    private float playerSize = 2.0f;


    public ExampleRace() {
        // Here is where the difficulty for each of the
        super(Difficulty.INTERMEDIATE);
    }

    @Override
    public double getBaseHealth() {
        return baseHealth;
    }

    @Override
    public float getPlayerSize() {
        return playerSize;
    }

    @Override
    public double getBaseAttackDamage() {
        return baseAttackDamage;
    }

    @Override
    public double getBaseAttackSpeed() {
        return baseAttackSpeed;
    }

    @Override
    public double getKnockbackResistance() {
        return knockbackResistance;
    }

    @Override
    public double getJumpHeight() {
        // Jump height is a little strange, so use this helper to hide the rest of the calculations
        return JumpPowerHelper.defaultPlayer(jumpHeight);
    }

    @Override
    public double getMovementSpeed() {
        return movementSpeed;
    }

    @Override
    public double getSprintSpeed() {
        return sprintSpeed;
    }

    @Override
    public Pair<Double, Double> getBaseAuraRange() {
        // The range of values that the Aura Range could be. So between 800 and 1211
        return Pair.of(auraMin, auraMax);
    }

    @Override
    public Pair<Double, Double> getBaseMagiculeRange() {
        // The range of values that the Max Magicules could be. So between 80 and 120
        return Pair.of(startingMagiculeMin, startingMagiculeMax);
    }

    /**
     * This method adds the intrinsic skills, from either the addon or the main mod to the class.
     * To add more, simply add to the list with .add()
     * @param player
     * @return
     */
    @Override
    public List<TensuraSkill> getIntrinsicSkills(Player player) {
        List<TensuraSkill> list = new ArrayList<>();
        list.add(AllSkills.EXAMPLE_INTRINSIC.get());
        list.add(IntrinsicSkills.BODY_ARMOR.get());

        return list;
    }

    public boolean isMajin() {
        return false;
    }

    public boolean isSpiritual() {
        return false;
    }

    public boolean isDivine() {
        return false;
    }
}
