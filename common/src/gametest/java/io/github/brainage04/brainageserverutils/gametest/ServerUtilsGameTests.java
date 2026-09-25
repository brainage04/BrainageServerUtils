package io.github.brainage04.brainageserverutils.gametest;

import io.github.brainage04.brainageserverutils.config.EnchantmentRankings;
import io.github.brainage04.brainageserverutils.enchantment.MaxEnchantment;
import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

/// GameTest bodies shared by the Fabric and NeoForge GameTest registrations.
public final class ServerUtilsGameTests {
    private static final List<String> COMMANDS =
            List.of("fly", "heal", "kit", "maxenchant", "more", "setupgamerules", "setupscoreboard");

    private ServerUtilsGameTests() {
    }

    public static void customRulesAndCommandsAreRegistered(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        assertEquals(8, ModGameRules.REGISTERED.size(), "Expected eight ServerUtils gamerules");
        for (GameRule<Boolean> rule : ModGameRules.REGISTERED) {
            assertTrue(level.getGameRules().availableRules().anyMatch(available -> available == rule), "Expected gamerule to be registered");
        }
        for (String command : COMMANDS) {
            assertTrue(level.getServer().getCommands().getDispatcher().getRoot().getChild(command) != null,
                    "Expected /" + command + " to be registered");
        }
        helper.succeed();
    }

    public static void itemAndConsumptionRulesChangeRuntimeBehavior(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Zombie user = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        assertTrue(user != null, "Expected to create a zombie");
        ItemStack apples = new ItemStack(Items.APPLE, 3);
        Consumable consumable = apples.get(DataComponents.CONSUMABLE);
        assertTrue(consumable != null, "Expected apples to have a consumable component");
        try {
            setRule(helper, ModGameRules.DISABLE_ITEM_DECREMENT, false);
            apples.consume(1, user);
            assertEquals(2, apples.getCount(), "Expected vanilla item consumption");
            setRule(helper, ModGameRules.DISABLE_ITEM_DECREMENT, true);
            apples.consume(1, user);
            assertEquals(2, apples.getCount(), "Expected disabled item decrement to preserve the stack");

            setRule(helper, ModGameRules.INSTANT_CONSUME, false);
            assertTrue(consumable.consumeTicks() > 1, "Expected vanilla consumption duration");
            setRule(helper, ModGameRules.INSTANT_CONSUME, true);
            assertEquals(1, consumable.consumeTicks(), "Expected one-tick consumption");
        } finally {
            resetRules(helper);
        }
        helper.succeed();
    }

    public static void maxEnchantResolvesConflictsByPreference(GameTestHelper helper) {
        HolderLookup.RegistryLookup<Enchantment> enchantments = enchantments(helper);
        List<List<Identifier>> rankings = EnchantmentRankings.defaults();

        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(enchantment(helper, Enchantments.SMITE), 1);
        MaxEnchantment.Result ranked = MaxEnchantment.apply(sword, enchantments, rankings, List.of());
        assertEquals(5, level(helper, Enchantments.SHARPNESS, sword), "Expected the ranking to pick Sharpness V");
        assertEquals(0, level(helper, Enchantments.SMITE, sword), "Expected existing enchantments to be replaced");
        assertTrue(ranked.conflicts().stream().allMatch(MaxEnchantment.Conflict::decided),
                "Expected every sword conflict to be decided by a ranking");

        ItemStack preferredSword = new ItemStack(Items.DIAMOND_SWORD);
        MaxEnchantment.apply(preferredSword, enchantments, rankings, List.of(enchantment(helper, Enchantments.SMITE)));
        assertEquals(5, level(helper, Enchantments.SMITE, preferredSword), "Expected an explicit preference to beat the ranking");
        assertEquals(0, level(helper, Enchantments.SHARPNESS, preferredSword), "Expected Sharpness to be dropped for Smite");

        ItemStack bow = new ItemStack(Items.BOW);
        MaxEnchantment.apply(bow, enchantments, rankings, List.of());
        assertEquals(1, level(helper, Enchantments.INFINITY, bow), "Expected Infinity to be ranked above Mending");
        assertEquals(0, level(helper, Enchantments.MENDING, bow), "Expected Mending to be dropped for Infinity");

        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        List<List<Identifier>> withoutCrossbowRanking = rankings.stream()
                .filter(ranking -> !ranking.contains(Enchantments.MULTISHOT.identifier()))
                .toList();
        MaxEnchantment.Result unranked = MaxEnchantment.apply(crossbow, enchantments, withoutCrossbowRanking, List.of());
        assertTrue(unranked.conflicts().stream().anyMatch(conflict -> !conflict.decided()),
                "Expected an unranked Multishot/Piercing conflict to be reported as undecided");

        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        MaxEnchantment.apply(helmet, enchantments, rankings, List.of());
        assertEquals(0, level(helper, Enchantments.BINDING_CURSE, helmet), "Expected curses to be left off");

        ItemStack apple = new ItemStack(Items.APPLE);
        MaxEnchantment.Result nothing = MaxEnchantment.apply(apple, enchantments, rankings, List.of());
        assertTrue(nothing.applied().isEmpty() && apple.getEnchantments().isEmpty(), "Expected apples to stay unenchanted");
        helper.succeed();
    }

    public static void hungerAndCooldownRulesAffectPlayers(GameTestHelper helper) {
        ServerPlayer player = survivalPlayer(helper);
        try {
            player.getFoodData().setFoodLevel(3);
            setRule(helper, ModGameRules.DISABLE_HUNGER, false);
            player.getFoodData().tick(player);
            assertEquals(3, player.getFoodData().getFoodLevel(), "Expected hunger to stay low without the rule");
            setRule(helper, ModGameRules.DISABLE_HUNGER, true);
            player.getFoodData().tick(player);
            assertEquals(20, player.getFoodData().getFoodLevel(), "Expected disable_hunger to refill hunger");

            ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
            setRule(helper, ModGameRules.DISABLE_ITEM_COOLDOWNS, true);
            player.getCooldowns().addCooldown(pearl, 20);
            assertTrue(!player.getCooldowns().isOnCooldown(pearl), "Expected disable_item_cooldowns to drop the cooldown");

            ItemStack shield = new ItemStack(Items.SHIELD);
            BlocksAttacks blocksAttacks = shield.get(DataComponents.BLOCKS_ATTACKS);
            assertTrue(blocksAttacks != null, "Expected shields to block attacks");
            blocksAttacks.disable(player.level(), player, 5.0F, shield);
            assertTrue(!player.getCooldowns().isOnCooldown(shield), "Expected shields not to be disabled");

            setRule(helper, ModGameRules.DISABLE_ITEM_COOLDOWNS, false);
            player.getCooldowns().addCooldown(pearl, 20);
            assertTrue(player.getCooldowns().isOnCooldown(pearl), "Expected vanilla cooldowns without the rule");
            blocksAttacks.disable(player.level(), player, 5.0F, shield);
            assertTrue(player.getCooldowns().isOnCooldown(shield), "Expected vanilla shield disabling without the rule");
        } finally {
            resetRules(helper);
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    public static void enchantingRulesWaiveLevelCostsAndCap(GameTestHelper helper) {
        ServerPlayer player = survivalPlayer(helper);
        try {
            EnchantmentMenu table = new EnchantmentMenu(0, player.getInventory());
            table.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
            table.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 3));
            table.costs[0] = 30;
            player.setExperienceLevels(0);
            assertTrue(!table.clickMenuButton(player, 0), "Expected the level requirement without free_enchanting");
            setRule(helper, ModGameRules.FREE_ENCHANTING, true);
            assertTrue(table.clickMenuButton(player, 0), "Expected free_enchanting to waive the level requirement");

            player.setExperienceLevels(10);
            player.onEnchantmentPerformed(ItemStack.EMPTY, 3);
            assertEquals(10, player.experienceLevel, "Expected free_enchanting to keep levels");

            AnvilMenu anvil = new AnvilMenu(0, player.getInventory());
            ItemStack worn = new ItemStack(Items.DIAMOND_SWORD);
            worn.set(DataComponents.REPAIR_COST, 50);
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            book.enchant(enchantment(helper, Enchantments.SHARPNESS), 1);
            anvil.getSlot(0).set(worn);
            anvil.getSlot(1).set(book);

            anvil.createResult();
            assertTrue(anvil.getSlot(2).getItem().isEmpty(), "Expected vanilla Too Expensive without the rule");
            setRule(helper, ModGameRules.DISABLE_TOO_EXPENSIVE, true);
            anvil.createResult();
            assertTrue(!anvil.getSlot(2).getItem().isEmpty(), "Expected disable_too_expensive to produce a result");
            assertEquals(39, anvil.getCost(), "Expected the cost to be capped at 39 levels");

            player.setExperienceLevels(0);
            setRule(helper, ModGameRules.FREE_ENCHANTING, false);
            assertTrue(!anvil.getSlot(2).mayPickup(player), "Expected the anvil to require levels without free_enchanting");
            setRule(helper, ModGameRules.FREE_ENCHANTING, true);
            assertTrue(anvil.getSlot(2).mayPickup(player), "Expected free_enchanting to waive the anvil level requirement");
            player.setExperienceLevels(45);
            anvil.getSlot(2).onTake(player, anvil.getSlot(2).getItem());
            assertEquals(45, player.experienceLevel, "Expected free_enchanting to keep levels at the anvil");
        } finally {
            resetRules(helper);
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    public static void playerCommandsAffectTheirTargets(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerPlayer player = survivalPlayer(helper);
        CommandSourceStack source = server.createCommandSourceStack().withEntity(player).withSuppressedOutput();
        try {
            player.getInventory().clearContent();
            run(server, source, "kit netherite");
            ItemStack sword = findItem(player, Items.NETHERITE_SWORD);
            assertEquals(5, level(helper, Enchantments.SHARPNESS, sword), "Expected the kit sword to have Sharpness V");
            assertTrue(sword.has(DataComponents.UNBREAKABLE), "Expected kit gear to be unbreakable");

            player.getInventory().clearContent();
            player.getInventory().setSelectedSlot(0);
            player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
            run(server, source, "maxenchant @s minecraft:smite");
            assertEquals(5, level(helper, Enchantments.SMITE, player.getMainHandItem()), "Expected /maxenchant to honour the preference");

            player.getInventory().setItem(0, new ItemStack(Items.APPLE));
            run(server, source, "more");
            assertEquals(64, player.getMainHandItem().getCount(), "Expected /more to fill the held stack");

            player.setHealth(1.0F);
            player.setRemainingFireTicks(100);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
            player.getFoodData().setFoodLevel(1);
            run(server, source, "heal");
            assertTrue(player.getHealth() == player.getMaxHealth(), "Expected /heal to restore health");
            assertTrue(!player.hasEffect(MobEffects.POISON) && !player.isOnFire(), "Expected /heal to clear poison and fire");
            assertEquals(20, player.getFoodData().getFoodLevel(), "Expected /heal to restore hunger");

            run(server, source, "setupgamerules sandbox");
            assertTrue(ModGameRules.REGISTERED.stream().allMatch(rule -> server.getGameRules().get(rule)),
                    "Expected the sandbox preset to enable every mod rule");
            assertTrue(!server.getGameRules().get(GameRules.FALL_DAMAGE), "Expected the sandbox preset to disable fall damage");
            run(server, source, "setupgamerules survival");
            assertTrue(ModGameRules.REGISTERED.stream().noneMatch(rule -> server.getGameRules().get(rule)),
                    "Expected the survival preset to disable every mod rule");
            assertTrue(server.getGameRules().get(GameRules.FALL_DAMAGE), "Expected the survival preset to restore fall damage");
        } finally {
            resetRules(helper);
            server.getPlayerList().remove(player);
        }
        helper.succeed();
    }

    /// Mock players report creative mode regardless; survival abilities are still needed for the level checks.
    /// `/fly` is covered by the Fabric client GameTest instead, since it skips creative players.
    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }

    private static void run(MinecraftServer server, CommandSourceStack source, String command) {
        server.getCommands().performPrefixedCommand(source, command);
    }

    private static ItemStack findItem(ServerPlayer player, net.minecraft.world.item.Item item) {
        for (ItemStack stack : player.getInventory()) {
            if (stack.is(item)) {
                return stack;
            }
        }
        throw new AssertionError("Expected " + item + " in the inventory");
    }

    private static HolderLookup.RegistryLookup<Enchantment> enchantments(GameTestHelper helper) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    }

    private static Holder.Reference<Enchantment> enchantment(GameTestHelper helper, ResourceKey<Enchantment> key) {
        return enchantments(helper).getOrThrow(key);
    }

    private static int level(GameTestHelper helper, ResourceKey<Enchantment> key, ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment(helper, key), stack);
    }

    private static void setRule(GameTestHelper helper, GameRule<Boolean> rule, boolean value) {
        MinecraftServer server = helper.getLevel().getServer();
        server.getGameRules().set(rule, value, server);
    }

    private static void resetRules(GameTestHelper helper) {
        for (GameRule<Boolean> rule : ModGameRules.REGISTERED) {
            setRule(helper, rule, false);
        }
        MinecraftServer server = helper.getLevel().getServer();
        server.getGameRules().set(GameRules.FALL_DAMAGE, true, server);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + ", found " + actual);
        }
    }
}
