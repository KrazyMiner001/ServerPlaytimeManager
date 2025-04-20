package krazyminer001.playtime.networking;

import krazyminer001.playtime.config.Config;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public record ChangeTimeWindowPacket(int index, Config.TimePeriodString timePeriodString) implements CustomPayload {
    public static final CustomPayload.Id<ChangeTimeWindowPacket> ID = new Id<>(of("change_time_period"));
    public static final PacketCodec<RegistryByteBuf, ChangeTimeWindowPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ChangeTimeWindowPacket::index,
            Config.TimePeriodString.PACKET_CODEC, ChangeTimeWindowPacket::timePeriodString,
            ChangeTimeWindowPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
