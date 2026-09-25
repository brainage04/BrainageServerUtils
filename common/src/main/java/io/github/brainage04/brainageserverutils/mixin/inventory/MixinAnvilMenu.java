package io.github.brainage04.brainageserverutils.mixin.inventory;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.gamerule.RuleChecks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class MixinAnvilMenu extends ItemCombinerMenu {
    @Shadow
    @Final
    private DataSlot cost;

    private MixinAnvilMenu(
            MenuType<?> menuType,
            int containerId,
            Inventory inventory,
            ContainerLevelAccess access,
            ItemCombinerMenuSlotDefinition itemInputSlots
    ) {
        super(menuType, containerId, inventory, access, itemInputSlots);
    }

    @Inject(method = "mayPickup(Lnet/minecraft/world/entity/player/Player;Z)Z", at = @At("HEAD"), cancellable = true)
    private void mayPickup$waiveLevelRequirement(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        if (RuleChecks.isEnabledFor(player, ModGameRules.FREE_ENCHANTING)) {
            cir.setReturnValue(this.cost.get() > 0);
        }
    }

    @Redirect(
            method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V")
    )
    private void onTake$waiveCost(Player player, int levels) {
        if (!RuleChecks.isEnabledFor(player, ModGameRules.FREE_ENCHANTING)) {
            player.giveExperienceLevels(levels);
        }
    }

    /// The result is computed in `createResult` on Fabric and in `createResultInternal` on NeoForge. Only the
    /// "Too Expensive" check runs with a cost of 40 or more; the earlier creative-mode compatibility check (vanilla
    /// only) runs while the cost is still 1. Capping the cost at 39 keeps unmodified clients' display consistent.
    /// Stacked inputs are left alone: vanilla prices those at 40 purely to forbid them.
    @Redirect(
            method = {"createResult()V", "createResultInternal()V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"),
            require = 1
    )
    private boolean createResult$capCost(Player player) {
        if (this.cost.get() >= ModGameRules.TOO_EXPENSIVE_ANVIL_COST
                && this.inputSlots.getItem(0).getCount() == 1
                && RuleChecks.isEnabledFor(player, ModGameRules.DISABLE_TOO_EXPENSIVE)) {
            this.cost.set(ModGameRules.TOO_EXPENSIVE_ANVIL_COST - 1);
            return true;
        }
        return player.hasInfiniteMaterials();
    }
}
