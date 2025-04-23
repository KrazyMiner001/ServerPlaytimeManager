package krazyminer001.playtime.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public record RequestTimezonePacket() implements CustomPayload {
    public static final Id<RequestTimezonePacket> ID = new Id<>(of("request_timezone"));
    public static final PacketCodec<RegistryByteBuf, RequestTimezonePacket> CODEC = PacketCodec.unit(new RequestTimezonePacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
