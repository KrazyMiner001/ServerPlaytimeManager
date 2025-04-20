package krazyminer001.playtime.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import krazyminer001.playtime.config.Config;
import krazyminer001.playtime.networking.ChangeTimeWindowPacket;
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
            assert this.client != null;
            this.client.send(() -> this.client.setScreen(new AdminPlaytimeScreen()));
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
                                        Containers.horizontalFlow(Sizing.fixed(175), Sizing.fixed(35))
                                                .child(
                                                        Components.label(Text.literal("Start Time   "))
                                                )
                                                .child(
                                                        Components.textBox(
                                                                Sizing.fixed(100),
                                                                indexedLocalTimePair.time1.format(DateTimeFormatter.ISO_LOCAL_TIME)
                                                        )
                                                                .id("start_time_" + indexedLocalTimePair.index)
                                                )
                                                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                                                .padding(Insets.of(5))
                                )
                                .child(
                                        Containers.horizontalFlow(Sizing.fixed(175), Sizing.fixed(35))
                                                .child(
                                                        Components.label(Text.literal("End Time     "))
                                                )
                                                .child(
                                                        Components.textBox(
                                                                Sizing.fixed(100),
                                                                indexedLocalTimePair.time2.format(DateTimeFormatter.ISO_LOCAL_TIME)
                                                        )
                                                                .id("end_time_" + indexedLocalTimePair.index)
                                                )
                                                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                                                .padding(Insets.of(5))
                                )
                                .child(
                                        Containers.horizontalFlow(Sizing.content(), Sizing.content())
                                                .child(
                                                        Components.button(
                                                                Text.literal("Save"),
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
                                                                        this.client.player.sendMessage(Text.literal("Invalid start or end time").withColor(0xFF0000));
                                                                    }

                                                                    this.client.send(() -> this.client.setScreen(new AdminPlaytimeScreen()));
                                                                }
                                                        )
                                                )
                                                .padding(Insets.of(5))
                                )
                                .alignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
                                .padding(Insets.of(5))
                                .surface(Surface.outline(0xFFA0A0A0).and(Surface.flat(0x50505050)))
                                .margins(Insets.of(5))
                ));
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

    private record IndexedLocalTimePair(LocalTime time1, LocalTime time2, int index) {}
}
