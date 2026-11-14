package io.github.brainage04.brainageserverutils.gametest;

import io.github.brainage04.brainageserverutils.util.CurrentServer;
import net.minecraft.server.MinecraftServer;

/// Each loader's GameTest source set calls [#serverStarting] after the mod's own server-starting
/// listener.
public final class StartPhaseProbe {
    private static volatile Boolean knownWhileStarting;

    private StartPhaseProbe() {}

    public static void serverStarting(MinecraftServer server) {
        knownWhileStarting = CurrentServer.get() == server;
    }

    static Boolean knownWhileStarting() {
        return knownWhileStarting;
    }
}
