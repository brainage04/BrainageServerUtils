package io.github.brainage04.brainageserverutils.mixin.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrownTrident.class)
public abstract class MixinThrownTrident extends AbstractArrow {
    private MixinThrownTrident(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    /// `ThrownTrident.tryPickup` hands a returning Loyalty trident to its owner whatever its pickup
    /// rule (26.3:172). Vanilla makes a player-owned trident ALLOWED (`AbstractArrow.setOwner`,
    /// 26.3:634), so a DISALLOWED one was preserved by `MixinAbstractArrow` under
    /// `disable_item_decrement` and its owner still holds the thrown trident: returning it would
    /// duplicate it. It vanishes instead, as a creative player's does.
    @Inject(
            method = "tryPickup(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void tryPickup$keepPreservedTridentSingle(
            Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this.pickup == AbstractArrow.Pickup.DISALLOWED
                && this.isNoPhysics()
                && this.ownedBy(player)) {
            cir.setReturnValue(true);
        }
    }
}
