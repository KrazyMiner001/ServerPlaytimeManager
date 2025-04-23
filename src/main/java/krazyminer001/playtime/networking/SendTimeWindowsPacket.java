package krazyminer001.playtime.networking;

import krazyminer001.playtime.config.TimePeriodString;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public record SendTimeWindowsPacket(List<TimePeriodString> timePeriods) implements CustomPayload {
    public static final CustomPayload.Id<SendTimeWindowsPacket> ID = new CustomPayload.Id<>(of("send_time_windows"));
    public static final PacketCodec<RegistryByteBuf, SendTimeWindowsPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(
                    ArrayList::new,
                    TimePeriodString.PACKET_CODEC
            ),
            SendTimeWindowsPacket::timePeriods,
            SendTimeWindowsPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
