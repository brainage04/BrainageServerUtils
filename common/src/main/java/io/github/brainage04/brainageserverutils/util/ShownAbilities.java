package io.github.brainage04.brainageserverutils.util;

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;

/// Implemented by `ServerPlayer` through a mixin.
public interface ShownAbilities {
    ClientboundPlayerAbilitiesPacket brainageserverutils$showAbilities(ClientboundPlayerAbilitiesPacket packet);
}
