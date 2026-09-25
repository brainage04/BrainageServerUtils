package io.github.brainage04.brainageserverutils.command.setup;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

/// `/setupgamerules <survival|sandbox>`. Both presets set every rule they touch, so switching between them is
/// lossless.
public final class SetupGamerulesCommand {
    private static final List<GameRule<Boolean>> ENVIRONMENTAL_DAMAGE =
            List.of(GameRules.FALL_DAMAGE, GameRules.FIRE_DAMAGE, GameRules.DROWNING_DAMAGE, GameRules.FREEZE_DAMAGE);

    private enum Preset {
        /// The server's usual rules, with vanilla damage and every BrainageServerUtils rule off.
        SURVIVAL(false),
        /// SURVIVAL plus every BrainageServerUtils rule on and no fall, fire, drowning or freezing damage.
        SANDBOX(true);

        private final boolean sandbox;

        Preset(boolean sandbox) {
            this.sandbox = sandbox;
        }

        String commandName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private SetupGamerulesCommand() {
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("setupgamerules")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (Preset preset : Preset.values()) {
            root.then(Commands.literal(preset.commandName()).executes(context -> execute(context.getSource(), preset)));
        }
        dispatcher.register(root);
    }

    private static int execute(CommandSourceStack source, Preset preset) {
        MinecraftServer server = source.getServer();
        GameRules rules = server.getGameRules();
        rules.set(GameRules.MOB_GRIEFING, false, server);
        rules.set(GameRules.KEEP_INVENTORY, true, server);
        rules.set(GameRules.MOB_EXPLOSION_DROP_DECAY, false, server);
        rules.set(GameRules.LOCATOR_BAR, false, server);
        rules.set(GameRules.RESPAWN_RADIUS, 0, server);
        rules.set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 1, server);
        for (GameRule<Boolean> rule : ENVIRONMENTAL_DAMAGE) {
            rules.set(rule, !preset.sandbox, server);
        }
        for (GameRule<Boolean> rule : ModGameRules.REGISTERED) {
            rules.set(rule, preset.sandbox, server);
        }
        source.sendSuccess(() -> Component.literal("Applied the " + preset.commandName() + " gamerule preset"), true);
        return 1;
    }
}
