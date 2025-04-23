package krazyminer001.playtime.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.PredicateConstraint;
import krazyminer001.playtime.ServerPlaytimeManager;
import krazyminer001.playtime.time.TimePeriod;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

@SuppressWarnings("unused")
@Config(name = ServerPlaytimeManager.MOD_ID, wrapperName = "PlaytimeConfig")
public class OwoConfigModel {
    @PredicateConstraint("validTimezone")
    public String timezone = "Z";

    @PredicateConstraint("validListOfTimePeriodStrings")
    public List<TimePeriodString> timePeriods = List.of();

    public int maxTime = -1;

    public static boolean validTimezone(String timezone) {
        try {
            ZoneOffset.of(timezone);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }
    public static boolean validListOfTimePeriodStrings(List<TimePeriodString> timePeriods) {
        try {
            timePeriods.forEach(TimePeriod::new);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
