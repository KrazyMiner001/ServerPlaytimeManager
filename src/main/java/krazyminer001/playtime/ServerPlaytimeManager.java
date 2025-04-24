package krazyminer001.playtime;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import krazyminer001.playtime.config.PlaytimeConfig;
import krazyminer001.playtime.config.TimePeriodString;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
	public static PlaytimeConfig PLAYTIME_CONFIG;

	@Override
	public void onInitialize() {
		PLAYTIME_CONFIG = PlaytimeConfig.createAndLoad();

		ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer -> PLAYTIME_TRACKER = PlayerPlaytimeTracker.getServerState(minecraftServer)));

		ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {if (PLAYTIME_TRACKER != null) PLAYTIME_TRACKER.tick(minecraftServer);});

        //noinspection CodeBlock2Expr
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("playerplaytime")
					.then(literal("query")
							.executes(context -> {
								if (!context.getSource().isExecutedByPlayer()) {
									context.getSource().sendError(Text.translatableWithFallback("commands.playtime.query.not_a_player", "You must be a player or specify a player target to run this command"));
									return 0;
								}
								assert context.getSource().getEntity() != null;
								long seconds = PLAYTIME_TRACKER.getPlaytime(context.getSource().getEntity().getUuid()).toSeconds();
								context.getSource().sendFeedback(() -> Text.translatableWithFallback("commands.playtime.query.user_playtime", "You have played: " + seconds +" seconds today", seconds), false);
								return 1;
							})
							.then(argument("targets", GameProfileArgumentType.gameProfile())
									.requires(source -> source.hasPermissionLevel(2))
									.executes(context -> {
										final Collection<GameProfile> gameProfiles = GameProfileArgumentType.getProfileArgument(context, "targets");
										gameProfiles.forEach(profile -> context
                                                .getSource()
                                                .sendFeedback(
														() -> Text.translatableWithFallback("commands.playtime.query.player_playtime",  profile.getName() + " has played " + PLAYTIME_TRACKER.getPlaytime(profile.getId()).toSeconds() +" seconds today", profile.getName(), PLAYTIME_TRACKER.getPlaytime(profile.getId()).toSeconds()),
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
													context.getSource().sendFeedback(
															() -> Text.translatableWithFallback("commands.playtime.set.success", "Successfully set playtime to " + ticks + " ticks for player " + profile.getName(), ticks, profile.getName()),
															true
													);
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
										PLAYTIME_CONFIG.load();
										PLAYTIME_TRACKER.reload();
										return 1;
									})
							)
							.then(literal("set")
									.then(literal("maxTime")
											.then(argument("ticks", IntegerArgumentType.integer())
													.executes(context -> {
														PLAYTIME_CONFIG.maxTime(IntegerArgumentType.getInteger(context, "ticks"));
														PLAYTIME_CONFIG.save();
														return 1;
													})
											)
									)
									.then(literal("timezone")
											.then(argument("timezone", StringArgumentType.string())
													.executes(context -> {
														PLAYTIME_CONFIG.timezone(StringArgumentType.getString(context, "timezone"));
														return 1;
													})
											)
									)
							)
							.then(literal("get")
									.then(literal("maxTime")
											.executes(context -> {
												context.getSource().sendFeedback(() -> Text.of(String.valueOf(PLAYTIME_CONFIG.maxTime())), false);
												return 1;
											})
									)
									.then(literal("timezone")
											.executes(context -> {
												context.getSource().sendFeedback(() -> Text.of(String.valueOf(PLAYTIME_CONFIG.timezone())), false);
												return 1;
											})
									)
							)
							.then(literal("timewindows")
									.then(literal("add")
											.then(argument("startTime", StringArgumentType.string())
													.then(argument("endTime", StringArgumentType.string())
															.executes(context -> {
																try {
																	DateTimeFormatter.ISO_OFFSET_TIME.parse(StringArgumentType.getString(context, "startTime"));
																} catch (DateTimeParseException e) {
																	context
																			.getSource()
																			.sendFeedback(
																					() -> Text.translatableWithFallback("commands.playtime.timewindows.add.invalid_start_time", "Invalid startTime: " + e.getMessage(), e.getMessage()),
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
																					() -> Text.translatableWithFallback("commands.playtime.timewindows.add.invalid_end_time", "Invalid endTime:" + e.getMessage(), e.getMessage()),
																					false
																			);
																	return 0;
																}

																PLAYTIME_CONFIG.timePeriods().add(
																		new TimePeriodString(
																				StringArgumentType.getString(context, "startTime"),
																				StringArgumentType.getString(context, "endTime")
																		)
																);

																PLAYTIME_CONFIG.save();
																PLAYTIME_TRACKER.reload();
																return 1;
															})
													)
											)
									)
									.then(literal("remove")
											.then(argument("startTime", StringArgumentType.string())
													.then(argument("endTime", StringArgumentType.string())
															.executes(context -> {
																final String startTime = StringArgumentType.getString(context, "startTime");
																final String endTime = StringArgumentType.getString(context, "endTime");

																try {
																	DateTimeFormatter.ISO_OFFSET_TIME.parse(startTime);
																} catch (DateTimeParseException e) {
																	context
																			.getSource()
																			.sendFeedback(
																					() -> Text.translatableWithFallback("commands.playtime.timewindows.remove.invalid_start_time", "Invalid startTime: " + e.getMessage(), e.getMessage()),
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
																					() -> Text.translatable("commands.playtime.timewindows.remove.invalid_end_time", "Invalid endTime: " + e.getMessage(), e.getMessage()),
																					false
																			);
																	return 0;
																}

																if (PLAYTIME_CONFIG.timePeriods().stream().noneMatch((time -> time.equals(new TimePeriodString(startTime, endTime))))) {
																	context
																			.getSource()
																			.sendFeedback(
																					() -> Text.translatableWithFallback("commands.playtime.timewindows.remove.non_existent_timewindow", "There is no period defined by startTime: " + startTime + " and endTime: " + endTime, startTime, endTime),
																					false
																			);
																	return 0;
																}

																PLAYTIME_CONFIG.timePeriods().remove(new TimePeriodString(startTime, endTime));
																PLAYTIME_CONFIG.save();

																PLAYTIME_TRACKER.reload();
																return 1;
															})
													)
											)
									)
									.then(literal("list")
											.executes(context -> {
												PLAYTIME_CONFIG.timePeriods()
														.forEach(period -> context
                                                                .getSource()
                                                                .sendFeedback(
                                                                        period::display,
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
		PayloadTypeRegistry.playC2S().register(AddTimeWindowPacket.ID, AddTimeWindowPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestTimezonePacket.ID, RequestTimezonePacket.CODEC);

		PayloadTypeRegistry.playS2C().register(SendUserPlaytimePacket.ID, SendUserPlaytimePacket.CODEC);
		PayloadTypeRegistry.playS2C().register(SendTimeWindowsPacket.ID, SendTimeWindowsPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(SendTimezonePacket.ID, SendTimezonePacket.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(RequestUserPlaytimePacket.ID, (payload, context) ->
				context.responseSender().sendPacket(new SendUserPlaytimePacket(PLAYTIME_TRACKER.getPlaytimeTicks(context.player().getUuid())))
		);
		ServerPlayNetworking.registerGlobalReceiver(RequestTimeWindowsPacket.ID, (payload, context) ->
				context.responseSender().sendPacket(new SendTimeWindowsPacket(PLAYTIME_CONFIG.timePeriods()))
		);
		ServerPlayNetworking.registerGlobalReceiver(ChangeTimeWindowPacket.ID, (payload, context) -> {
			if (context.player().hasPermissionLevel(3)) {
				if (PLAYTIME_CONFIG.timePeriods().size() <= payload.index()) {
					context.player().sendMessage(Text.translatable("playtime.playtime.timewindows.invalid_time_period_change").withColor(0xFF0000));
					return;
				}
				PLAYTIME_CONFIG.timePeriods().set(payload.index(), payload.timePeriodString());
				PLAYTIME_CONFIG.save();
				PLAYTIME_TRACKER.reload();
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(RemoveTimeWindowPacket.ID, ((payload, context) -> {
			if (context.player().hasPermissionLevel(3)) {
				PLAYTIME_CONFIG.timePeriods().remove(payload.index());
				PLAYTIME_CONFIG.save();
				PLAYTIME_TRACKER.reload();
			}
		}));
		ServerPlayNetworking.registerGlobalReceiver(AddTimeWindowPacket.ID, ((payload, context) -> {
			if (context.player().hasPermissionLevel(3)) {
				PLAYTIME_CONFIG.timePeriods().add(payload.timePeriodString());
				PLAYTIME_CONFIG.save();
				PLAYTIME_TRACKER.reload();
			}
		}));
		ServerPlayNetworking.registerGlobalReceiver(RequestTimezonePacket.ID, (payload, context) ->
				context.responseSender().sendPacket(new SendTimezonePacket(ZoneOffset.of(PLAYTIME_CONFIG.timezone())))
		);
	}
}