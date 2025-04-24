package krazyminer001.playtime;

import io.wispforest.owo.ui.parsing.UIParsing;
import krazyminer001.playtime.networking.SendTimeWindowsPacket;
import krazyminer001.playtime.networking.SendTimezonePacket;
import krazyminer001.playtime.networking.SendUserPlaytimePacket;
import krazyminer001.playtime.screen.AdminPlaytimeScreen;
import krazyminer001.playtime.screen.PlaytimeScreen;
import krazyminer001.playtime.screen.component.UpdatableLabelComponent;
import krazyminer001.playtime.tracking.ClientServerDataCache;
import krazyminer001.playtime.util.IdentifierHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

@SuppressWarnings("unused")
public class ServerPlaytimeManagerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UIParsing.registerFactory(IdentifierHelper.of("updatable-label"), element -> new UpdatableLabelComponent(Text::empty));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("playerplaytime")
				.then(ClientCommandManager.literal("client")
						.executes(context -> {
							MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(new PlaytimeScreen()));
							return 1;
						})
						.then(ClientCommandManager.literal("timewindows")
								.executes(context -> {
									MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(new AdminPlaytimeScreen()));
									return 1;
								})
						)
				)
		));

		ClientTickEvents.END_WORLD_TICK.register((minecraftClient -> ClientServerDataCache.playtime++));

		ClientPlayNetworking.registerGlobalReceiver(SendUserPlaytimePacket.ID, (sendUserPlaytimePacket, context) ->
			ClientServerDataCache.playtime = sendUserPlaytimePacket.playtime()
		);
		ClientPlayNetworking.registerGlobalReceiver(SendTimeWindowsPacket.ID, (sendTimeWindowsPacket, context) -> {
				ClientServerDataCache.timePeriodStrings = sendTimeWindowsPacket.timePeriods();
				ClientServerDataCache.timePeriodStringDirty = false;
		});
		ClientPlayNetworking.registerGlobalReceiver(SendTimezonePacket.ID, (sendTimezonePacket, context) ->
				ClientServerDataCache.midnightTimezone = sendTimezonePacket.zoneOffset()
		);
	}
}