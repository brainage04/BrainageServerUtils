package com.github.brainage04.brainageserverutils.mixin.item;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.listener.ServerTickListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Consumable.class)
public class MixinConsumableComponent {
    @Inject(method = "consumeTicks()I", at = @At("HEAD"), cancellable = true)
    private void consumeTicks$injected(CallbackInfoReturnable<Integer> cir) {
        MinecraftServer server = ServerTickListener.getServer();
        if (server != null && server.getGameRules().get(ModGameRules.INSTANT_CONSUME)) {
            cir.setReturnValue(1);
        }
    }
}
