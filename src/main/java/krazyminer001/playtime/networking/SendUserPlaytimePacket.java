package krazyminer001.playtime.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public record SendUserPlaytimePacket(int playtime) implements CustomPayload {
    public static final Id<SendUserPlaytimePacket> ID = new CustomPayload.Id<>(of("send_user_playtime"));
    public static final PacketCodec<RegistryByteBuf, SendUserPlaytimePacket> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, SendUserPlaytimePacket::playtime, SendUserPlaytimePacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
