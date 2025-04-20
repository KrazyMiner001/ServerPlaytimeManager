package krazyminer001.playtime.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public record RequestUserPlaytimePacket() implements CustomPayload {
    public static final CustomPayload.Id<RequestUserPlaytimePacket> ID = new CustomPayload.Id<>(of("user_playtime_request"));
    public static final PacketCodec<RegistryByteBuf, RequestUserPlaytimePacket> CODEC = PacketCodec.unit(new RequestUserPlaytimePacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
