package com.github.brainage04.brainageserverutils.command.setup;

import com.mojang.brigadier.CommandDispatcher;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GiveFireworksCommand {
    public static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        ComponentMap fireworkComponents = ComponentMap.builder()
                .add(DataComponentTypes.FIREWORKS, new FireworksComponent(
                        3,
                        List.of(
                                new FireworkExplosionComponent(
                                        FireworkExplosionComponent.Type.LARGE_BALL,
                                        IntList.of(Colors.RED),
                                        IntList.of(Colors.RED),
                                        false,
                                        false
                                )
                        )
                ))
                .build();

        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET, 64);
        stack.applyComponentsFrom(fireworkComponents);

        for (ServerPlayerEntity player : targets) {
            player.giveOrDropStack(stack);
        }

        source.sendFeedback(() -> Text.literal("Gave fireworks."), false);

        return 1;
    }

    public static void initialize(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("givefireworks")
                        .then(argument("targets", EntityArgumentType.players())
                                .executes(context ->
                                        execute(
                                                context.getSource(),
                                                EntityArgumentType.getPlayers(context, "targets")
                                        )
                                )
                        )
        );
    }
}