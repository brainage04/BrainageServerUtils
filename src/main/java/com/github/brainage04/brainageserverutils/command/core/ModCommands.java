package com.github.brainage04.brainageserverutils.command.core;

import com.github.brainage04.brainageserverutils.command.ExampleCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ModCommands {
    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            ExampleCommand.initialize(dispatcher);
        });
    }
}
