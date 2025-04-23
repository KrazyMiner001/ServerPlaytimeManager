package krazyminer001.playtime.tracking;

import krazyminer001.playtime.config.Config;

import java.time.ZoneOffset;
import java.util.List;

public class ClientServerDataCache {
    public static int playtime = 0;

    public static List<Config.TimePeriodString> timePeriodStrings = List.of();
    public static boolean timePeriodStringDirty = true;

    public static ZoneOffset midnightTimezone = ZoneOffset.UTC;
}
