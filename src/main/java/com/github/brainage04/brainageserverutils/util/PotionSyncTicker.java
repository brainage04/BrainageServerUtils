package com.github.brainage04.brainageserverutils.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class PotionSyncTicker {
    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(PotionSyncTicker::onEndTick);
    }

    private static void onEndTick(MinecraftServer server) {
        if (server.getTicks() % 10 != 0) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            for (StatusEffectInstance effect : player.getStatusEffects()) {
                player.networkHandler.sendPacket(
                        new EntityStatusEffectS2CPacket(player.getId(), effect, true)
                );
            }
        }
    }
}