package com.github.b4ndithelps.tenaddex.network;

import com.github.b4ndithelps.tenaddex.capability.SkillEnergyCapability;
import com.github.b4ndithelps.tenaddex.util.SkillEnergyCalculator;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.SkillAPI;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import com.github.manasmods.tensura.capability.ep.EPStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from client to server when player wants to create/forge a new skill.
 * Consumes Skill Energy and EP to grant a new skill.
 */
public class SkillCreatePacket {

    private final String skillId;

    public SkillCreatePacket(String skillId) {
        this.skillId = skillId;
    }

    public static void encode(SkillCreatePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.skillId);
    }

    public static SkillCreatePacket decode(FriendlyByteBuf buf) {
        return new SkillCreatePacket(buf.readUtf());
    }

    public static void handle(SkillCreatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Get the skill from registry
            ResourceLocation skillLocation = new ResourceLocation(packet.skillId);
            ManasSkill skill = SkillAPI.getSkillRegistry().getValue(skillLocation);
            
            if (skill == null) {
                player.sendSystemMessage(Component.literal("§cUnknown skill: " + packet.skillId));
                return;
            }

            SkillStorage skillStorage = SkillStorage.get(player);
            if (skillStorage == null) return;

            // Check if player already has this skill
            for (var existingSkill : skillStorage.getLearnedSkills()) {
                if (existingSkill.getSkill().getRegistryName().equals(skillLocation)) {
                    player.sendSystemMessage(Component.literal("§cYou already possess this skill!"));
                    return;
                }
            }

            // Calculate costs
            double energyCost = SkillEnergyCalculator.calculateCreationCost(skill);
            double epCost = SkillEnergyCalculator.calculateEPCost(skill);

            // Check skill energy
            SkillEnergyCapability.get(player).ifPresent(cap -> {
                if (cap.getEnergy() < energyCost) {
                    player.sendSystemMessage(Component.literal(
                            "§cInsufficient Skill Energy! Need §b" + String.format("%.0f", energyCost) + 
                            "§c, have §b" + String.format("%.0f", cap.getEnergy())
                    ));
                    return;
                }

                // Check EP
                EPStorage epStorage = EPStorage.get(player);
                if (epStorage == null || epStorage.getEp() < epCost) {
                    player.sendSystemMessage(Component.literal(
                            "§cInsufficient EP! Need §e" + String.format("%.0f", epCost)
                    ));
                    return;
                }

                // Consume resources
                cap.consumeEnergy(energyCost);
                epStorage.removeEp(epCost);

                // Grant the skill
                skillStorage.learnSkill(skill);

                player.sendSystemMessage(Component.literal(
                        "§6[Skill Forge] §aSuccessfully forged §e" + skill.getName().getString() + "§a!"
                ));

                // Sync energy to client
                ModNetwork.sendToPlayer(new SyncEnergyPacket(cap.getEnergy(), cap.getMaxEnergy(), cap.getSkillsAnalyzed()), player);
            });
        });

        ctx.get().setPacketHandled(true);
    }
}

