package krazyminer001.playtime.networking;

import krazyminer001.playtime.config.Config;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public record AddTimeWindowPacket(Config.TimePeriodString timePeriodString) implements CustomPayload {
    public static final Id<AddTimeWindowPacket> ID = new Id<>(of("add_time_window"));
    public static final PacketCodec<RegistryByteBuf, AddTimeWindowPacket> CODEC = PacketCodec.tuple(
            Config.TimePeriodString.PACKET_CODEC, AddTimeWindowPacket::timePeriodString,
            AddTimeWindowPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
