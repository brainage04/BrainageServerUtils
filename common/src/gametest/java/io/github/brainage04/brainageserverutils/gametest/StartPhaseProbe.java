package io.github.brainage04.brainageserverutils.gametest;

import io.github.brainage04.brainageserverutils.util.CurrentServer;
import net.minecraft.server.MinecraftServer;

/// Each loader's GameTest source set calls [#serverStarting] from the loader's first server
/// lifecycle event, after the mod's own listener for it.
public final class StartPhaseProbe {
    private static volatile Boolean knownWhileStarting;
    private static volatile Boolean levelsLoaded;

    private StartPhaseProbe() {}

    public static void serverStarting(MinecraftServer server) {
        knownWhileStarting = CurrentServer.get() == server;
        levelsLoaded = server.getAllLevels().iterator().hasNext();
    }

    static Boolean knownWhileStarting() {
        return knownWhileStarting;
    }

    static Boolean levelsLoaded() {
        return levelsLoaded;
    }
}
