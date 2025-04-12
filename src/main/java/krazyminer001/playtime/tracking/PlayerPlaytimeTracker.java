package krazyminer001.playtime.tracking;

import krazyminer001.playtime.config.Config;
import krazyminer001.playtime.time.TimePeriod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

public class PlayerPlaytimeTracker {
    private final MinecraftServer minecraftServer;
    private final Map<UUID, Integer> playerPlaytimes = new HashMap<>();
    private LocalDate cachedDate;
    private final List<TimePeriod> nonTrackingPeriods;

    public PlayerPlaytimeTracker(MinecraftServer server) {
        minecraftServer = server;
        cachedDate = LocalDate.now(ZoneOffset.of(Config.HANDLER.instance().timezone));
        nonTrackingPeriods = Arrays.stream(Config.HANDLER.instance().nonTrackingPeriods).map(TimePeriod::new).toList();
    }

    public void tick(MinecraftServer server) {
        if (!minecraftServer.equals(server)) return;

        LocalTime now = LocalTime.now();

        if (cachedDate.isBefore(LocalDate.now(ZoneOffset.of(Config.HANDLER.instance().timezone)))) {
            playerPlaytimes.clear();
            cachedDate = LocalDate.now(ZoneOffset.of(Config.HANDLER.instance().timezone));
        }

        server.getPlayerManager().getPlayerList().forEach(player -> {
            UUID uuid = player.getUuid();
            playerPlaytimes.putIfAbsent(uuid, 0);
            if (nonTrackingPeriods.stream().noneMatch(timePeriod -> timePeriod.isWithin(now)))
                playerPlaytimes.computeIfPresent(uuid, (key, playtime) -> ++playtime);
        });

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
}
