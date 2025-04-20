package krazyminer001.playtime.tracking;

import krazyminer001.playtime.ServerPlaytimeManager;
import krazyminer001.playtime.config.Config;
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
        nonTrackingPeriods = Arrays.stream(Config.HANDLER.instance().nonTrackingPeriods).map(TimePeriod::new).collect(Collectors.toList());
    }

    public void reload() {
        nonTrackingPeriods.clear();
        nonTrackingPeriods.addAll(Arrays.stream(Config.HANDLER.instance().nonTrackingPeriods).map(TimePeriod::new).toList());
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
        playerPlaytimeTracker.cachedDate = LocalDate.now(Config.getZoneOffset());
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
        LocalTime now = LocalTime.now(Config.getZoneOffset());

        if (cachedDate.isBefore(LocalDate.now(Config.getZoneOffset()))) {
            playerPlaytimes.clear();
            cachedDate = LocalDate.now(Config.getZoneOffset());
        }

        server.getPlayerManager().getPlayerList().forEach(player -> {
            UUID uuid = player.getUuid();
            playerPlaytimes.putIfAbsent(uuid, 0);
            if (nonTrackingPeriods.stream().noneMatch(timePeriod -> timePeriod.isWithin(now)))
                playerPlaytimes.computeIfPresent(uuid, (key, playtime) -> ++playtime);
        });

        markDirty();

        Optional<TimePeriod> soonestTimePeriod = nonTrackingPeriods.stream().min(Comparator.comparing(period -> period.until(now)));

        if (Config.HANDLER.instance().maxTime >= 0) {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                if (playerPlaytimes.get(player.getUuid()) > Config.HANDLER.instance().maxTime) {
                    player.networkHandler.disconnect(Text.translatableWithFallback(
                            "playtime.disconnect.overtime",
                            "You have exceeded your playtime",
                            Config.HANDLER.instance().maxTime,
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
