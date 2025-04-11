package krazyminer001.playtime.tracking;

import net.minecraft.server.MinecraftServer;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerPlaytimeTracker {
    private final MinecraftServer minecraftServer;
    private final Map<UUID, Integer> playerPlaytimes = new HashMap<>();
    private LocalDate cachedDate;

    public PlayerPlaytimeTracker(MinecraftServer server) {
        minecraftServer = server;
        cachedDate = LocalDate.now();
    }

    public void tick(MinecraftServer server) {
        if (!minecraftServer.equals(server)) return;

        server.getPlayerManager().getPlayerList().forEach(player -> {
            UUID uuid = player.getUuid();
            playerPlaytimes.putIfAbsent(uuid, 0);
            playerPlaytimes.computeIfPresent(uuid, (key, playtime) -> ++playtime);
        });

        if (cachedDate.isBefore(LocalDate.now())) {
            playerPlaytimes.clear();
            cachedDate = LocalDate.now();
        }
    }
}
