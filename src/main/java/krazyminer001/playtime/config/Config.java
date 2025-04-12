package krazyminer001.playtime.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import krazyminer001.playtime.ServerPlaytimeManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

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

    @SerialEntry(comment = "Maximum time a player can have outside of non tracking windows\nTime is measured in ticks\nUse -1 for infinite")
    public int maxTime = -1;

    public static class TimePeriodString {
        @SerialEntry
        public String startTime;

        @SerialEntry
        public String endTime;
    }
}
