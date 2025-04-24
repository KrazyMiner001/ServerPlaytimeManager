package krazyminer001.playtime.tracking;

import krazyminer001.playtime.ServerPlaytimeManager;
import krazyminer001.playtime.time.TimePeriod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerPlaytimeTracker extends PersistentState {
    private final Map<UUID, Integer> playerPlaytimes;
    private LocalDate cachedDate;
    private final List<TimePeriod> nonTrackingPeriods;

    private static final Type<PlayerPlaytimeTracker> type = new Type<>(
            PlayerPlaytimeTracker::createNew,
            PlayerPlaytimeTracker::createFromNbt,
            null
    );

    private PlayerPlaytimeTracker() {
        playerPlaytimes = new HashMap<>();
        nonTrackingPeriods = ServerPlaytimeManager.PLAYTIME_CONFIG.timePeriods().stream().map(timePeriodString -> new TimePeriod(timePeriodString, ZoneOffset.of(ServerPlaytimeManager.PLAYTIME_CONFIG.timezone()))).collect(Collectors.toList());
    }

    public void reload() {
        nonTrackingPeriods.clear();
        nonTrackingPeriods.addAll(ServerPlaytimeManager.PLAYTIME_CONFIG.timePeriods().stream().map(timePeriodString -> new TimePeriod(timePeriodString, ZoneOffset.of(ServerPlaytimeManager.PLAYTIME_CONFIG.timezone()))).toList());
    }
    
    public static PlayerPlaytimeTracker createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        PlayerPlaytimeTracker playerPlaytimeTracker = new PlayerPlaytimeTracker();
        playerPlaytimeTracker.cachedDate = LocalDate.parse(tag.getString("cachedDate"), DateTimeFormatter.ISO_DATE);
        NbtCompound playtimes = tag.getCompound("playerPlaytimes");
        playtimes.getKeys().forEach(key -> playerPlaytimeTracker.playerPlaytimes.put(UUID.fromString(key), playtimes.getInt(key)));
        return playerPlaytimeTracker;
    }
    
    public static PlayerPlaytimeTracker createNew() {
        PlayerPlaytimeTracker playerPlaytimeTracker = new PlayerPlaytimeTracker();
        playerPlaytimeTracker.cachedDate = LocalDate.now(ZoneOffset.of(ServerPlaytimeManager.PLAYTIME_CONFIG.timezone()));
        return playerPlaytimeTracker;
    }

    public static PlayerPlaytimeTracker getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(ServerWorld.OVERWORLD);
        assert serverWorld != null;

        PlayerPlaytimeTracker playerPlaytimeTracker = serverWorld.getPersistentStateManager().getOrCreate(type, ServerPlaytimeManager.MOD_ID);
        playerPlaytimeTracker.markDirty();

        return playerPlaytimeTracker;
    }

    public Duration getPlaytime(UUID uuid) {
        return Duration.of(playerPlaytimes.get(uuid) * 50, ChronoUnit.MILLIS);
    }

    public int getPlaytimeTicks(UUID uuid) {
        return playerPlaytimes.get(uuid);
    }

    public void setPlaytime(UUID uuid, int ticks) {
        playerPlaytimes.put(uuid, ticks);
        markDirty();
    }

    public void tick(MinecraftServer server) {
        LocalTime now = LocalTime.now(ZoneOffset.of(ServerPlaytimeManager.PLAYTIME_CONFIG.timezone()));

        if (cachedDate.isBefore(LocalDate.now(ZoneOffset.of(ServerPlaytimeManager.PLAYTIME_CONFIG.timezone())))) {
            playerPlaytimes.clear();
            cachedDate = LocalDate.now(ZoneOffset.of(ServerPlaytimeManager.PLAYTIME_CONFIG.timezone()));
        }

        if (cachedDate.isAfter(LocalDate.now(ZoneOffset.of(ServerPlaytimeManager.PLAYTIME_CONFIG.timezone())))) {
            cachedDate = LocalDate.now(ZoneOffset.of(ServerPlaytimeManager.PLAYTIME_CONFIG.timezone()));
        }

        server.getPlayerManager().getPlayerList().forEach(player -> {
            UUID uuid = player.getUuid();
            playerPlaytimes.putIfAbsent(uuid, 0);
            if (nonTrackingPeriods.stream().noneMatch(timePeriod -> timePeriod.isWithin(now))) {
                playerPlaytimes.computeIfPresent(uuid, (key, playtime) -> ++playtime);
            }
        });

        markDirty();

        Optional<TimePeriod> soonestTimePeriod = nonTrackingPeriods.stream().min(Comparator.comparing(period -> period.until(now)));

        if (ServerPlaytimeManager.PLAYTIME_CONFIG.maxTime() >= 0) {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                if (playerPlaytimes.get(player.getUuid()) > ServerPlaytimeManager.PLAYTIME_CONFIG.maxTime()) {
                    player.networkHandler.disconnect(Text.translatableWithFallback(
                            "disconnect.playtime.overtime",
                            "You have exceeded your playtime",
                            ServerPlaytimeManager.PLAYTIME_CONFIG.maxTime(),
                            soonestTimePeriod.map(TimePeriod::display).orElse("there don't seem to be any time periods")
                    ));
                }
            });
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putString("cachedDate", cachedDate.format(DateTimeFormatter.ISO_DATE));
        NbtCompound playtimesMap = new NbtCompound();
        playerPlaytimes.forEach((uuid, playtime) -> playtimesMap.putInt(uuid.toString(), playtime));
        nbt.put("playerPlaytimes", playtimesMap);
        return nbt;
    }
}
