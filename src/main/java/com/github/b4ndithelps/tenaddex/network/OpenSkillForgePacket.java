package com.github.b4ndithelps.tenaddex.network;

import com.github.b4ndithelps.tenaddex.gui.SkillForgeMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * Packet sent from client to server to open the Skill Forge GUI.
 */
public class OpenSkillForgePacket {

    public OpenSkillForgePacket() {}

    public static void encode(OpenSkillForgePacket packet, FriendlyByteBuf buf) {
        // No data needed
    }

    public static OpenSkillForgePacket decode(FriendlyByteBuf buf) {
        return new OpenSkillForgePacket();
    }

    public static void handle(OpenSkillForgePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                NetworkHooks.openScreen(player, new SkillForgeMenu.Provider(), buf -> {});
            }
        });

        ctx.get().setPacketHandled(true);
    }
}

