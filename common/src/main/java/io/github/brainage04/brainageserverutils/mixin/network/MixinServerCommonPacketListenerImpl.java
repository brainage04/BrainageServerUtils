package io.github.brainage04.brainageserverutils.mixin.network;

import io.github.brainage04.brainageserverutils.util.ShownAbilities;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/// Every abilities packet, whoever sends it (`/fly`, game mode changes, respawns), passes through here, so a
/// `free_enchanting` display override survives ability changes made while a menu is open.
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class MixinServerCommonPacketListenerImpl {
    @ModifyVariable(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Packet<?> send$showAbilities(Packet<?> packet) {
        if (packet instanceof ClientboundPlayerAbilitiesPacket abilities
                && (Object) this instanceof ServerGamePacketListenerImpl game) {
            return ((ShownAbilities) game.player).brainageserverutils$showAbilities(abilities);
        }
        return packet;
    }
}
