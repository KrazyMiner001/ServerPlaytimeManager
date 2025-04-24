package krazyminer001.playtime.time;

import krazyminer001.playtime.config.TimePeriodString;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimePeriod {
    public final LocalTime startTime;
    public final LocalTime endTime;

    public TimePeriod(TimePeriodString timePeriodString) {
        this.startTime = OffsetTime.parse(timePeriodString.startTime()).withOffsetSameInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now())).toLocalTime();
        this.endTime = OffsetTime.parse(timePeriodString.endTime()).withOffsetSameInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now())).toLocalTime();
    }

    public boolean isWithin(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public Duration until(LocalTime time) {
        if (isWithin(time)) return Duration.ZERO;
        if (time.isBefore(startTime)) return Duration.between(time, startTime);
        return Duration.of(time.until(LocalTime.MAX, ChronoUnit.SECONDS) + LocalTime.MIDNIGHT.until(startTime, ChronoUnit.SECONDS), ChronoUnit.SECONDS);
    }

    public String display() {
        return DateTimeFormatter.ISO_OFFSET_TIME.format(startTime) + " to " + DateTimeFormatter.ISO_OFFSET_TIME.format(endTime);
    }
}
