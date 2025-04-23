package krazyminer001.playtime.tracking;

import krazyminer001.playtime.config.TimePeriodString;

import java.time.ZoneOffset;
import java.util.List;

public class ClientServerDataCache {
    public static int playtime = 0;

    public static List<TimePeriodString> timePeriodStrings = List.of();
    public static boolean timePeriodStringDirty = true;

    public static ZoneOffset midnightTimezone = ZoneOffset.UTC;
}
