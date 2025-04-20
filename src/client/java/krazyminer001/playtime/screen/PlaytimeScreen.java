package krazyminer001.playtime.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import krazyminer001.playtime.networking.RequestUserPlaytimePacket;
import krazyminer001.playtime.screen.component.UpdatableLabelComponent;
import krazyminer001.playtime.tracking.ClientServerDataCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

import static krazyminer001.playtime.util.IdentifierHelper.of;

public class PlaytimeScreen extends BaseUIModelScreen<FlowLayout> {
    public PlaytimeScreen() {
        super(FlowLayout.class, DataSource.asset(of("playtime_screen")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        ClientPlayNetworking.send(new RequestUserPlaytimePacket());

        rootComponent.childById(UpdatableLabelComponent.class, "seconds-counter")
                .text(() -> Text.of(String.valueOf(ClientServerDataCache.playtime / 20)));
    }
}
