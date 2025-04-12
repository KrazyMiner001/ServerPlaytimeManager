package krazyminer001.playtime;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import krazyminer001.playtime.config.Config;
import krazyminer001.playtime.tracking.PlayerPlaytimeTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ServerPlaytimeManager implements ModInitializer {
	public static final String MOD_ID = "playtime";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static PlayerPlaytimeTracker PLAYTIME_TRACKER;

	@Override
	public void onInitialize() {
		Config.HANDLER.load();

		ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer -> PLAYTIME_TRACKER = PlayerPlaytimeTracker.getServerState(minecraftServer)));

		ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {if (PLAYTIME_TRACKER != null) PLAYTIME_TRACKER.tick(minecraftServer);});

        //noinspection CodeBlock2Expr
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("playerplaytime")
					.then(literal("query")
							.executes(context -> {
								if (!context.getSource().isExecutedByPlayer()) {
									context.getSource().sendError(Text.literal("You must be a player or specify a player target to run this command"));
									return 0;
								}
								assert context.getSource().getEntity() != null;
								context.getSource().sendFeedback(() -> Text.literal("You have played: " + PLAYTIME_TRACKER.getPlaytime(context.getSource().getEntity().getUuid()).toSeconds() + " seconds today"), false);
								return 1;
							})
							.then(argument("targets", GameProfileArgumentType.gameProfile())
									.requires(source -> source.hasPermissionLevel(2))
									.executes(context -> {
										final Collection<GameProfile> gameProfiles = GameProfileArgumentType.getProfileArgument(context, "targets");
										gameProfiles.forEach(profile -> context
                                                .getSource()
                                                .sendFeedback(
                                                        () -> Text.literal(profile.getName() + " has played " + PLAYTIME_TRACKER.getPlaytime(profile.getId()).toSeconds() + " seconds today"),
                                                        false
                                                ));
										return 1;
									})
							)
					)
					.then(literal("set")
							.requires(source -> source.hasPermissionLevel(3))
							.then(argument("targets", GameProfileArgumentType.gameProfile())
									.then(argument("ticks", IntegerArgumentType.integer())
											.executes(context -> {
												final Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "targets");
												final int ticks = IntegerArgumentType.getInteger(context, "ticks");

												profiles.forEach(profile -> {
													PLAYTIME_TRACKER.setPlaytime(profile.getId(), ticks);
													context.getSource().sendFeedback(() -> Text.literal("Successfully set playtime to " + ticks + " ticks for player " + profile.getName()), true);
												});

												return 1;
											})
									)
							)
					)
			);
		});
	}
}