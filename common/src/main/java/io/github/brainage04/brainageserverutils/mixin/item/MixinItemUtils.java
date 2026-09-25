package io.github.brainage04.brainageserverutils.mixin.item;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.util.PlayerUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemUtils.class)
public class MixinItemUtils {
    @Redirect(
            method = "createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"
            )
    )
    private static boolean createFilledResult$injected(Player player) {
        if (!(player.level() instanceof ServerLevel level)
                || !level.getGameRules().get(ModGameRules.DISABLE_BUCKET_DECREMENT)) {
            return player.hasInfiniteMaterials();
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PlayerUtils.updateHotbar(serverPlayer);
        }

        return true;
    }
}
