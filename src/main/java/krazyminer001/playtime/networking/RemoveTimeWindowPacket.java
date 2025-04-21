package krazyminer001.playtime.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public record RemoveTimeWindowPacket(int index) implements CustomPayload {
    public static final Id<RemoveTimeWindowPacket> ID = new Id<>(of("remove_time_window"));
    public static final PacketCodec<RegistryByteBuf, RemoveTimeWindowPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, RemoveTimeWindowPacket::index,
            RemoveTimeWindowPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
