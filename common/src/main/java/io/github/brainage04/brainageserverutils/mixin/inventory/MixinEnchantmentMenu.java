package io.github.brainage04.brainageserverutils.mixin.inventory;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.gamerule.RuleChecks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentMenu.class)
public abstract class MixinEnchantmentMenu {
    /// The second `hasInfiniteMaterials` check waives the level requirement; the first (lapis) is left alone.
    @Redirect(
            method = "clickMenuButton(Lnet/minecraft/world/entity/player/Player;I)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z", ordinal = 1)
    )
    private boolean clickMenuButton$waiveLevelRequirement(Player player) {
        return player.hasInfiniteMaterials() || RuleChecks.isEnabledFor(player, ModGameRules.FREE_ENCHANTING);
    }
}
