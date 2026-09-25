package io.github.brainage04.brainageserverutils.command.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/// Shared shape for game-master commands that act on `[targets]`, defaulting to the executing player.
public final class PlayerTargets {
    public static final String ARGUMENT = "targets";

    private PlayerTargets() {
    }

    @FunctionalInterface
    public interface Action {
        int run(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> command(String name, Action action) {
        return Commands.literal(name)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> action.run(context.getSource(), self(context)))
                .then(Commands.argument(ARGUMENT, EntityArgument.players())
                        .executes(context -> action.run(context.getSource(), EntityArgument.getPlayers(context, ARGUMENT))));
    }

    public static Collection<ServerPlayer> self(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return List.of(context.getSource().getPlayerOrException());
    }

    /// Reports "<verb> <player>" for one target or "<verb> <n> players" for several.
    public static void sendSuccess(CommandSourceStack source, String verb, Collection<ServerPlayer> affected) {
        if (affected.size() == 1) {
            ServerPlayer player = affected.iterator().next();
            source.sendSuccess(() -> Component.literal(verb + " ").append(player.getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.literal(verb + " " + affected.size() + " players"), true);
        }
    }
}
