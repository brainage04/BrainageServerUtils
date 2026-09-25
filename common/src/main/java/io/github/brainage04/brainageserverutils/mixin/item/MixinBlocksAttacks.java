package io.github.brainage04.brainageserverutils.mixin.item;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.gamerule.RuleChecks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlocksAttacks.class)
public abstract class MixinBlocksAttacks {
    /// Axes and other disabling attacks no longer lower a player's shield or put it on cooldown.
    @Inject(
            method = "disable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disable$injected(ServerLevel level, LivingEntity user, float baseSeconds, ItemStack blockingWith, CallbackInfo ci) {
        if (user instanceof Player player && RuleChecks.isEnabledFor(player, ModGameRules.DISABLE_ITEM_COOLDOWNS)) {
            ci.cancel();
        }
    }
}
