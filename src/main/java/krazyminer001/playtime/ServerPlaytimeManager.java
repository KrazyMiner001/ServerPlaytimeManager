package krazyminer001.playtime;

import krazyminer001.playtime.tracking.PlayerPlaytimeTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerPlaytimeManager implements ModInitializer {
	public static final String MOD_ID = "playtime";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static PlayerPlaytimeTracker PLAYTIME_TRACKER;

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer -> PLAYTIME_TRACKER = new PlayerPlaytimeTracker(minecraftServer)));

		ServerTickEvents.END_SERVER_TICK.register((PLAYTIME_TRACKER::tick));
	}
}