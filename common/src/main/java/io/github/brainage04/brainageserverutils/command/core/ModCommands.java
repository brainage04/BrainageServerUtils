package io.github.brainage04.brainageserverutils.command.core;

import com.mojang.brigadier.CommandDispatcher;
import io.github.brainage04.brainageserverutils.command.player.FlyCommand;
import io.github.brainage04.brainageserverutils.command.player.HealCommand;
import io.github.brainage04.brainageserverutils.command.player.KitCommand;
import io.github.brainage04.brainageserverutils.command.player.MaxEnchantCommand;
import io.github.brainage04.brainageserverutils.command.player.MoreCommand;
import io.github.brainage04.brainageserverutils.command.setup.SetupGamerulesCommand;
import io.github.brainage04.brainageserverutils.command.setup.SetupScoreboardCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

public final class ModCommands {
    private ModCommands() {
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        FlyCommand.initialize(dispatcher);
        HealCommand.initialize(dispatcher);
        KitCommand.initialize(dispatcher);
        MaxEnchantCommand.initialize(dispatcher, buildContext);
        MoreCommand.initialize(dispatcher);
        SetupGamerulesCommand.initialize(dispatcher);
        SetupScoreboardCommand.initialize(dispatcher);
    }
}
