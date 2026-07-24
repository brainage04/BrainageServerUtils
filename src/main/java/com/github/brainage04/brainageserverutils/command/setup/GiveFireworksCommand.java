package com.github.brainage04.brainageserverutils.command.setup;

import com.mojang.brigadier.CommandDispatcher;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class GiveFireworksCommand {
    private GiveFireworksCommand() {
    }

    public static int execute(CommandSourceStack source, Collection<ServerPlayer> targets) {
        DataComponentMap components = DataComponentMap.builder()
                .set(DataComponents.FIREWORKS, new Fireworks(3, List.of(new FireworkExplosion(
                        FireworkExplosion.Shape.LARGE_BALL, IntList.of(ARGB.color(255, 0, 0)),
                        IntList.of(ARGB.color(255, 0, 0)), false, false))))
                .build();
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET, 64);
        stack.applyComponents(components);
        for (ServerPlayer player : targets) {
            player.getInventory().placeItemBackInInventory(stack.copy());
        }
        source.sendSuccess(() -> Component.literal("Gave fireworks."), false);
        return 1;
    }

    public static void initialize(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("givefireworks").then(argument("targets", EntityArgument.players())
                .executes(context -> execute(context.getSource(), EntityArgument.getPlayers(context, "targets")))));
    }
}
