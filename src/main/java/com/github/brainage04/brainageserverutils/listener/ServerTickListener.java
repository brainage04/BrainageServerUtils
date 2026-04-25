package com.github.brainage04.brainageserverutils.listener;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

public class ServerTickListener implements ServerTickEvents.EndTick {
    private static MinecraftServer server;

    public static MinecraftServer getServer() {
        return server;
    }

    public static GameRules getGameRules() {
        return server.getGameRules();
    }

    public static boolean isGameRuleActive(GameRules.Key<GameRules.BooleanRule> rule) {
        return server.getGameRules().getBoolean(rule);
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        ServerTickListener.server = server;
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(new ServerTickListener());
    }
}