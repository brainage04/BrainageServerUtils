package io.github.brainage04.brainageserverutils.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.brainage04.brainageserverutils.command.core.PlayerTargets;
import io.github.brainage04.brainageserverutils.config.EnchantmentRankings;
import io.github.brainage04.brainageserverutils.enchantment.MaxEnchantment;
import io.github.brainage04.brainageserverutils.util.Translations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class MaxEnchantCommand {
    private static final int MAX_PREFERENCES = 8;
    private static final SimpleCommandExceptionType NOTHING_ENCHANTED =
            new SimpleCommandExceptionType(
                    Translations.text("commands.brainageserverutils.maxenchant.failed"));

    private MaxEnchantCommand() {}

    public static void initialize(
            CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        RequiredArgumentBuilder<CommandSourceStack, ?> preferences = null;
        for (int count = MAX_PREFERENCES; count >= 1; count--) {
            int preferenceCount = count;
            RequiredArgumentBuilder<CommandSourceStack, ?> preference =
                    Commands.argument(
                                    preferenceArgument(count),
                                    ResourceArgument.resource(buildContext, Registries.ENCHANTMENT))
                            .executes(
                                    context ->
                                            execute(
                                                    context.getSource(),
                                                    EntityArgument.getPlayers(
                                                            context, PlayerTargets.ARGUMENT),
                                                    preferences(context, preferenceCount)));
            if (preferences != null) {
                preference.then(preferences);
            }
            preferences = preference;
        }

        dispatcher.register(
                PlayerTargets.command(
                                "maxenchant",
                                (source, targets) -> execute(source, targets, List.of()))
                        .then(
                                Commands.argument(PlayerTargets.ARGUMENT, EntityArgument.players())
                                        .then(preferences)));
    }

    private static String preferenceArgument(int index) {
        return "preference" + index;
    }

    private static List<Holder.Reference<Enchantment>> preferences(
            CommandContext<CommandSourceStack> context, int count) throws CommandSyntaxException {
        List<Holder.Reference<Enchantment>> preferences = new ArrayList<>(count);
        for (int index = 1; index <= count; index++) {
            preferences.add(ResourceArgument.getEnchantment(context, preferenceArgument(index)));
        }
        return preferences;
    }

    private static int execute(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            List<Holder.Reference<Enchantment>> preferred)
            throws CommandSyntaxException {
        HolderLookup.RegistryLookup<Enchantment> enchantments =
                source.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<List<Identifier>> rankings = loadRankings(enchantments);
        int enchanted = 0;
        for (ServerPlayer player : targets) {
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) {
                source.sendFailure(
                        Translations.text(
                                "commands.brainageserverutils.maxenchant.not_holding",
                                player.getDisplayName()));
                continue;
            }
            MaxEnchantment.Result result =
                    MaxEnchantment.apply(stack, enchantments, rankings, preferred);
            for (Holder.Reference<Enchantment> inapplicable : result.inapplicable()) {
                source.sendFailure(
                        Translations.text(
                                "commands.brainageserverutils.maxenchant.inapplicable",
                                inapplicable.value().description(),
                                stack.getDisplayName()));
            }
            if (result.applied().isEmpty()) {
                source.sendFailure(
                        Translations.text(
                                "commands.brainageserverutils.maxenchant.cannot_enchant",
                                stack.getDisplayName()));
                continue;
            }
            enchanted++;
            source.sendSuccess(() -> describeApplied(player, stack, result), true);
            for (MaxEnchantment.Conflict conflict : result.conflicts()) {
                if (!conflict.decided()) {
                    source.sendSuccess(() -> describeUndecided(conflict), false);
                }
            }
        }
        if (enchanted == 0) {
            throw NOTHING_ENCHANTED.create();
        }
        return enchanted;
    }

    static List<List<Identifier>> loadRankings(
            HolderLookup.RegistryLookup<Enchantment> enchantments) throws CommandSyntaxException {
        List<List<Identifier>> rankings;
        try {
            rankings = EnchantmentRankings.load();
        } catch (EnchantmentRankings.InvalidConfigException exception) {
            throw new SimpleCommandExceptionType(exception.problem()).create();
        }
        for (List<Identifier> ranking : rankings) {
            for (Identifier id : ranking) {
                if (enchantments.get(ResourceKey.create(Registries.ENCHANTMENT, id)).isEmpty()) {
                    throw new SimpleCommandExceptionType(
                                    Translations.text(
                                            "commands.brainageserverutils.rankings.unknown_enchantment",
                                            EnchantmentRankings.FILE_NAME,
                                            id.toString()))
                            .create();
                }
            }
        }
        return rankings;
    }

    private static Component describeApplied(
            ServerPlayer player, ItemStack stack, MaxEnchantment.Result result) {
        return Translations.text(
                "commands.brainageserverutils.maxenchant.success",
                stack.getDisplayName(),
                player.getDisplayName(),
                Translations.joined(
                        result.applied().stream()
                                .map(
                                        enchantment ->
                                                Enchantment.getFullname(
                                                        enchantment,
                                                        enchantment.value().getMaxLevel()))
                                .toList()));
    }

    private static Component describeUndecided(MaxEnchantment.Conflict conflict) {
        return Translations.text(
                "commands.brainageserverutils.maxenchant.undecided",
                conflict.chosen().value().description(),
                conflict.skipped().value().description(),
                conflict.skipped().key().identifier().toString(),
                EnchantmentRankings.FILE_NAME);
    }
}
