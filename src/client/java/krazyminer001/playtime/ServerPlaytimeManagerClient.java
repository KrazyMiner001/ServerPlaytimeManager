package krazyminer001.playtime;

import io.wispforest.owo.ui.parsing.UIParsing;
import krazyminer001.playtime.screen.AdminPlaytimeScreen;
import krazyminer001.playtime.screen.PlaytimeScreen;
import krazyminer001.playtime.screen.component.UpdatableLabelComponent;
import krazyminer001.playtime.util.IdentifierHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@SuppressWarnings("unused")
public class ServerPlaytimeManagerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UIParsing.registerFactory(IdentifierHelper.of("updatable-label"), element -> new UpdatableLabelComponent(Text::empty));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("foo_client")
				.executes(context -> {
					MinecraftClient mc = MinecraftClient.getInstance();
					Screen screen = new PlaytimeScreen();
					mc.send(() -> mc.setScreen(screen));
					return 1;
				})
				.then(ClientCommandManager.literal("admin")
						.requires((source) -> source.hasPermissionLevel(3))
						.executes(context -> {
							MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(new AdminPlaytimeScreen()));
							return 1;
						})
				)
		));
	}
}