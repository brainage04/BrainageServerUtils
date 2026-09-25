package io.github.brainage04.brainageserverutils.mixin.item;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.util.PlayerUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow
    public abstract Item getItem();

    @Inject(
            method = "processDurabilityChange(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void processDurabilityChange$injected(
            int baseDamage,
            ServerLevel level,
            ServerPlayer player,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (level.getGameRules().get(ModGameRules.DISABLE_DURABILITY)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(
            method = "consume(ILnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void consume$injected(int amount, LivingEntity entity, CallbackInfo ci) {
        if (this.getItem() instanceof BucketItem) {
            return;
        }

        if (!(entity.level() instanceof ServerLevel level)
                || !level.getGameRules().get(ModGameRules.DISABLE_ITEM_DECREMENT)) {
            return;
        }

        if (entity instanceof ServerPlayer player) {
            PlayerUtils.updateSelectedSlot(player);
        }

        ci.cancel();
    }
}
