package com.github.brainage04.brainageserverutils.mixin.entity;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class MixinPersistentProjectileEntity {
    @Shadow
    public PersistentProjectileEntity.PickupPermission pickupType;

    @Inject(method = "setOwner(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
    private void onSetOwner(Entity entity, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player) {
            MinecraftServer server = player.getServer();
            if (server == null) return;

            if (server.getGameRules().getBoolean(ModGameRules.DISABLE_ITEM_DECREMENT)) {
                this.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
            }
        }
    }
}