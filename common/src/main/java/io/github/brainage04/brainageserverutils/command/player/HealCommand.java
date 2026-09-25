package io.github.brainage04.brainageserverutils.command.player;

import com.mojang.brigadier.CommandDispatcher;
import io.github.brainage04.brainageserverutils.command.core.PlayerTargets;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodData;

public final class HealCommand {
    private HealCommand() {
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(PlayerTargets.command("heal", HealCommand::execute));
    }

    private static int execute(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            heal(player);
        }
        PlayerTargets.sendSuccess(source, "Healed", targets);
        return targets.size();
    }

    /// Restores health, hunger, saturation and air, puts out fire and thaws, and removes harmful effects.
    private static void heal(ServerPlayer player) {
        player.setHealth(player.getMaxHealth());
        FoodData food = player.getFoodData();
        food.setFoodLevel(FoodConstants.MAX_FOOD);
        food.setSaturation(FoodConstants.MAX_SATURATION);
        player.clearFire();
        player.setTicksFrozen(0);
        player.setAirSupply(player.getMaxAirSupply());
        List<Holder<MobEffect>> harmful = player.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL)
                .toList();
        harmful.forEach(player::removeEffect);
    }
}
