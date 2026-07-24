package com.github.brainage04.brainageserverutils.listener;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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


    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(currentServer -> server = currentServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(stoppedServer -> {
            if (server == stoppedServer) {
                server = null;
            }
        });
    }
}
