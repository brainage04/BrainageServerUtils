package com.github.brainage04.brainageserverutils.mixin.item;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.listener.ServerTickListener;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConsumableComponent.class)
public class MixinConsumableComponent {
    @Inject(method = "getConsumeTicks", at = @At("HEAD"), cancellable = true)
    private void getConsumeTicks$injected(CallbackInfoReturnable<Integer> cir) {
        MinecraftServer server = ServerTickListener.getServer();
        if (server == null) return;

        GameRules rules = server.getGameRules();
        if (rules.getBoolean(ModGameRules.INSTANT_CONSUME)) cir.setReturnValue(1);
    }
}
