package io.github.brainage04.brainageserverutils.enchantment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/// Replaces an item's enchantments with every applicable non-curse enchantment at its maximum level.
///
/// Mutually exclusive enchantments are resolved greedily in this order: explicitly preferred enchantments (in the
/// order given), then configured rankings (most preferred first), then everything else by ID.
public final class MaxEnchantment {
    private MaxEnchantment() {
    }

    /// An enchantment left off because it conflicts with one that was applied. `decided` is false when neither a
    /// ranking nor an explicit preference chose between the two, so the pick fell back to ID order.
    public record Conflict(Holder.Reference<Enchantment> skipped, Holder.Reference<Enchantment> chosen, boolean decided) {
    }

    /// `inapplicable` lists explicitly preferred enchantments the item does not support.
    public record Result(
            List<Holder.Reference<Enchantment>> applied,
            List<Conflict> conflicts,
            List<Holder.Reference<Enchantment>> inapplicable
    ) {
    }

    private record Rank(int ranking, int position) {
    }

    /// Leaves the stack untouched when no enchantment applies to it.
    public static Result apply(
            ItemStack stack,
            HolderLookup.RegistryLookup<Enchantment> enchantments,
            List<List<Identifier>> rankings,
            List<Holder.Reference<Enchantment>> preferred
    ) {
        Map<Identifier, Rank> ranks = new HashMap<>();
        for (int ranking = 0; ranking < rankings.size(); ranking++) {
            List<Identifier> entries = rankings.get(ranking);
            for (int position = 0; position < entries.size(); position++) {
                ranks.putIfAbsent(entries.get(position), new Rank(ranking, position));
            }
        }
        List<Identifier> preferredIds = preferred.stream().map(holder -> holder.key().identifier()).toList();

        Comparator<Holder.Reference<Enchantment>> order = Comparator
                .comparingInt((Holder.Reference<Enchantment> holder) -> tier(holder, preferredIds, ranks))
                .thenComparingInt(holder -> position(holder, preferredIds, ranks))
                .thenComparingInt(holder -> ranking(holder, ranks))
                .thenComparing(holder -> holder.key().identifier().toString());

        List<Holder.Reference<Enchantment>> candidates = enchantments.listElements()
                .filter(holder -> holder.value().canEnchant(stack))
                .filter(holder -> !holder.is(EnchantmentTags.CURSE) || preferredIds.contains(holder.key().identifier()))
                .sorted(order)
                .toList();

        List<Holder.Reference<Enchantment>> applied = new ArrayList<>();
        List<Conflict> conflicts = new ArrayList<>();
        for (Holder.Reference<Enchantment> candidate : candidates) {
            Holder.Reference<Enchantment> blocker = null;
            for (Holder.Reference<Enchantment> chosen : applied) {
                if (!Enchantment.areCompatible(candidate, chosen)) {
                    blocker = chosen;
                    break;
                }
            }
            if (blocker == null) {
                applied.add(candidate);
            } else {
                conflicts.add(new Conflict(candidate, blocker, decided(candidate, blocker, preferredIds, ranks)));
            }
        }

        if (!applied.isEmpty()) {
            ItemEnchantments.Mutable result = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (Holder<Enchantment> enchantment : applied) {
                result.set(enchantment, enchantment.value().getMaxLevel());
            }
            EnchantmentHelper.setEnchantments(stack, result.toImmutable());
        }

        List<Holder.Reference<Enchantment>> inapplicable = preferred.stream()
                .filter(holder -> !holder.value().canEnchant(stack))
                .toList();
        return new Result(List.copyOf(applied), List.copyOf(conflicts), inapplicable);
    }

    private static int tier(Holder.Reference<Enchantment> holder, List<Identifier> preferredIds, Map<Identifier, Rank> ranks) {
        Identifier id = holder.key().identifier();
        if (preferredIds.contains(id)) {
            return 0;
        }
        return ranks.containsKey(id) ? 1 : 2;
    }

    private static int position(Holder.Reference<Enchantment> holder, List<Identifier> preferredIds, Map<Identifier, Rank> ranks) {
        Identifier id = holder.key().identifier();
        int preferredIndex = preferredIds.indexOf(id);
        if (preferredIndex >= 0) {
            return preferredIndex;
        }
        Rank rank = ranks.get(id);
        return rank == null ? 0 : rank.position();
    }

    private static int ranking(Holder.Reference<Enchantment> holder, Map<Identifier, Rank> ranks) {
        Rank rank = ranks.get(holder.key().identifier());
        return rank == null ? 0 : rank.ranking();
    }

    private static boolean decided(
            Holder.Reference<Enchantment> skipped,
            Holder.Reference<Enchantment> chosen,
            List<Identifier> preferredIds,
            Map<Identifier, Rank> ranks
    ) {
        if (preferredIds.contains(chosen.key().identifier())) {
            return true;
        }
        Rank skippedRank = ranks.get(skipped.key().identifier());
        Rank chosenRank = ranks.get(chosen.key().identifier());
        return skippedRank != null && chosenRank != null && skippedRank.ranking() == chosenRank.ranking();
    }
}
