package io.github.brainage04.brainageserverutils.command.setup;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import static net.minecraft.commands.Commands.literal;

public final class SetupScoreboardCommand {
    private SetupScoreboardCommand() {
    }

    public static int execute(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Setting up scoreboard..."), true);
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        setupObjective(scoreboard, "Health", ObjectiveCriteria.HEALTH, DisplaySlot.BELOW_NAME);
        setupObjective(scoreboard, "Deaths", ObjectiveCriteria.DEATH_COUNT, DisplaySlot.LIST);
        setupObjective(scoreboard, "Kills", ObjectiveCriteria.KILL_COUNT_PLAYERS, DisplaySlot.SIDEBAR);
        source.sendSuccess(() -> Component.literal("Scoreboard set up."), true);
        return 1;
    }

    private static void setupObjective(ServerScoreboard scoreboard, String name, ObjectiveCriteria criterion, DisplaySlot displaySlot) {
        Objective objective = scoreboard.getObjective(name);
        if (objective == null) {
            objective = scoreboard.addObjective(
                    name,
                    criterion,
                    Component.literal(name),
                    ObjectiveCriteria.RenderType.INTEGER,
                    true,
                    null
            );
        }
        scoreboard.setDisplayObjective(displaySlot, objective);
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("setupscoreboard")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> execute(context.getSource())));
    }
}
