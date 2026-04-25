package com.github.brainage04.brainageserverutils.command.setup;

import com.github.brainage04.brainageserverutils.util.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class UpdatePlayerHotbarCommand {
    public static int execute(ServerCommandSource source) {
        PlayerUtils.updateHotbar(source.getPlayer());

        source.sendFeedback(() -> Text.literal("Updated player hotbar."), false);

        return 1;
    }

    public static void initialize(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("updateplayerhotbar")
                .executes(context ->
                        execute(
                                context.getSource()
                        )
                )
        );
    }
}