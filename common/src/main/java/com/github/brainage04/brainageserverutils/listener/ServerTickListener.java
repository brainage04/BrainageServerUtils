package com.github.brainage04.brainageserverutils.listener;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

public final class ServerTickListener {
    private static volatile MinecraftServer server;

    public static MinecraftServer getServer() {
        return server;
    }

    public static GameRules getGameRules() {
        MinecraftServer currentServer = server;
        return currentServer == null ? null : currentServer.getGameRules();
    }

    public static boolean isGameRuleActive(GameRule<Boolean> rule) {
        GameRules rules = getGameRules();
        return rules != null && rules.get(rule);
    }


    public static void onServerStarting(MinecraftServer currentServer) {
        server = currentServer;
    }

    public static void onServerStopped(MinecraftServer stoppedServer) {
        if (server == stoppedServer) {
            server = null;
        }
    }
}
