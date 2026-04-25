package com.github.brainage04.brainageserverutils.util;

import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerUtils {
    public static void updateHotbar(ServerPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            updateHotbarSlot(player, i);
        }
    }

    public static void updateSelectedSlot(ServerPlayerEntity player) {
        updateHotbarSlot(player, player.getInventory().getSelectedSlot());
    }

    public static void updateHotbarSlot(ServerPlayerEntity player, int i) {
        player.networkHandler.sendPacket(
                new SetPlayerInventoryS2CPacket(
                        i,
                        player.getInventory().getStack(i)
                )
        );
    }
}
