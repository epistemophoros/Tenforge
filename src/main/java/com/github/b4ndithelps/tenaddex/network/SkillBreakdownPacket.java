package com.github.b4ndithelps.tenaddex.network;

import com.github.b4ndithelps.tenaddex.capability.SkillEnergyCapability;
import com.github.b4ndithelps.tenaddex.util.SkillEnergyCalculator;
import com.github.manasmods.manascore.api.skills.ManasSkill;
import com.github.manasmods.manascore.api.skills.ManasSkillInstance;
import com.github.manasmods.manascore.api.skills.capability.SkillStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Packet sent from client to server when player wants to break down a skill.
 * The skill is destroyed and converted to Skill Energy.
 */
public class SkillBreakdownPacket {

    private final String skillId;
    private final boolean fullBreakdown; // true = destroy skill, false = partial extract

    public SkillBreakdownPacket(String skillId, boolean fullBreakdown) {
        this.skillId = skillId;
        this.fullBreakdown = fullBreakdown;
    }

    public static void encode(SkillBreakdownPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.skillId);
        buf.writeBoolean(packet.fullBreakdown);
    }

    public static SkillBreakdownPacket decode(FriendlyByteBuf buf) {
        return new SkillBreakdownPacket(buf.readUtf(), buf.readBoolean());
    }

    public static void handle(SkillBreakdownPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            SkillStorage skillStorage = SkillStorage.get(player);
            if (skillStorage == null) return;

            // Find the skill to break down
            ManasSkillInstance targetSkill = null;
            for (ManasSkillInstance skill : skillStorage.getLearnedSkills()) {
                if (skill.getSkill().getRegistryName().toString().equals(packet.skillId)) {
                    targetSkill = skill;
                    break;
                }
            }

            if (targetSkill == null) {
                player.sendSystemMessage(Component.literal("§cSkill not found!"));
                return;
            }

            // Calculate energy yield
            double energyYield = SkillEnergyCalculator.calculateBreakdownEnergy(targetSkill, packet.fullBreakdown);

            // Add energy to player
            SkillEnergyCapability.get(player).ifPresent(cap -> {
                double added = cap.addEnergy(energyYield);
                cap.incrementSkillsAnalyzed();

                // If full breakdown, remove the skill
                if (packet.fullBreakdown) {
                    skillStorage.forgetSkill(targetSkill.getSkill());
                    player.sendSystemMessage(Component.literal(
                            "§6[Skill Forge] §aBroke down §e" + targetSkill.getSkill().getName().getString() + 
                            "§a! Gained §b" + String.format("%.0f", added) + " §aSkill Energy."
                    ));
                } else {
                    // Partial extract reduces mastery
                    int currentMastery = targetSkill.getMastery();
                    targetSkill.setMastery(Math.max(0, currentMastery - 25));
                    player.sendSystemMessage(Component.literal(
                            "§6[Skill Forge] §aExtracted energy from §e" + targetSkill.getSkill().getName().getString() + 
                            "§a! Gained §b" + String.format("%.0f", added) + " §aSkill Energy. Mastery reduced."
                    ));
                }

                // Sync energy to client
                ModNetwork.sendToPlayer(new SyncEnergyPacket(cap.getEnergy(), cap.getMaxEnergy(), cap.getSkillsAnalyzed()), player);
            });
        });

        ctx.get().setPacketHandled(true);
    }
}

