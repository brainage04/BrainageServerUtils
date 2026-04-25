package com.github.brainage04.brainageserverutils.mixin.potion;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.listener.ServerTickListener;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StatusEffectInstance.class)
public class MixinStatusEffectInstance {
    @Redirect(
            method = "updateDuration",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/effect/StatusEffectInstance;mapDuration(Lit/unimi/dsi/fastutil/ints/Int2IntFunction;)I"
            )
    )
    private int updateDuration$injected(StatusEffectInstance instance, Int2IntFunction mapper) {
        MinecraftServer server = ServerTickListener.getServer();
        GameRules rules = server.getGameRules();
        if (rules.getBoolean(ModGameRules.FASTER_EFFECT_DAMAGE_TICKING)) return instance.mapDuration(d -> d - 10);
        else return instance.mapDuration(d -> d - 1);
    }
}
