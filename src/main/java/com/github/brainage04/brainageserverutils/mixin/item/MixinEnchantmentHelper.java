package com.github.brainage04.brainageserverutils.mixin.item;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentHelper {
    @Inject(method = "getAmmoUse", at = @At("HEAD"), cancellable = true)
    private static void getAmmoUse(ServerWorld world, ItemStack rangedWeaponStack, ItemStack projectileStack, int baseAmmoUse, CallbackInfoReturnable<Integer> cir) {
        GameRules rules = world.getGameRules();

        if (rules.getBoolean(ModGameRules.DISABLE_ITEM_DECREMENT)) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
}
