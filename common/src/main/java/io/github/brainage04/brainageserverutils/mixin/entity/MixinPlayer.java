package io.github.brainage04.brainageserverutils.mixin.entity;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.gamerule.RuleChecks;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class MixinPlayer {
    /// Enchanting-table use still rerolls the enchantment seed but costs no levels.
    @ModifyVariable(
            method = "onEnchantmentPerformed(Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private int onEnchantmentPerformed$waiveCost(int enchantmentCost) {
        return RuleChecks.isEnabledFor((Player) (Object) this, ModGameRules.FREE_ENCHANTING) ? 0 : enchantmentCost;
    }
}
