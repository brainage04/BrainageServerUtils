package com.github.brainage04.brainageserverutils.mixin.item;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.util.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemUsage.class)
public class MixinItemUsage {
    @Redirect(
            method = "exchangeStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isInCreativeMode()Z"
            )
    )
    private static boolean exchangeStack$injected(
            PlayerEntity instance
    ) {
        MinecraftServer server = instance.getServer();
        if (server == null) return instance.isInCreativeMode();

        GameRules gameRules = server.getGameRules();
        if (gameRules.getBoolean(ModGameRules.DISABLE_BUCKET_DECREMENT)) {
            if (instance instanceof ServerPlayerEntity player) {
                PlayerUtils.updateHotbar(player);
            }

            return true;
        } else return instance.isInCreativeMode();
    }
}
