package krazyminer001.playtime;

import krazyminer001.playtime.screen.AdminPlaytimeScreen;
import krazyminer001.playtime.screen.PlaytimeScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

@SuppressWarnings("unused")
public class ServerPlaytimeManagerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("foo_client")
				.executes(context -> {
					MinecraftClient mc = MinecraftClient.getInstance();
					Screen screen = context.getSource().hasPermissionLevel(2) ? new AdminPlaytimeScreen() : new PlaytimeScreen();
					mc.send(() -> mc.setScreen(screen));
					return 1;
				}
				)));
	}
}