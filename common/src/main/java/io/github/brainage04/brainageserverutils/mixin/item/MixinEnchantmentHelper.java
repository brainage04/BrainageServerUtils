package io.github.brainage04.brainageserverutils.mixin.item;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentHelper {
    @Inject(
            method = "processAmmoUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void processAmmoUse(
            ServerLevel level,
            ItemStack rangedWeaponStack,
            ItemStack projectileStack,
            int baseAmmoUse,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (level.getGameRules().get(ModGameRules.DISABLE_ITEM_DECREMENT)) {
            cir.setReturnValue(0);
        }
    }

    /// Every durability loss (`ItemStack.hurtAndBreak` and `hurtWithoutBreaking`) funnels through this helper on
    /// both loaders. `ItemStack.processDurabilityChange` itself cannot be targeted: NeoForge re-routes it through a
    /// `LivingEntity` overload that vanilla does not have.
    @Inject(
            method = "processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void processDurabilityChange(
            ServerLevel level,
            ItemStack stack,
            int baseDamage,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (level.getGameRules().get(ModGameRules.DISABLE_DURABILITY)) {
            cir.setReturnValue(0);
        }
    }
}
