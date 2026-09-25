package io.github.brainage04.brainageserverutils.mixin.item;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.util.CurrentServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Consumable.class)
public class MixinConsumable {
    @Inject(method = "consumeTicks()I", at = @At("HEAD"), cancellable = true)
    private void consumeTicks$injected(CallbackInfoReturnable<Integer> cir) {
        MinecraftServer server = CurrentServer.get();
        if (server != null && server.getGameRules().get(ModGameRules.INSTANT_CONSUME)) {
            cir.setReturnValue(1);
        }
    }
}
