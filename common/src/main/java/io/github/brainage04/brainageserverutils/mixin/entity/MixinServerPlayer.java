package io.github.brainage04.brainageserverutils.mixin.entity;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.gamerule.RuleChecks;
import io.github.brainage04.brainageserverutils.util.ShownAbilities;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Unmodified clients refuse to click an enchanting-table option, and render anvil costs as unaffordable, unless the
/// player has enough levels or creative-style infinite materials. While `free_enchanting` is on, the client is told
/// the player has infinite materials for as long as an enchanting table, or an anvil whose result is obtainable, is
/// open. The server's own abilities never change.
///
/// An anvil costing 40 or more is not obtainable (unless `disable_too_expensive` caps it), and the client only
/// shows "Too Expensive!" to players without infinite materials, so the override is dropped while the cost is that
/// high. Outgoing abilities packets are rewritten in `MixinServerCommonPacketListenerImpl`; this class tracks what
/// the client was last told and resends when the wanted state changes (menu opened or closed, cost or rule changed).
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer implements ShownAbilities {
    @Unique
    private boolean brainageserverutils$clientInstabuild;

    @Inject(method = "initMenu(Lnet/minecraft/world/inventory/AbstractContainerMenu;)V", at = @At("TAIL"))
    private void initMenu$syncShownAbilities(AbstractContainerMenu menu, CallbackInfo ci) {
        this.brainageserverutils$syncShownAbilities();
    }

    @Inject(method = "doCloseContainer()V", at = @At("TAIL"))
    private void doCloseContainer$syncShownAbilities(CallbackInfo ci) {
        this.brainageserverutils$syncShownAbilities();
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tick$syncShownAbilities(CallbackInfo ci) {
        this.brainageserverutils$syncShownAbilities();
    }

    @Override
    public ClientboundPlayerAbilitiesPacket brainageserverutils$showAbilities(ClientboundPlayerAbilitiesPacket packet) {
        boolean instabuild = this.brainageserverutils$shownInstabuild();
        this.brainageserverutils$clientInstabuild = instabuild;
        if (packet.canInstabuild() == instabuild) {
            return packet;
        }
        Abilities shown = new Abilities();
        shown.invulnerable = packet.isInvulnerable();
        shown.flying = packet.isFlying();
        shown.mayfly = packet.canFly();
        shown.instabuild = instabuild;
        shown.setFlyingSpeed(packet.getFlyingSpeed());
        shown.setWalkingSpeed(packet.getWalkingSpeed());
        return new ClientboundPlayerAbilitiesPacket(shown);
    }

    @Unique
    private void brainageserverutils$syncShownAbilities() {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.connection != null
                && this.brainageserverutils$clientInstabuild != this.brainageserverutils$shownInstabuild()) {
            player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
        }
    }

    @Unique
    private boolean brainageserverutils$shownInstabuild() {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.getAbilities().instabuild) {
            return true;
        }
        if (!RuleChecks.isEnabledFor(player, ModGameRules.FREE_ENCHANTING)) {
            return false;
        }
        AbstractContainerMenu menu = player.containerMenu;
        return menu instanceof EnchantmentMenu
                || menu instanceof AnvilMenu anvil && anvil.getCost() < ModGameRules.TOO_EXPENSIVE_ANVIL_COST;
    }
}
