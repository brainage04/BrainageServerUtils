package io.github.brainage04.brainageserverutils;

import io.github.brainage04.brainageserverutils.gametest.StartPhaseProbe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

public final class StartPhaseProbeFabric implements ModInitializer {
    private static final Identifier AFTER_MOD =
            Identifier.fromNamespaceAndPath(BrainageServerUtils.MOD_ID, "start_phase_probe");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.addPhaseOrdering(Event.DEFAULT_PHASE, AFTER_MOD);
        ServerLifecycleEvents.SERVER_STARTING.register(AFTER_MOD, StartPhaseProbe::serverStarting);
    }
}
