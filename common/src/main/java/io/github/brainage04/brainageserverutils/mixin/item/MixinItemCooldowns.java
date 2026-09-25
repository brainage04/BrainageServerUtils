package io.github.brainage04.brainageserverutils.mixin.item;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.gamerule.RuleChecks;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ServerItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCooldowns.class)
public abstract class MixinItemCooldowns {
    /// Drops every server-side cooldown. Clients start ender pearl, wind charge and similar cooldowns themselves when
    /// using the item, so a zero-length cooldown is sent back to clear that prediction.
    @Inject(method = "addCooldown(Lnet/minecraft/resources/Identifier;I)V", at = @At("HEAD"), cancellable = true)
    private void addCooldown$injected(Identifier cooldownGroup, int time, CallbackInfo ci) {
        if (!((Object) this instanceof ServerItemCooldowns)) {
            return;
        }
        ServerPlayer player = ((ServerItemCooldownsAccessor) this).getPlayer();
        if (RuleChecks.isEnabledFor(player, ModGameRules.DISABLE_ITEM_COOLDOWNS)) {
            player.connection.send(new ClientboundCooldownPacket(cooldownGroup, 0));
            ci.cancel();
        }
    }
}
