package krazyminer001.playtime.time;

import krazyminer001.playtime.config.Config;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimePeriod {
    private final LocalTime startTime;
    private final LocalTime endTime;

    public TimePeriod(Config.TimePeriodString timePeriodString) {
        this.startTime = LocalTime.from(DateTimeFormatter.ISO_OFFSET_TIME.parse(timePeriodString.startTime()));
        this.endTime = LocalTime.from(DateTimeFormatter.ISO_OFFSET_TIME.parse(timePeriodString.endTime()));
    }

    public boolean isWithin(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public Duration until(LocalTime time) {
        if (isWithin(time)) return Duration.ZERO;
        if (time.isBefore(startTime)) return Duration.between(time, startTime);
        return Duration.of(time.until(LocalTime.MIDNIGHT, ChronoUnit.SECONDS) + LocalTime.MIDNIGHT.until(time, ChronoUnit.SECONDS), ChronoUnit.SECONDS);
    }

    public String display() {
        return DateTimeFormatter.ISO_OFFSET_TIME.format(startTime) + " to " + DateTimeFormatter.ISO_OFFSET_TIME.format(endTime);
    }
}
