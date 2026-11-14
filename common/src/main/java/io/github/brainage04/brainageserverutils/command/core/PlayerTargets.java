package io.github.brainage04.brainageserverutils.command.core;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.brainage04.brainageserverutils.util.Translations;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerTargets {
    public static final String ARGUMENT = "targets";

    private PlayerTargets() {}

    @FunctionalInterface
    public interface Action {
        int run(CommandSourceStack source, Collection<ServerPlayer> targets)
                throws CommandSyntaxException;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> command(String name, Action action) {
        return Commands.literal(name)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> action.run(context.getSource(), self(context)))
                .then(
                        Commands.argument(ARGUMENT, EntityArgument.players())
                                .executes(
                                        context ->
                                                action.run(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(
                                                                context, ARGUMENT))));
    }

    public static Collection<ServerPlayer> self(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return List.of(context.getSource().getPlayerOrException());
    }

    /// Reports the affected players: `singleKey` takes the player's name and `multipleKey` the
    /// number of players, each after `args`.
    public static void sendSuccess(
            CommandSourceStack source,
            String singleKey,
            String multipleKey,
            Collection<ServerPlayer> affected,
            Object... args) {
        Object[] withTarget = Arrays.copyOf(args, args.length + 1);
        if (affected.size() == 1) {
            withTarget[args.length] = affected.iterator().next().getDisplayName();
            source.sendSuccess(() -> Translations.text(singleKey, withTarget), true);
        } else {
            withTarget[args.length] = affected.size();
            source.sendSuccess(() -> Translations.text(multipleKey, withTarget), true);
        }
    }
}
