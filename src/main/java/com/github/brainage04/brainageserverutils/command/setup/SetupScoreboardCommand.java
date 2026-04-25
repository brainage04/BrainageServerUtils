package com.github.brainage04.brainageserverutils.command.setup;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class SetupScoreboardCommand {
    public static int execute(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Setting up scoreboard..."), true);

        Scoreboard scoreboard = source.getServer().getScoreboard();

        ScoreboardObjective health = scoreboard.getNullableObjective("Health");
        if (health == null) {
            scoreboard.addObjective(
                    "Health",
                    ScoreboardCriterion.HEALTH,
                    Text.literal("Health"),
                    ScoreboardCriterion.RenderType.INTEGER,
                    true,
                    null
            );
        } else {
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.LIST, health);
        }

        ScoreboardObjective deaths = scoreboard.getNullableObjective("Deaths");
        if (deaths == null) {
             scoreboard.addObjective(
                     "Deaths",
                     ScoreboardCriterion.DEATH_COUNT,
                     Text.literal("Deaths"),
                     ScoreboardCriterion.RenderType.INTEGER,
                     true,
                     null
            );
        } else {
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, deaths);
        }

        ScoreboardObjective kills = scoreboard.getNullableObjective("Kills");
        if (kills == null) {
            scoreboard.addObjective(
                    "Kills",
                    ScoreboardCriterion.PLAYER_KILL_COUNT,
                    Text.literal("Kills"),
                    ScoreboardCriterion.RenderType.INTEGER,
                    true,
                    null
            );
        } else {
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, kills);
        }

        source.sendFeedback(() -> Text.literal("Scoreboard set up."), true);

        return 1;
    }

    public static void initialize(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("setupscoreboard")
                .executes(context ->
                        execute(
                                context.getSource()
                        )
                )
        );
    }
}