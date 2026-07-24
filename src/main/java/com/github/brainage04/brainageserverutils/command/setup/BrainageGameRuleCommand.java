package com.github.brainage04.brainageserverutils.command.setup;

import com.github.brainage04.brainageserverutils.BrainageServerUtils;
import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.util.FeedbackUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.Map;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class BrainageGameRuleCommand {
    private static final Map<String, GameRule<Boolean>> RULES = Map.of(
            "disableDurability", ModGameRules.DISABLE_DURABILITY,
            "disableItemDecrement", ModGameRules.DISABLE_ITEM_DECREMENT,
            "disableBucketDecrement", ModGameRules.DISABLE_BUCKET_DECREMENT,
            "instantConsume", ModGameRules.INSTANT_CONSUME
    );

    private BrainageGameRuleCommand() {
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = literal("brainagegamerule")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        RULES.forEach((name, rule) -> addRuleSubcommand(root, name, rule));
        root.then(literal("all")
                .executes(context -> executeMassQuery(context.getSource()))
                .then(argument("value", BoolArgumentType.bool())
                        .executes(BrainageGameRuleCommand::executeMassSet)));
        dispatcher.register(root);
    }

    private static void sendQueryTitle(CommandSourceStack source) {
        FeedbackUtils.sendFeedback(source, "%s gamerules:".formatted(BrainageServerUtils.MOD_NAME));
    }

    private static void executeQuery(CommandSourceStack source, String name, GameRule<Boolean> rule, GameRules rules) {
        FeedbackUtils.sendFeedback(source, " - %s: %s".formatted(name, rules.getAsString(rule)));
    }

    private static int executeMassQuery(CommandSourceStack source) {
        GameRules rules = source.getServer().getGameRules();
        sendQueryTitle(source);
        RULES.forEach((name, rule) -> executeQuery(source, name, rule, rules));
        return 1;
    }

    private static int executeMassSet(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        boolean value = BoolArgumentType.getBool(context, "value");
        sendQueryTitle(source);
        RULES.forEach((name, rule) -> server.getGameRules().set(rule, value, server));
        return 1;
    }

    private static void addRuleSubcommand(LiteralArgumentBuilder<CommandSourceStack> root, String name, GameRule<Boolean> rule) {
        root.then(literal(name)
                .executes(context -> {
                    executeQuery(context.getSource(), name, rule, context.getSource().getServer().getGameRules());
                    return 1;
                })
                .then(argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            source.getServer().getGameRules().set(rule, BoolArgumentType.getBool(context, "value"), source.getServer());
                            return 1;
                        })));
    }
}
