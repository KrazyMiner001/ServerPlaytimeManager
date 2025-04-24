package krazyminer001.playtime.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import krazyminer001.playtime.networking.RequestTimeWindowsPacket;
import krazyminer001.playtime.networking.RequestUserPlaytimePacket;
import krazyminer001.playtime.screen.component.UpdatableLabelComponent;
import krazyminer001.playtime.time.TimePeriod;
import krazyminer001.playtime.tracking.ClientServerDataCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public class PlaytimeScreen extends BaseUIModelScreen<FlowLayout> {
    public PlaytimeScreen() {
        super(FlowLayout.class, DataSource.asset(of("playtime_screen")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        ClientPlayNetworking.send(new RequestUserPlaytimePacket());
        ClientPlayNetworking.send(new RequestTimeWindowsPacket());

        rootComponent.childById(UpdatableLabelComponent.class, "playtime-counter")
                .text(() -> {
                    Duration playtime = Duration.of(ClientServerDataCache.playtime * 50L, ChronoUnit.MILLIS);
                    return Text.translatable("playtime.playtime.playtime", playtime.toHoursPart(), playtime.toMinutesPart(), playtime.toSecondsPart());
                });

        rootComponent.childById(UpdatableLabelComponent.class, "next-time-window")
                .text(() -> ClientServerDataCache.timePeriodStrings.stream()
                        .map(TimePeriod::new)
                        .min(Comparator.comparingLong(timePeriod -> timePeriod.until(LocalTime.now()).toSeconds()))
                        .map(timePeriod -> {
                            String nextPeriod = timePeriod.startTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
                            Duration untilNextPeriod = timePeriod.until(LocalTime.now());

                            return Text.translatable("playtime.playtime.next_time_window", nextPeriod, untilNextPeriod.toHours(), untilNextPeriod.toMinutesPart(), untilNextPeriod.toSecondsPart());
                        })
                        .orElse(Text.translatable("playtime.playtime.no_time_windows")));

        rootComponent.childById(UpdatableLabelComponent.class, "midnight")
                .text(() -> Text.translatable(
                        "playtime.playtime.midnight_local_time",
                        LocalTime.MIDNIGHT
                                .atOffset(ClientServerDataCache.midnightTimezone)
                                .withOffsetSameInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now()))
                                .toLocalTime()
                                .toString()
                ));
    }
}
