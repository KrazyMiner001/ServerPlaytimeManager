package krazyminer001.playtime.util;

import krazyminer001.playtime.ServerPlaytimeManager;
import net.minecraft.util.Identifier;

public class IdentifierHelper {
    public static Identifier of(String id) {
        return Identifier.of(ServerPlaytimeManager.MOD_ID, id);
    }
}
