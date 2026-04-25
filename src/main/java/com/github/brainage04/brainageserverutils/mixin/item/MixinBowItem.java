package com.github.brainage04.brainageserverutils.mixin.item;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.listener.ServerTickListener;
import net.minecraft.item.BowItem;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class MixinBowItem {
    @Inject(method = "getPullProgress", at = @At("HEAD"), cancellable = true)
    private static void getPullProgress$injected(int useTicks, CallbackInfoReturnable<Float> cir) {
        GameRules rules = ServerTickListener.getServer().getGameRules();
        if (rules.getBoolean(ModGameRules.INSTANT_SHOOT)) cir.setReturnValue(1F);
    }
}
