package com.github.brainage04.brainageserverutils.mixin.potion;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.listener.ServerTickListener;
import net.minecraft.entity.effect.PoisonStatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PoisonStatusEffect.class)
public abstract class MixinPoisonStatusEffect {
    @Inject(method = "canApplyUpdateEffect", at = @At("HEAD"), cancellable = true)
    private void fasterEffectTicking(int duration, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        if (!ServerTickListener.isGameRuleActive(ModGameRules.FASTER_EFFECT_DAMAGE_TICKING)) cir.cancel();

        int i = (25 >> amplifier) / 10;
        if (i == 0) i = 1;
        cir.setReturnValue(duration % i == 0);
    }
}