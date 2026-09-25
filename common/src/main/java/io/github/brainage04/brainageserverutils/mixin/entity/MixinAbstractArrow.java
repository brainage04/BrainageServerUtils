package io.github.brainage04.brainageserverutils.mixin.entity;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow {
    @Shadow
    public AbstractArrow.Pickup pickup;

    @Inject(method = "setOwner(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void onSetOwner(Entity entity, CallbackInfo ci) {
        if (entity instanceof Player player
                && player.level() instanceof ServerLevel serverLevel
                && serverLevel.getGameRules().get(ModGameRules.DISABLE_ITEM_DECREMENT)) {
            this.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }
}