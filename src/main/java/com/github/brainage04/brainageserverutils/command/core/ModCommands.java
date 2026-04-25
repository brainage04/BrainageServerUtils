package com.github.brainage04.brainageserverutils.command.core;

import com.github.brainage04.brainageserverutils.command.setup.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ModCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            BrainageGameRuleCommand.initialize(dispatcher);

            GiveFireworksCommand.initialize(dispatcher);

            SetupGamerulesCommand.initialize(dispatcher);
            SetupScoreboardCommand.initialize(dispatcher);

            UpdatePlayerHotbarCommand.initialize(dispatcher);
        });
    }
}
