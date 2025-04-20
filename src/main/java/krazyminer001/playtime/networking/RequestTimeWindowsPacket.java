package krazyminer001.playtime.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public record RequestTimeWindowsPacket() implements CustomPayload {
    public static final CustomPayload.Id<RequestTimeWindowsPacket> ID = new CustomPayload.Id<>(of("time_windows_request"));
    public static final PacketCodec<RegistryByteBuf, RequestTimeWindowsPacket> CODEC = PacketCodec.unit(new RequestTimeWindowsPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
