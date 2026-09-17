package com.github.brainage04.brainageserverutils.command.setup;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRules;

import static net.minecraft.commands.Commands.literal;

public final class SetupGamerulesCommand {
    private SetupGamerulesCommand() {
    }

    public static int execute(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Setting up gamerules..."), true);
        GameRules rules = source.getLevel().getGameRules();
        MinecraftServer server = source.getServer();
        rules.set(GameRules.MOB_GRIEFING, false, server);
        rules.set(GameRules.KEEP_INVENTORY, true, server);
        rules.set(GameRules.MOB_EXPLOSION_DROP_DECAY, false, server);
        rules.set(GameRules.LOCATOR_BAR, false, server);
        rules.set(GameRules.RESPAWN_RADIUS, 0, server);
        rules.set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 1, server);
        source.sendSuccess(() -> Component.literal("Gamerules set up."), true);
        return 1;
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("setupgamerules")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> execute(context.getSource())));
    }
}
