package krazyminer001.playtime.config;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record TimePeriodString(String startTime, String endTime) {
    public static final PacketCodec<RegistryByteBuf, TimePeriodString> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, TimePeriodString::startTime,
            PacketCodecs.STRING, TimePeriodString::endTime,
            TimePeriodString::new
    );

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimePeriodString(String otherStartTime, String otherEndTime))) return false;

        return this.startTime.equals(otherStartTime) && this.endTime.equals(otherEndTime);
    }

    @Override
    public int hashCode() {
        int result = startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "TimePeriodString{" +
                "startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }

    public Text display() {
        return Text.translatable("timeperiod.playtime.display", startTime, endTime);
    }
}
