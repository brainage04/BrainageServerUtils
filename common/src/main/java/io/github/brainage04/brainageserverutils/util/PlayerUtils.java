package io.github.brainage04.brainageserverutils.util;

import net.minecraft.server.level.ServerPlayer;

public final class PlayerUtils {
    private PlayerUtils() {
    }

    public static void updateHotbar(ServerPlayer player) {
        for (int i = 0; i < 9; i++) {
            updateHotbarSlot(player, i);
        }
    }

    public static void updateSelectedSlot(ServerPlayer player) {
        updateHotbarSlot(player, player.getInventory().getSelectedSlot());
    }

    public static void updateHotbarSlot(ServerPlayer player, int slot) {
        player.connection.send(player.getInventory().createInventoryUpdatePacket(slot));
    }
}
