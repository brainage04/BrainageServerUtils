package io.github.brainage04.brainageserverutils.command.setup;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import io.github.brainage04.brainageserverutils.util.Translations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public final class SetupScoreboardCommand {
    private SetupScoreboardCommand() {}

    public static int execute(CommandSourceStack source) {
        source.sendSuccess(
                () -> Translations.text("commands.brainageserverutils.setupscoreboard.started"),
                true);
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        setupObjective(
                scoreboard,
                "Health",
                "scoreboard.brainageserverutils.health",
                ObjectiveCriteria.HEALTH,
                DisplaySlot.BELOW_NAME);
        setupObjective(
                scoreboard,
                "Deaths",
                "scoreboard.brainageserverutils.deaths",
                ObjectiveCriteria.DEATH_COUNT,
                DisplaySlot.LIST);
        setupObjective(
                scoreboard,
                "Kills",
                "scoreboard.brainageserverutils.kills",
                ObjectiveCriteria.KILL_COUNT_PLAYERS,
                DisplaySlot.SIDEBAR);
        source.sendSuccess(
                () -> Translations.text("commands.brainageserverutils.setupscoreboard.success"),
                true);
        return 1;
    }

    private static void setupObjective(
            ServerScoreboard scoreboard,
            String name,
            String displayNameKey,
            ObjectiveCriteria criterion,
            DisplaySlot displaySlot) {
        Objective objective = scoreboard.getObjective(name);
        if (objective == null) {
            objective =
                    scoreboard.addObjective(
                            name,
                            criterion,
                            Translations.text(displayNameKey),
                            ObjectiveCriteria.RenderType.INTEGER,
                            true,
                            null);
        }
        scoreboard.setDisplayObjective(displaySlot, objective);
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("setupscoreboard")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> execute(context.getSource())));
    }
}
