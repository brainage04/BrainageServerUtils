package com.github.brainage04.brainageserverutils.mixin.entity;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.listener.ServerTickListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Inject(method = "damage", at = @At("HEAD"))
    private void alwaysTakeDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        GameRules rules = world.getGameRules();
        if (!rules.getBoolean(ModGameRules.DISABLE_IFRAMES)) return;
        if (!rules.getBoolean(ModGameRules.FASTER_EFFECT_DAMAGE_TICKING)) {
            // todo: test without this
            if (!(source.getAttacker() instanceof LivingEntity)) return;
        }

        LivingEntity self = (LivingEntity) (Object) this;

        self.timeUntilRegen = Math.min(self.timeUntilRegen, 10);
        self.maxHurtTime = 0;
        self.hurtTime = self.maxHurtTime;
    }

    @ModifyConstant(
            method = "tickMovement",
            constant = @Constant(intValue = 40)
    )
    private int modifyFreezeTickInterval(int constant) {
        return ServerTickListener.isGameRuleActive(ModGameRules.FASTER_EFFECT_DAMAGE_TICKING)
                ? constant / 10
                : constant;
    }

    @Redirect(
            method = "tickMovement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;setFrozenTicks(I)V"
            )
    )
    private void decrementFreezeTicksFaster(LivingEntity instance, int i) {
        Entity entity = (Entity) (Object) this;

        MinecraftServer server = instance.getServer();
        if (server == null) return;

        if (server.getGameRules().getBoolean(ModGameRules.FASTER_EFFECT_DAMAGE_TICKING)) {
            entity.setFrozenTicks(Math.max(0, entity.getFrozenTicks() - 20));
        } else entity.setFrozenTicks(Math.max(0, entity.getFrozenTicks() - 2));
        ;
    }
}