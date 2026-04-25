package com.github.brainage04.brainageserverutils.mixin.entity;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.listener.ServerTickListener;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public abstract void setFireTicks(int fireTicks);

    @Shadow
    private int fireTicks;

    @ModifyConstant(
            method = "baseTick",
            constant = @Constant(intValue = 20)
    )
    private int modifyFireTickInterval(int constant) {
        return ServerTickListener.isGameRuleActive(ModGameRules.FASTER_EFFECT_DAMAGE_TICKING)
                ? constant / 10
                : constant;
    }

    @Redirect(
            method = "baseTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;setFireTicks(I)V"
            )
    )
    private void decrementFireTicksFaster(Entity instance, int fireTicks) {
        MinecraftServer server = instance.getServer();
        if (server == null) return;

        if (server.getGameRules().getBoolean(ModGameRules.FASTER_EFFECT_DAMAGE_TICKING)) {
            this.setFireTicks(this.fireTicks - 10);
        } else this.setFireTicks(this.fireTicks - 1);
    }
}
