package com.github.b4ndithelps.tenaddex.mixin;

import com.github.b4ndithelps.tenaddex.ability.skill.intrinsic.ExampleIntrinsicSkill;
import com.github.b4ndithelps.tenaddex.registry.skill.AllSkills;
import com.github.manasmods.tensura.ability.TensuraSkill;
import com.github.manasmods.tensura.race.Race;
import com.github.manasmods.tensura.race.human.HumanRace;
import com.github.manasmods.tensura.race.slime.SlimeRace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(HumanRace.class)
public abstract class HumanRaceMixin {

//    @Inject(method = "getIntrinsicSkills()Ljava/util/List;", at = @At("RETURN"), cancellable = true)
//    private void onGetIntrinsicSkills(CallbackInfoReturnable<List<TensuraSkill>> cir) {
//        List<TensuraSkill> skills = cir.getReturnValue();
//
//        // If the original method returned null, initialize the list
//        if (skills == null) {
//            skills = new ArrayList<>();
//        }
//
//        // Add your custom skills here
//        // For example:
//        skills.add(AllSkills.EXAMPLE_INTRINSIC.get());
//
//        // Set the modified list as the return value
//        cir.setReturnValue(skills);
//    }

//    @Inject(method = "getIntrinsicSkills", at = @AT("RETURN"), cancellable=true)
//    private void addIntrinsicSkill(CallbackInfoReturnable<List<TensuraSkill>> cir) {
//        List<TensuraSkill> skills = cir.getReturnValue();
//        skills.add(AllSkills.EXAMPLE_EXTRA.get());
//    }

//        private final List<TensuraSkill> intrinsicSkills = new ArrayList<>();
//
//        // Add a method to manage intrinsic skills
//        public List<TensuraSkill> getCustomIntrinsicSkills() {
//            if (intrinsicSkills.isEmpty()) {
//                intrinsicSkills.add(AllSkills.EXAMPLE_INTRINSIC.get());
//            }
//            return intrinsicSkills;
//        }

//    @Inject(method = "getIntrinsicSkills", at = @At("RETURN"), cancellable = true, remap = false)
//    private void addCustomSkill(CallbackInfoReturnable<List<TensuraSkill>> cir) {
//        List<TensuraSkill> skills = cir.getReturnValue();
//        skills.add(AllSkills.EXAMPLE_INTRINSIC.get());
//        cir.setReturnValue(skills);
//    }

//    @Inject(method = "getIntrinsicSkills", at = @At("RETURN"), cancellable = true, remap = false)
//    private void overrideIntrinsicSkills(CallbackInfoReturnable<List<TensuraSkill>> cir) {
//        List<TensuraSkill> skills = cir.getReturnValue();
//        skills.add(MyCustomSkills.MY_CUSTOM_HUMAN_SKILL);
//        cir.setReturnValue(skills);
//    }

//        @Inject(method = "getIntrinsicSkills", at = @At("RETURN"), cancellable = true, remap = false)
//        private void overrideIntrinsicSkills(CallbackInfoReturnable<List<TensuraSkill>> cir) {
//            List<TensuraSkill> skills = cir.getReturnValue();
//            skills.add(AllSkills.EXAMPLE_INTRINSIC.get());
//            cir.setReturnValue(skills);
//        }

//    @Inject(method = "getIntrinsicSkills", at = @At("RETURN"), cancellable = true, remap = false)
//    private void modifyIntrinsicSkillsForAllRaces(CallbackInfoReturnable<List<TensuraSkill>> cir) {
//        List<TensuraSkill> skills = cir.getReturnValue();
//
//        // Add a skill conditionally based on the class of the instance
//        if (this instanceof HumanRace) {
//            skills.add(AllSkills.EXAMPLE_INTRINSIC);
//        }
//
//        cir.setReturnValue(skills);
//    }

}
