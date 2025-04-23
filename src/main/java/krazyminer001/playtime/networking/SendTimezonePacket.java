package krazyminer001.playtime.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.time.ZoneOffset;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public record SendTimezonePacket(ZoneOffset zoneOffset) implements CustomPayload {
    public static final Id<SendTimezonePacket> ID = new Id<>(of("send_timezone"));
    public static final PacketCodec<RegistryByteBuf, SendTimezonePacket> CODEC = PacketCodec.tuple(
            PacketCodec.tuple(
                    PacketCodecs.STRING, ZoneOffset::getId,
                    ZoneOffset::of
            ), SendTimezonePacket::zoneOffset,
            SendTimezonePacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
