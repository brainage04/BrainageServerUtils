package io.github.brainage04.brainageserverutils.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.brainage04.brainageserverutils.command.core.PlayerTargets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodData;

public final class HealCommand {
    private static final SimpleCommandExceptionType NO_LIVING_TARGETS =
            new SimpleCommandExceptionType(Component.literal("Only living players can be healed"));

    private HealCommand() {}

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(PlayerTargets.command("heal", HealCommand::execute));
    }

    /// A dead player is skipped: the server ignores the respawn request of a player whose health is above zero
    /// (`ServerGamePacketListenerImpl.handleClientCommand`, 26.3:1959), so healing one on the death screen would
    /// leave them there until they reconnect.
    private static int execute(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        List<ServerPlayer> healed = new ArrayList<>();
        for (ServerPlayer player : targets) {
            if (player.isDeadOrDying()) {
                source.sendFailure(Component.empty().append(player.getDisplayName()).append(" is dead and cannot be healed"));
                continue;
            }
            heal(player);
            healed.add(player);
        }
        if (healed.isEmpty()) {
            throw NO_LIVING_TARGETS.create();
        }
        PlayerTargets.sendSuccess(source, "Healed", healed);
        return healed.size();
    }

    private static void heal(ServerPlayer player) {
        player.setHealth(player.getMaxHealth());
        FoodData food = player.getFoodData();
        food.setFoodLevel(FoodConstants.MAX_FOOD);
        food.setSaturation(FoodConstants.MAX_SATURATION);
        player.clearFire();
        player.setTicksFrozen(0);
        player.setAirSupply(player.getMaxAirSupply());
        List<Holder<MobEffect>> harmful =
                player.getActiveEffects().stream()
                        .map(MobEffectInstance::getEffect)
                        .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL)
                        .toList();
        harmful.forEach(player::removeEffect);
    }
}
