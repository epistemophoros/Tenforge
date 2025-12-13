package com.github.b4ndithelps.tenaddex.network;

import com.github.b4ndithelps.tenaddex.data.SkillTransferManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from client to server when player wants to forge a new skill.
 * Uses SkillTransferManager for optimized, centralized handling.
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

            ResourceLocation skillId = new ResourceLocation(packet.skillId);
            SkillTransferManager.OperationResult result = 
                    SkillTransferManager.get().forgeSkill(player, skillId);
            
            // Send result message to player
            String prefix = result.success() ? "§6[Skill Forge] §a" : "§6[Skill Forge] §c";
            player.sendSystemMessage(Component.literal(prefix).append(result.toComponent()));
        });

        ctx.get().setPacketHandled(true);
    }
}
