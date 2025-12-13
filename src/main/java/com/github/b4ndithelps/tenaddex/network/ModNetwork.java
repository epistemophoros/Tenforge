package com.github.b4ndithelps.tenaddex.network;

import com.github.b4ndithelps.tenaddex.TensuraAddonExample;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Network handler for client-server communication.
 * Handles skill forge GUI packets and energy sync.
 */
public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TensuraAddonExample.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    
    private static int nextId() {
        return packetId++;
    }

    /**
     * Register all network packets
     */
    public static void register() {
        // Client -> Server: Request to break down a skill
        CHANNEL.messageBuilder(SkillBreakdownPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(SkillBreakdownPacket::encode)
                .decoder(SkillBreakdownPacket::decode)
                .consumerMainThread(SkillBreakdownPacket::handle)
                .add();

        // Client -> Server: Request to create a skill
        CHANNEL.messageBuilder(SkillCreatePacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(SkillCreatePacket::encode)
                .decoder(SkillCreatePacket::decode)
                .consumerMainThread(SkillCreatePacket::handle)
                .add();

        // Server -> Client: Sync skill energy data
        CHANNEL.messageBuilder(SyncEnergyPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncEnergyPacket::encode)
                .decoder(SyncEnergyPacket::decode)
                .consumerMainThread(SyncEnergyPacket::handle)
                .add();

        // Client -> Server: Open skill forge GUI
        CHANNEL.messageBuilder(OpenSkillForgePacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(OpenSkillForgePacket::encode)
                .decoder(OpenSkillForgePacket::decode)
                .consumerMainThread(OpenSkillForgePacket::handle)
                .add();
    }

    /**
     * Send packet to specific player
     */
    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /**
     * Send packet to server
     */
    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    /**
     * Send packet to all players
     */
    public static void sendToAll(Object packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}

