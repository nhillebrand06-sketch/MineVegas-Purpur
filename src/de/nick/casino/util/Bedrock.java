/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package de.nick.casino.util;

import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.entity.Player;

public final class Bedrock {
    private static Object api;
    private static Method isFloodgate;
    private static boolean tried;

    private Bedrock() {
    }

    private static void init() {
        tried = true;
        try {
            Class<?> clazz = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            api = clazz.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            isFloodgate = clazz.getMethod("isFloodgatePlayer", UUID.class);
        }
        catch (Throwable throwable) {
            api = null;
        }
    }

    public static boolean isBedrock(Player player) {
        if (!tried) {
            Bedrock.init();
        }
        if (api != null) {
            try {
                return (Boolean)isFloodgate.invoke(api, player.getUniqueId());
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return player.getUniqueId().getMostSignificantBits() == 0L;
    }
}

