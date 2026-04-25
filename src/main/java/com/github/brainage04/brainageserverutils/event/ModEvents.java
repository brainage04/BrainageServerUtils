package com.github.brainage04.brainageserverutils.event;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ModEvents {
    public static void initialize() {
        // set appropriate attack cooldown for new players
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ModGameRules.modifyAttackSpeed(
                    server.getGameRules().getBoolean(ModGameRules.DISABLE_ATTACK_COOLDOWN),
                    handler.player
            );
        });

        // set appropriate trident cooldown on server startup
        ServerLifecycleEvents.SERVER_STARTED.register(ModGameRules::updateTridentCooldown);
    }
}
