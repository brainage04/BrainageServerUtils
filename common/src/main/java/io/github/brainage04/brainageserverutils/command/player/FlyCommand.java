package io.github.brainage04.brainageserverutils.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.brainage04.brainageserverutils.command.core.PlayerTargets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;

/// Grants or revokes survival/adventure flight. Vanilla resets it whenever a player's game mode changes.
public final class FlyCommand {
    private static final SimpleCommandExceptionType NO_SURVIVAL_TARGETS = new SimpleCommandExceptionType(
            Component.literal("Flight can only be toggled for players in survival or adventure mode"));

    private FlyCommand() {
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(PlayerTargets.command("fly", (source, targets) -> execute(source, targets, null))
                .then(Commands.argument(PlayerTargets.ARGUMENT, EntityArgument.players())
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> execute(
                                        context.getSource(),
                                        EntityArgument.getPlayers(context, PlayerTargets.ARGUMENT),
                                        BoolArgumentType.getBool(context, "enabled")
                                )))));
    }

    /// `enabled == null` toggles each target individually.
    private static int execute(CommandSourceStack source, Collection<ServerPlayer> targets, Boolean enabled)
            throws CommandSyntaxException {
        List<ServerPlayer> enabledFor = new ArrayList<>();
        List<ServerPlayer> disabledFor = new ArrayList<>();
        for (ServerPlayer player : targets) {
            if (player.isCreative() || player.isSpectator()) {
                continue;
            }
            Abilities abilities = player.getAbilities();
            boolean mayFly = enabled == null ? !abilities.mayfly : enabled;
            abilities.mayfly = mayFly;
            if (!mayFly) {
                abilities.flying = false;
            }
            player.onUpdateAbilities();
            (mayFly ? enabledFor : disabledFor).add(player);
        }
        if (enabledFor.isEmpty() && disabledFor.isEmpty()) {
            throw NO_SURVIVAL_TARGETS.create();
        }
        if (!enabledFor.isEmpty()) {
            PlayerTargets.sendSuccess(source, "Enabled flight for", enabledFor);
        }
        if (!disabledFor.isEmpty()) {
            PlayerTargets.sendSuccess(source, "Disabled flight for", disabledFor);
        }
        return enabledFor.size() + disabledFor.size();
    }
}
