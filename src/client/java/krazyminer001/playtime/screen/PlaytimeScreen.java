package krazyminer001.playtime.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import krazyminer001.playtime.ServerPlaytimeManager;
import krazyminer001.playtime.screen.component.UpdatableLabelComponent;
import net.minecraft.text.Text;

import java.time.temporal.ChronoUnit;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public class PlaytimeScreen extends BaseUIModelScreen<FlowLayout> {
    public PlaytimeScreen() {
        super(FlowLayout.class, DataSource.asset(of("playtime_screen")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(UpdatableLabelComponent.class, "seconds-counter")
                .text(() -> Text.of(String.valueOf(ServerPlaytimeManager.PLAYTIME_TRACKER.getPlaytime(this.client.player.getUuid()).get(ChronoUnit.SECONDS))));
    }
}
