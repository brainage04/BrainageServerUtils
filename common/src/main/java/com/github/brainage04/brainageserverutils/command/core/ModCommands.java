package com.github.brainage04.brainageserverutils.command.core;

import com.github.brainage04.brainageserverutils.command.setup.*;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;


public final class ModCommands {
    private ModCommands() {
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        BrainageGameRuleCommand.initialize(dispatcher);
        GiveFireworksCommand.initialize(dispatcher);
        SetupGamerulesCommand.initialize(dispatcher);
        SetupScoreboardCommand.initialize(dispatcher);
        UpdatePlayerHotbarCommand.initialize(dispatcher);
    }
}
