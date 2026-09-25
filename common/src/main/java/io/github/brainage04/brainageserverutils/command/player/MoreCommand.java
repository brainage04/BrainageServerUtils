package io.github.brainage04.brainageserverutils.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.brainage04.brainageserverutils.command.core.PlayerTargets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class MoreCommand {
    private static final SimpleCommandExceptionType NOTHING_HELD =
            new SimpleCommandExceptionType(Component.literal("No target is holding an item"));

    private MoreCommand() {
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(PlayerTargets.command("more", MoreCommand::execute));
    }

    /// Fills each target's main-hand stack to its maximum stack size.
    private static int execute(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        List<ServerPlayer> filled = new ArrayList<>();
        for (ServerPlayer player : targets) {
            ItemStack stack = player.getMainHandItem();
            if (!stack.isEmpty()) {
                stack.setCount(stack.getMaxStackSize());
                filled.add(player);
            }
        }
        if (filled.isEmpty()) {
            throw NOTHING_HELD.create();
        }
        PlayerTargets.sendSuccess(source, "Filled the held stack of", filled);
        return filled.size();
    }
}
