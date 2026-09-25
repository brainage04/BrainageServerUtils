package io.github.brainage04.brainageserverutils.util;

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;

/// Implemented by `ServerPlayer` through a mixin: rewrites outgoing abilities packets into what the client should see.
public interface ShownAbilities {
    ClientboundPlayerAbilitiesPacket brainageserverutils$showAbilities(ClientboundPlayerAbilitiesPacket packet);
}
