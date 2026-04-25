package com.github.brainage04.brainageserverutils.mixin.item;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class MixinCrossbowItem {
    @Inject(method = "getPullTime", at = @At("HEAD"), cancellable = true)
    private static void getPullTime$injected(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        MinecraftServer server = user.getServer();
        if (server == null) return;

        GameRules rules = server.getGameRules();
        if (rules.getBoolean(ModGameRules.INSTANT_SHOOT)) cir.setReturnValue(1);
    }
}
