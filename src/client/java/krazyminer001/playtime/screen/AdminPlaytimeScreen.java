package krazyminer001.playtime.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import krazyminer001.playtime.config.Config;
import krazyminer001.playtime.networking.AddTimeWindowPacket;
import krazyminer001.playtime.networking.ChangeTimeWindowPacket;
import krazyminer001.playtime.networking.RemoveTimeWindowPacket;
import krazyminer001.playtime.networking.RequestTimeWindowsPacket;
import krazyminer001.playtime.tracking.ClientServerDataCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class AdminPlaytimeScreen extends BaseOwoScreen<FlowLayout> {


    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        ClientPlayNetworking.send(new RequestTimeWindowsPacket());
        if (ClientServerDataCache.timePeriodStringDirty) {
            reopenScreen();
        }

        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(5));

        ArrayList<Config.TimePeriodString> timePeriodStrings = new ArrayList<>(ClientServerDataCache.timePeriodStrings);

        //region Time Windows
        FlowLayout timeWindows = (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .padding(Insets.of(5));

        timePeriodStrings.stream()
                .map(timePeriodString ->
                        new IndexedLocalTimePair(
                                OffsetTime.parse(timePeriodString.startTime()).withOffsetSameInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now())).toLocalTime(),
                                OffsetTime.parse(timePeriodString.endTime()).withOffsetSameInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now())).toLocalTime(),
                                timePeriodStrings.indexOf(timePeriodString)
                        )
                )
                .forEach(indexedLocalTimePair -> timeWindows.child(
                        Containers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(
                                        Containers.grid(Sizing.content(), Sizing.content(), 2, 2)
                                                .child(
                                                        Components.label(Text.translatable("playtime.playtime.timewindows.start_time"))
                                                                .margins(Insets.of(5)),
                                                        0,
                                                        0
                                                )
                                                .child(
                                                        Components.textBox(
                                                                Sizing.fixed(100),
                                                                indexedLocalTimePair.time1.format(DateTimeFormatter.ISO_LOCAL_TIME)
                                                        )
                                                                .margins(Insets.of(5))
                                                                .id("start_time_" + indexedLocalTimePair.index),
                                                        0,
                                                        1
                                                )
                                                .child(
                                                        Components.label(Text.translatable("playtime.playtime.timewindows.end_time"))
                                                                .margins(Insets.of(5)),
                                                        1,
                                                        0
                                                )
                                                .child(
                                                        Components.textBox(
                                                                        Sizing.fixed(100),
                                                                        indexedLocalTimePair.time2.format(DateTimeFormatter.ISO_LOCAL_TIME)
                                                                )
                                                                .margins(Insets.of(5))
                                                                .id("end_time_" + indexedLocalTimePair.index),
                                                        1,
                                                        1
                                                )
                                                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                                )
                                .child(
                                        Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                                .child(
                                                        Components.button(
                                                                Text.translatable("playtime.playtime.timewindows.save_button"),
                                                                button -> {
                                                                    TextBoxComponent startTimeBox = timeWindows.childById(TextBoxComponent.class, "start_time_" + indexedLocalTimePair.index);
                                                                    TextBoxComponent endTimeBox = timeWindows.childById(TextBoxComponent.class, "end_time_" + indexedLocalTimePair.index);
                                                                    String offset = ZoneId.systemDefault().getRules().getOffset(Instant.now()).getId();
                                                                    String startTime = startTimeBox.getText() + offset;
                                                                    String endTime = endTimeBox.getText() + offset;
                                                                    assert this.client != null;

                                                                    try {
                                                                        DateTimeFormatter.ISO_OFFSET_TIME.parse(startTime);
                                                                        DateTimeFormatter.ISO_OFFSET_TIME.parse(endTime);

                                                                        Config.TimePeriodString timePeriodString = new Config.TimePeriodString(
                                                                                startTime,
                                                                                endTime
                                                                        );

                                                                        ClientPlayNetworking.send(new ChangeTimeWindowPacket(indexedLocalTimePair.index, timePeriodString));
                                                                        ClientServerDataCache.timePeriodStringDirty = true;
                                                                    } catch (DateTimeParseException e) {
                                                                        assert this.client.player != null;
                                                                        this.client.player.sendMessage(Text.translatable("playtime.playtime.timewindows.save.invalid_time").withColor(0xFF0000));
                                                                    }

                                                                    reopenScreen();
                                                                }
                                                        )
                                                                .margins(Insets.of(5))
                                                )
                                                .child(
                                                        Components.button(
                                                                Text.translatable("playtime.playtime.timewindows.delete_button"),
                                                                button -> {
                                                                    ClientPlayNetworking.send(new RemoveTimeWindowPacket(indexedLocalTimePair.index));
                                                                    reopenScreen();
                                                                    ClientServerDataCache.timePeriodStringDirty = true;
                                                                }
                                                        )
                                                                .margins(Insets.of(5))
                                                )
                                )
                                .alignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
                                .padding(Insets.of(5))
                                .surface(Surface.outline(0xFFA0A0A0).and(Surface.flat(0x50505050)))
                                .margins(Insets.of(5))
                ));

        timeWindows.child(
                Components.button(
                        Text.translatable("playtime.playtime.timewindows.add_new_button"),
                        button -> {
                            ClientPlayNetworking.send(new AddTimeWindowPacket(new Config.TimePeriodString("00:00+00:00", "00:00+00:00")));
                            ClientServerDataCache.timePeriodStringDirty = true;
                            reopenScreen();
                        }
                )
                        .margins(Insets.of(5))
        );

        timeWindows.alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        //endregion

        rootComponent
                .child(
                        Containers.verticalScroll(
                                Sizing.content(),
                                Sizing.fill(80),
                                timeWindows
                        )
                );

    }

    private void reopenScreen() {
        assert this.client != null;
        this.client.send(() -> this.client.setScreen(new AdminPlaytimeScreen()));
    }

    private record IndexedLocalTimePair(LocalTime time1, LocalTime time2, int index) {}
}
