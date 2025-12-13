package com.github.b4ndithelps.tenaddex.network;

import com.github.b4ndithelps.tenaddex.capability.SkillEnergyCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to sync skill energy data.
 */
public class SyncEnergyPacket {

    private final double energy;
    private final double maxEnergy;
    private final int skillsAnalyzed;

    public SyncEnergyPacket(double energy, double maxEnergy, int skillsAnalyzed) {
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.skillsAnalyzed = skillsAnalyzed;
    }

    public static void encode(SyncEnergyPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.energy);
        buf.writeDouble(packet.maxEnergy);
        buf.writeInt(packet.skillsAnalyzed);
    }

    public static SyncEnergyPacket decode(FriendlyByteBuf buf) {
        return new SyncEnergyPacket(buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void handle(SyncEnergyPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side handling
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                SkillEnergyCapability.get(player).ifPresent(cap -> {
                    cap.setEnergy(packet.energy);
                    cap.setMaxEnergy(packet.maxEnergy);
                });
            }
        });

        ctx.get().setPacketHandled(true);
    }
}

