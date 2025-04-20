package krazyminer001.playtime.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import krazyminer001.playtime.ServerPlaytimeManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneOffset;

public class Config {
    public static ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.of(ServerPlaytimeManager.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(ServerPlaytimeManager.MOD_ID + ".json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    public static ZoneOffset getZoneOffset() {
        return ZoneOffset.of(HANDLER.instance().timezone);
    }

    @SerialEntry(comment = "Timezone offset from UTC\nAccepted formats: Z (for UTC), +h, +hh, +hh:mm, -hh:mm, +hhmm, -hhmm, +hh:mm:ss, -hh:mm:ss, +hhmmss, -hhmmss")
    public String timezone = "Z";

    @SerialEntry(comment = "Periods where time is not counted\nFormatted as a time with offset in accordance with ISO 8601")
    public TimePeriodString[] nonTrackingPeriods = new TimePeriodString[]{};

    @SerialEntry(comment = "Maximum time a player can have outside of non tracking timePeriods\nTime is measured in ticks\nUse -1 for infinite")
    public int maxTime = -1;

    public record TimePeriodString(@SerialEntry String startTime, @SerialEntry String endTime) {
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

        public String display() {
            return "startTime: " + startTime + ", endTime: " + endTime;
        }
    }
}
