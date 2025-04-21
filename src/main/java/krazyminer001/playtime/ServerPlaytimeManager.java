package krazyminer001.playtime;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import krazyminer001.playtime.config.Config;
import krazyminer001.playtime.networking.*;
import krazyminer001.playtime.tracking.PlayerPlaytimeTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.text.Text;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

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
					.then(literal("config")
							.requires(source -> source.hasPermissionLevel(2))
							.then(literal("reload")
									.executes(context -> {
										Config.HANDLER.load();
										PLAYTIME_TRACKER.reload();
										return 1;
									})
							)
							.then(literal("set")
									.then(literal("maxTime")
											.then(argument("ticks", IntegerArgumentType.integer())
													.executes(context -> {
														Config.HANDLER.instance().maxTime = IntegerArgumentType.getInteger(context, "ticks");
														Config.HANDLER.save();
														Config.HANDLER.load();
														return 1;
													})
											)
									)
							)
							.then(literal("get")
									.then(literal("maxTime")
											.executes(context -> {
												context.getSource().sendFeedback(() -> Text.literal(String.valueOf(Config.HANDLER.instance().maxTime)), false);
												return 1;
											})
									)
							)
							.then(literal("timewindows")
									.then(literal("add")
											.then(argument("startTime", StringArgumentType.word())
													.then(argument("endTime", StringArgumentType.word())
															.executes(context -> {
																try {
																	DateTimeFormatter.ISO_OFFSET_TIME.parse(StringArgumentType.getString(context, "startTime"));
																} catch (DateTimeParseException e) {
																	context
																			.getSource()
																			.sendFeedback(
																					() -> Text.literal("Invalied startTime: " + e.getMessage()),
																					false
																			);
																	return 0;
																}
																try {
																	DateTimeFormatter.ISO_OFFSET_TIME.parse(StringArgumentType.getString(context, "endTime"));
																} catch (DateTimeParseException e) {
																	context
																			.getSource()
																			.sendFeedback(
																					() -> Text.literal("Invalied endTime: " + e.getMessage()),
																					false
																			);
																	return 0;
																}

																Config.HANDLER.instance().nonTrackingPeriods =
																		ArrayUtils.add(
																					Config.HANDLER.instance().nonTrackingPeriods,
																					new Config.TimePeriodString(
																							StringArgumentType.getString(context, "startTime"),
																							StringArgumentType.getString(context, "endTime")
																					)
																				);

																Config.HANDLER.save();
																PLAYTIME_TRACKER.reload();
																return 1;
															})
													)
											)
									)
									.then(literal("remove")
											.then(argument("startTime", StringArgumentType.word())
													.then(argument("endTime", StringArgumentType.word())
															.executes(context -> {
																final String startTime = StringArgumentType.getString(context, "startTime");
																final String endTime = StringArgumentType.getString(context, "endTime");

																try {
																	DateTimeFormatter.ISO_OFFSET_TIME.parse(startTime);
																} catch (DateTimeParseException e) {
																	context
																			.getSource()
																			.sendFeedback(
																					() -> Text.literal("Invalied startTime: " + e.getMessage()),
																					false
																			);
																	return 0;
																}
																try {
																	DateTimeFormatter.ISO_OFFSET_TIME.parse(endTime);
																} catch (DateTimeParseException e) {
																	context
																			.getSource()
																			.sendFeedback(
																					() -> Text.literal("Invalied endTime: " + e.getMessage()),
																					false
																			);
																	return 0;
																}

																Stream<Config.TimePeriodString> times = Arrays.stream(Config.HANDLER.instance().nonTrackingPeriods);
																if (times.noneMatch((time -> time.equals(new Config.TimePeriodString(startTime, endTime))))) {
																	context
																			.getSource()
																			.sendFeedback(
																					() -> Text.literal("There is no period defined by startTime: " + startTime + " and endTime: " + endTime),
																					false
																			);
																	return 0;
																}

																times = Arrays.stream(Config.HANDLER.instance().nonTrackingPeriods);

																Config.HANDLER.instance().nonTrackingPeriods = times.filter(time -> !time.equals(new Config.TimePeriodString(startTime, endTime))).toArray(Config.TimePeriodString[]::new);

																Config.HANDLER.save();
																PLAYTIME_TRACKER.reload();
																return 1;
															})
													)
											)
									)
									.then(literal("list")
											.executes(context -> {
												Arrays.stream(Config.HANDLER.instance().nonTrackingPeriods)
														.forEach(period -> context
                                                                .getSource()
                                                                .sendFeedback(
                                                                        () -> Text.literal(period.display()),
                                                                        false
                                                                ));

												return 1;
											})
									)
							)
					)
			);
		});

		PayloadTypeRegistry.playC2S().register(RequestUserPlaytimePacket.ID, RequestUserPlaytimePacket.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestTimeWindowsPacket.ID, RequestTimeWindowsPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(ChangeTimeWindowPacket.ID, ChangeTimeWindowPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(RemoveTimeWindowPacket.ID, RemoveTimeWindowPacket.CODEC);

		PayloadTypeRegistry.playS2C().register(SendUserPlaytimePacket.ID, SendUserPlaytimePacket.CODEC);
		PayloadTypeRegistry.playS2C().register(SendTimeWindowsPacket.ID, SendTimeWindowsPacket.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(RequestUserPlaytimePacket.ID, (payload, context) ->
				context.responseSender().sendPacket(new SendUserPlaytimePacket(PLAYTIME_TRACKER.getPlaytimeTicks(context.player().getUuid())))
		);
		ServerPlayNetworking.registerGlobalReceiver(RequestTimeWindowsPacket.ID, (payload, context) ->
				context.responseSender().sendPacket(new SendTimeWindowsPacket(Arrays.asList(Config.HANDLER.instance().nonTrackingPeriods)))
		);
		ServerPlayNetworking.registerGlobalReceiver(ChangeTimeWindowPacket.ID, (payload, context) -> {
			if (context.player().hasPermissionLevel(3)) {
				if (Config.HANDLER.instance().nonTrackingPeriods.length > payload.index()) {
					Config.HANDLER.instance().nonTrackingPeriods[payload.index()] = payload.timePeriodString();
					Config.HANDLER.save();
				} else {
					context.player().sendMessage(Text.literal("Invalid time period change, the time periods may have changed after you opened your screen").withColor(0xFF0000));
				}
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(RemoveTimeWindowPacket.ID, ((payload, context) -> {
			if (context.player().hasPermissionLevel(3)) {
				Config.TimePeriodString[] timePeriodStrings = Config.HANDLER.instance().nonTrackingPeriods;

				List<Config.TimePeriodString> newTimePeriodStrings = Arrays.asList(timePeriodStrings);
				newTimePeriodStrings.remove(payload.index());
				Config.HANDLER.instance().nonTrackingPeriods = newTimePeriodStrings.toArray(Config.TimePeriodString[]::new);
			}
		}));
	}
}