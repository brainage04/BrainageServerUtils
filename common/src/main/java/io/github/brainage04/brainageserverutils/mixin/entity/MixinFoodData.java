package io.github.brainage04.brainageserverutils.mixin.entity;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.gamerule.RuleChecks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class MixinFoodData {
    @Shadow
    private int foodLevel;

    @Shadow
    private float exhaustionLevel;

    /// Keeps hunger full and discards exhaustion before it can drain saturation.
    @Inject(method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"))
    private void tick$injected(ServerPlayer player, CallbackInfo ci) {
        if (RuleChecks.isEnabledFor(player, ModGameRules.DISABLE_HUNGER)) {
            this.foodLevel = FoodConstants.MAX_FOOD;
            this.exhaustionLevel = 0.0F;
        }
    }
}
