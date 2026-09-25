package io.github.brainage04.brainageserverutils.util;

import net.minecraft.server.MinecraftServer;

/// Tracks the running server for code paths that have no level or entity to read gamerules from.
public final class CurrentServer {
    private static volatile MinecraftServer server;

    private CurrentServer() {
    }

    public static MinecraftServer get() {
        return server;
    }

    public static void onServerStarting(MinecraftServer startingServer) {
        server = startingServer;
    }

    public static void onServerStopped(MinecraftServer stoppedServer) {
        if (server == stoppedServer) {
            server = null;
        }
    }
}
