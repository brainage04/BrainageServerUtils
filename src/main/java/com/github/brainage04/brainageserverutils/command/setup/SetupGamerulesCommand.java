package com.github.brainage04.brainageserverutils.command.setup;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

import static net.minecraft.server.command.CommandManager.literal;

public class SetupGamerulesCommand {
    public static int execute(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Setting up gamerules..."), true);

        GameRules rules = source.getWorld().getGameRules();
        MinecraftServer server = source.getServer();

        rules.get(GameRules.DO_MOB_GRIEFING).set(false, server);
        rules.get(GameRules.KEEP_INVENTORY).set(true, server);
        rules.get(GameRules.MOB_EXPLOSION_DROP_DECAY).set(false, server);
        rules.get(GameRules.LOCATOR_BAR).set(false, server);

        rules.get(GameRules.SPAWN_RADIUS).set(0, server);
        rules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(1, server);

        source.sendFeedback(() -> Text.literal("Gamerules set up."), true);

        return 1;
    }

    public static void initialize(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("setupgamerules")
                .executes(context ->
                        execute(
                                context.getSource()
                        )
                )
        );
    }
}