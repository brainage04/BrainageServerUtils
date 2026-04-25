package com.github.brainage04.brainageserverutils.command.setup;

import com.github.brainage04.brainageserverutils.BrainageServerUtils;
import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.util.FeedbackUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.GameRuleCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameRules;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

// modified version of GameRuleCommand
// only includes gamerules registered with this mod
public class BrainageGameRuleCommand {
    public static void initialize(CommandDispatcher<ServerCommandSource> dispatcher) {
        final LiteralArgumentBuilder<ServerCommandSource> root =
                literal("brainagegamerule").requires(CommandManager.requirePermissionLevel(2));

        for (GameRules.Key<?> key : ModGameRules.REGISTERED) {
            addRuleSubcommand(root, key);
        }

        // add mass set/query commands
        root.then(literal("all")
                .executes(ctx -> executeMassQuery(ctx.getSource()))
                .then(argument("value", BoolArgumentType.bool())
                        .executes(BrainageGameRuleCommand::executeMassSet)
                )
        );

        dispatcher.register(root);
    }

    private static void sendQueryTitle(ServerCommandSource source) {
        FeedbackUtils.sendFeedback(source, "%s gamerules:".formatted(BrainageServerUtils.MOD_NAME));
    }

    private static void executeQuery(ServerCommandSource source, GameRules.Key<?> key, GameRules rules) {
        GameRules.Rule<?> rule = rules.get(key);

        String feedback = " - %s: %s".formatted(key.getName(), rule.toString());

        FeedbackUtils.sendFeedback(source, feedback);
    }

    private static int executeMassQuery(ServerCommandSource source) {
        MinecraftServer server = source.getServer();
        GameRules rules = server.getGameRules();

        sendQueryTitle(source);

        for (GameRules.Key<?> key : ModGameRules.REGISTERED) {
            executeQuery(source, key, rules);
        }

        return 1;
    }

    private static int executeMassSet(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        MinecraftServer server = source.getServer();
        GameRules rules = server.getGameRules();

        sendQueryTitle(source);

        for (GameRules.Key<?> key : ModGameRules.REGISTERED) {
            GameRules.Rule<?> rule = rules.get(key);
            if (!(rule instanceof GameRules.BooleanRule)) continue;

            GameRuleCommand.executeSet(ctx, key);
        }

        return 1;
    }

    private static <T extends GameRules.Rule<T>> void addRuleSubcommand(
            LiteralArgumentBuilder<ServerCommandSource> root,
            GameRules.Key<T> key
    ) {
        GameRules.Type<T> type = GameRules.getRuleType(key);
        LiteralArgumentBuilder<ServerCommandSource> literal = literal(key.getName());

        root.then(literal
                .executes(ctx -> GameRuleCommand.executeQuery(ctx.getSource(), key))
                .then(type.argument("value")
                        .executes(ctx -> GameRuleCommand.executeSet(ctx, key))
                )
        );
    }
}