package com.github.b4ndithelps.tenforge.network;

import com.github.b4ndithelps.tenforge.data.SkillTransferManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from client to server when player wants to break down a skill.
 * Uses SkillTransferManager for optimized, centralized handling.
 */
public class SkillBreakdownPacket {

    private final String skillId;
    private final boolean fullBreakdown;

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

            ResourceLocation skillId = new ResourceLocation(packet.skillId);
            SkillTransferManager.OperationResult result = 
                    SkillTransferManager.get().breakdownSkill(player, skillId, packet.fullBreakdown);
            
            // Send result message to player
            String prefix = result.success() ? "§6[Skill Forge] §a" : "§6[Skill Forge] §c";
            player.sendSystemMessage(Component.literal(prefix).append(result.toComponent()));
        });

        ctx.get().setPacketHandled(true);
    }
}
