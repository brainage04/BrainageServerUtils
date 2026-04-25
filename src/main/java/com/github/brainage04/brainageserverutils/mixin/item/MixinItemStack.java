package com.github.brainage04.brainageserverutils.mixin.item;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.util.PlayerUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
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

    @Inject(method = "calculateDamage", at = @At("HEAD"), cancellable = true)
    public void calculateDamage$injected(int baseDamage, ServerWorld world, ServerPlayerEntity player, CallbackInfoReturnable<Integer> cir) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        GameRules rules = server.getGameRules();
        if (rules.getBoolean(ModGameRules.DISABLE_DURABILITY)) cir.setReturnValue(0);
    }

    @Inject(method = "decrementUnlessCreative", at = @At("HEAD"), cancellable = true)
    public void decrementUnlessCreative$injected(int amount, @Nullable LivingEntity entity, CallbackInfo ci) {
        if (this.getItem() instanceof BucketItem) return;
        if (entity == null) return;

        MinecraftServer server = entity.getServer();
        if (server == null) return;

        GameRules rules = server.getGameRules();

        if (rules.getBoolean(ModGameRules.DISABLE_ITEM_DECREMENT)) {
            if (entity instanceof ServerPlayerEntity player) {
                PlayerUtils.updateSelectedSlot(player);
            }

            ci.cancel();
        }
    }
}
