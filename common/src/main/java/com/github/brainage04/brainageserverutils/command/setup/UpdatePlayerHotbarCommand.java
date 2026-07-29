package com.github.brainage04.brainageserverutils.command.setup;

import com.github.brainage04.brainageserverutils.util.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public final class UpdatePlayerHotbarCommand {
    private UpdatePlayerHotbarCommand() {
    }

    public static int execute(CommandSourceStack source) throws CommandSyntaxException {
        PlayerUtils.updateHotbar(source.getPlayerOrException());
        source.sendSuccess(() -> Component.literal("Updated player hotbar."), false);
        return 1;
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("updateplayerhotbar").executes(context -> execute(context.getSource())));
    }
}
