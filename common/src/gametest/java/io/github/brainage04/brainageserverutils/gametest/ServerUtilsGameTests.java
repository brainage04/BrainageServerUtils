package io.github.brainage04.brainageserverutils.gametest;

import com.mojang.authlib.GameProfile;
import io.github.brainage04.brainagelib.gametest.GameTestPlayer;
import io.github.brainage04.brainageserverutils.config.EnchantmentRankings;
import io.github.brainage04.brainageserverutils.enchantment.MaxEnchantment;
import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.util.ShownAbilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/// GameTest bodies shared by the Fabric and NeoForge GameTest registrations.
public final class ServerUtilsGameTests {
    private static final Map<ServerPlayer, GameTestPlayer> JOINED_PLAYERS = new IdentityHashMap<>();

    private ServerUtilsGameTests() {
    }

    public static void runIsolated(GameTestHelper helper, Consumer<GameTestHelper> test) {
        MinecraftServer server = helper.getLevel().getServer();
        GameRules original = server.getGameRules().copy(helper.getLevel().enabledFeatures());
        try {
            test.accept(helper);
        } finally {
            server.getGameRules().setAll(original, server);
        }
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

    /// Uses a zombie rather than a mock player: mock players report creative mode, which skips durability loss anyway.
    public static void durabilityRulePreventsItemDamage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Zombie user = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        assertTrue(user != null, "Expected to create a zombie");
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        ItemStack chestplate = new ItemStack(Items.IRON_CHESTPLATE);
        user.setItemSlot(EquipmentSlot.MAINHAND, pickaxe);
        user.setItemSlot(EquipmentSlot.CHEST, chestplate);
        try {
            setRule(helper, ModGameRules.DISABLE_DURABILITY, true);
            pickaxe.hurtAndBreak(5, user, EquipmentSlot.MAINHAND);
            chestplate.hurtAndBreak(5, user, EquipmentSlot.CHEST);
            assertEquals(0, pickaxe.getDamageValue(), "Expected disable_durability to preserve the held tool");
            assertEquals(0, chestplate.getDamageValue(), "Expected disable_durability to preserve the worn armour");

            setRule(helper, ModGameRules.DISABLE_DURABILITY, false);
            pickaxe.hurtAndBreak(5, user, EquipmentSlot.MAINHAND);
            chestplate.hurtAndBreak(5, user, EquipmentSlot.CHEST);
            assertEquals(5, pickaxe.getDamageValue(), "Expected vanilla durability loss on the held tool");
            assertEquals(5, chestplate.getDamageValue(), "Expected vanilla durability loss on the worn armour");
        } finally {
            resetRules(helper);
        }
        helper.succeed();
    }

    public static void bucketRulePreservesSourceWithoutDuplicatingResults(GameTestHelper helper) {
        ServerPlayer player = survivalPlayer(helper);
        try {
            assertTrue(!player.hasInfiniteMaterials(), "Expected survival bucket consumption");
            for (boolean preserveItems : List.of(false, true)) {
                setRule(helper, ModGameRules.DISABLE_ITEM_DECREMENT, preserveItems);
                setRule(helper, ModGameRules.DISABLE_BUCKET_DECREMENT, false);
                player.getInventory().clearContent();
                ItemStack source = new ItemStack(Items.BUCKET);
                ItemStack result = ItemUtils.createFilledResult(source, player, new ItemStack(Items.WATER_BUCKET));
                assertTrue(result.is(Items.WATER_BUCKET) && source.isEmpty(),
                        "Expected only the bucket rule, not the item rule, to preserve a bucket");

                setRule(helper, ModGameRules.DISABLE_BUCKET_DECREMENT, true);
                source = new ItemStack(Items.BUCKET, 2);
                result = ItemUtils.createFilledResult(source, player, new ItemStack(Items.WATER_BUCKET));
                assertTrue(result == source, "Expected to keep holding the source buckets");
                assertEquals(2, source.getCount(), "Expected source bucket count to be preserved");
                assertEquals(1, player.getInventory().countItem(Items.WATER_BUCKET), "Expected a filled bucket");
                ItemUtils.createFilledResult(source, player, new ItemStack(Items.WATER_BUCKET));
                assertEquals(1, player.getInventory().countItem(Items.WATER_BUCKET),
                        "Expected repeated filling not to duplicate a filled bucket already in inventory");
            }
        } finally {
            resetRules(helper);
            removePlayer(player);
        }
        helper.succeed();
    }

    public static void preservedAmmunitionCannotBePickedUp(GameTestHelper helper) {
        ServerPlayer player = survivalPlayer(helper);
        try {
            player.getInventory().clearContent();
            player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ARROW, 3));
            setRule(helper, ModGameRules.DISABLE_ITEM_DECREMENT, true);
            Arrow preserved = shootArrow(helper, player);
            assertEquals(3, player.getOffhandItem().getCount(), "Expected firing not to consume ammunition");
            setRule(helper, ModGameRules.DISABLE_ITEM_DECREMENT, false);
            preserved.setNoPhysics(true);
            preserved.playerTouch(player);
            assertTrue(!preserved.isRemoved(), "Expected the preserved arrow not to be picked up after disabling the rule");
            assertEquals(3, player.getOffhandItem().getCount(), "Expected no duplicated ammunition");
            preserved.discard();

            Arrow ordinary = shootArrow(helper, player);
            assertEquals(2, player.getOffhandItem().getCount(), "Expected vanilla ammunition consumption");
            ordinary.setNoPhysics(true);
            ordinary.playerTouch(player);
            assertTrue(ordinary.isRemoved(), "Expected vanilla arrow pickup");
            assertEquals(3, player.getOffhandItem().getCount(), "Expected the fired arrow to return to the stack");
        } finally {
            helper.getLevel().getEntitiesOfClass(Arrow.class, player.getBoundingBox().inflate(4),
                    arrow -> arrow.getOwner() == player).forEach(Arrow::discard);
            resetRules(helper);
            removePlayer(player);
        }
        helper.succeed();
    }

    private static Arrow shootArrow(GameTestHelper helper, ServerPlayer player) {
        ItemStack bow = player.getMainHandItem();
        assertTrue(bow.getItem().releaseUsing(bow, helper.getLevel(), player, bow.getUseDuration(player) - 20),
                "Expected a fully drawn bow to fire");
        List<Arrow> arrows = helper.getLevel().getEntitiesOfClass(Arrow.class, player.getBoundingBox().inflate(4),
                arrow -> arrow.getOwner() == player && !arrow.isRemoved());
        assertEquals(1, arrows.size(), "Expected one fired arrow");
        return arrows.getFirst();
    }

    public static void scoreboardTracksHealthDeathsAndPlayerKills(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerScoreboard scoreboard = server.getScoreboard();
        List<Objective> originalObjectives = List.copyOf(scoreboard.getObjectives());
        Objective belowName = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
        Objective list = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        Objective sidebar = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        ServerPlayer killer = survivalPlayer(helper);
        ServerPlayer victim = survivalPlayer(helper);
        try {
            setRule(helper, GameRules.PVP, true);
            run(server, server.createCommandSourceStack().withSuppressedOutput(), "setupscoreboard");
            Objective health = scoreboard.getObjective("Health");
            Objective deaths = scoreboard.getObjective("Deaths");
            Objective kills = scoreboard.getObjective("Kills");
            assertTrue(health != null && health.getCriteria() == ObjectiveCriteria.HEALTH
                            && scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME) == health,
                    "Expected health below player names");
            assertTrue(deaths != null && deaths.getCriteria() == ObjectiveCriteria.DEATH_COUNT
                            && scoreboard.getDisplayObjective(DisplaySlot.LIST) == deaths,
                    "Expected deaths in the player list");
            assertTrue(kills != null && kills.getCriteria() == ObjectiveCriteria.KILL_COUNT_PLAYERS
                            && scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) == kills,
                    "Expected player kills in the sidebar");
            killer.setHealth(7.0F);
            killer.doTick();
            assertEquals(7, scoreboard.getPlayerScoreInfo(killer, health).value(), "Expected health to update from the player");
            assertTrue(victim.hurtServer(helper.getLevel(), victim.damageSources().playerAttack(killer), 1000.0F),
                    "Expected lethal player damage");
            assertEquals(1, scoreboard.getPlayerScoreInfo(victim, deaths).value(), "Expected a death to increment Deaths");
            assertEquals(1, scoreboard.getPlayerScoreInfo(killer, kills).value(), "Expected a player kill to increment Kills");
            run(server, server.createCommandSourceStack().withSuppressedOutput(), "setupscoreboard");
            assertTrue(scoreboard.getObjective("Kills") == kills, "Expected repeated setup to reuse the objective");
            assertEquals(1, scoreboard.getPlayerScoreInfo(killer, kills).value(), "Expected repeated setup to retain scores");
        } finally {
            scoreboard.resetAllPlayerScores(killer);
            scoreboard.resetAllPlayerScores(victim);
            for (Objective objective : List.copyOf(scoreboard.getObjectives())) {
                if (!originalObjectives.contains(objective)) {
                    scoreboard.removeObjective(objective);
                }
            }
            scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, belowName);
            scoreboard.setDisplayObjective(DisplaySlot.LIST, list);
            scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, sidebar);
            removePlayer(killer);
            removePlayer(victim);
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
            player.getFoodData().setSaturation(5.0F);
            player.getFoodData().addExhaustion(10.0F);
            player.getFoodData().tick(player);
            assertEquals(20, player.getFoodData().getFoodLevel(), "Expected disable_hunger to refill hunger");
            assertTrue(player.getFoodData().getSaturationLevel() == 5.0F, "Expected hunger disabling to preserve saturation");

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
            removePlayer(player);
        }
        helper.succeed();
    }

    public static void enchantingRulesWaiveLevelCostsAndCap(GameTestHelper helper) {
        ServerPlayer player = survivalPlayer(helper);
        try {
            EnchantmentMenu table = new EnchantmentMenu(0, player.getInventory(),
                    net.minecraft.world.inventory.ContainerLevelAccess.create(helper.getLevel(),
                            helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1))));
            table.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
            table.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 3));
            table.costs[0] = 30;
            player.setExperienceLevels(0);
            assertTrue(!table.clickMenuButton(player, 0), "Expected the level requirement without free_enchanting");
            setRule(helper, ModGameRules.FREE_ENCHANTING, true);
            assertTrue(table.clickMenuButton(player, 0), "Expected free_enchanting to waive the level requirement");
            assertEquals(2, table.getSlot(1).getItem().getCount(), "Expected free enchanting to consume lapis");

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
            removePlayer(player);
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
            run(server, source, "kit ranged");
            assertTrue(findItem(player, Items.BOW).has(DataComponents.UNBREAKABLE), "Expected an unbreakable ranged weapon");
            assertEquals(64, findItem(player, Items.ARROW).getCount(), "Expected a full stack of arrows");
            player.getInventory().clearContent();
            run(server, source, "kit elytra");
            assertTrue(findItem(player, Items.ELYTRA).has(DataComponents.UNBREAKABLE), "Expected unbreakable elytra");
            var fireworks = findItem(player, Items.FIREWORK_ROCKET).get(DataComponents.FIREWORKS);
            assertTrue(fireworks != null && fireworks.flightDuration() == 3 && fireworks.explosions().isEmpty(),
                    "Expected flight-duration-3 rockets without explosions");

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
            player.getFoodData().setSaturation(0.0F);
            player.setAirSupply(1);
            player.setTicksFrozen(100);
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 200));
            run(server, source, "heal");
            assertTrue(player.getHealth() == player.getMaxHealth(), "Expected /heal to restore health");
            assertTrue(!player.hasEffect(MobEffects.POISON) && !player.isOnFire(), "Expected /heal to clear poison and fire");
            assertEquals(20, player.getFoodData().getFoodLevel(), "Expected /heal to restore hunger");
            assertTrue(player.getFoodData().getSaturationLevel() == 20.0F && player.getAirSupply() == player.getMaxAirSupply()
                            && player.getTicksFrozen() == 0 && player.hasEffect(MobEffects.SPEED),
                    "Expected /heal to restore saturation and air, thaw and preserve beneficial effects");

            run(server, source, "setupgamerules sandbox");
            assertTrue(ModGameRules.REGISTERED.stream().allMatch(rule -> server.getGameRules().get(rule)),
                    "Expected the sandbox preset to enable every mod rule");
            assertTrue(!server.getGameRules().get(GameRules.FALL_DAMAGE), "Expected the sandbox preset to disable fall damage");
            assertTrue(List.of(GameRules.FIRE_DAMAGE, GameRules.DROWNING_DAMAGE, GameRules.FREEZE_DAMAGE).stream()
                    .noneMatch(rule -> server.getGameRules().get(rule)), "Expected all sandbox environmental damage disabled");
            assertTrue(!server.getGameRules().get(GameRules.MOB_GRIEFING)
                            && server.getGameRules().get(GameRules.KEEP_INVENTORY)
                            && !server.getGameRules().get(GameRules.MOB_EXPLOSION_DROP_DECAY)
                            && !server.getGameRules().get(GameRules.LOCATOR_BAR)
                            && server.getGameRules().get(GameRules.RESPAWN_RADIUS) == 0
                            && server.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE) == 1,
                    "Expected the common vanilla preset settings");
            run(server, source, "setupgamerules survival");
            assertTrue(ModGameRules.REGISTERED.stream().noneMatch(rule -> server.getGameRules().get(rule)),
                    "Expected the survival preset to disable every mod rule");
            assertTrue(server.getGameRules().get(GameRules.FALL_DAMAGE), "Expected the survival preset to restore fall damage");
            assertTrue(List.of(GameRules.FIRE_DAMAGE, GameRules.DROWNING_DAMAGE, GameRules.FREEZE_DAMAGE).stream()
                    .allMatch(rule -> server.getGameRules().get(rule)), "Expected all survival environmental damage restored");
        } finally {
            resetRules(helper);
            removePlayer(player);
        }
        helper.succeed();
    }

    public static void flightAndEnchantingAbilitiesFollowMenuAndGameMode(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerPlayer player = survivalPlayer(helper);
        CommandSourceStack source = server.createCommandSourceStack().withEntity(player).withSuppressedOutput();
        try {
            run(server, source, "fly");
            assertTrue(player.getAbilities().mayfly, "Expected /fly to toggle survival flight on");
            player.getAbilities().flying = true;
            setRule(helper, ModGameRules.FREE_ENCHANTING, true);
            player.openMenu(new SimpleMenuProvider((id, inventory, menuPlayer) -> new EnchantmentMenu(id, inventory),
                    Component.literal("Free enchanting")));
            ClientboundPlayerAbilitiesPacket shown = shownAbilities(player);
            assertTrue(shown.canInstabuild() && shown.canFly() && shown.isFlying() && !player.getAbilities().instabuild,
                    "Expected the menu override to preserve flight without granting server-side infinite materials");
            setRule(helper, ModGameRules.FREE_ENCHANTING, false);
            assertTrue(!shownAbilities(player).canInstabuild(), "Expected disabling the rule to drop the menu override");
            setRule(helper, ModGameRules.FREE_ENCHANTING, true);
            player.closeContainer();
            assertTrue(!shownAbilities(player).canInstabuild(), "Expected closing the menu to restore normal abilities");
            run(server, source, "fly @s false");
            assertTrue(!player.getAbilities().mayfly && !player.getAbilities().flying, "Expected flight revocation to stop flight");
            player.setGameMode(GameType.ADVENTURE);
            run(server, source, "fly @s true");
            assertTrue(player.getAbilities().mayfly, "Expected adventure flight");
            player.setGameMode(GameType.CREATIVE);
            run(server, source, "fly @s false");
            assertTrue(player.getAbilities().mayfly && shownAbilities(player).canInstabuild(),
                    "Expected /fly to leave creative abilities alone");
            player.setGameMode(GameType.SURVIVAL);
            assertTrue(!player.getAbilities().mayfly, "Expected a game-mode change to reset flight");
            var denied = server.getCommands().getDispatcher().parse("more",
                    source.withPermission(PermissionSet.NO_PERMISSIONS));
            assertTrue(denied.getReader().canRead() && denied.getContext().getCommand() == null,
                    "Expected no command available without game-master permission");
        } finally {
            resetRules(helper);
            removePlayer(player);
        }
        helper.succeed();
    }

    private static ClientboundPlayerAbilitiesPacket shownAbilities(ServerPlayer player) {
        return ((ShownAbilities) player).brainageserverutils$showAbilities(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
    }

    public static void rankingsReloadAndRejectInvalidConfiguration(GameTestHelper helper) {
        Path path = EnchantmentRankings.path();
        byte[] original = null;
        try {
            original = Files.readAllBytes(path);
            Files.writeString(path, "{\"enchantment_rankings\":[[\"minecraft:smite\",\"minecraft:sharpness\"]]}");
            assertTrue(EnchantmentRankings.load().getFirst().getFirst().equals(Enchantments.SMITE.identifier()),
                    "Expected edited rankings to load without restarting");
            ServerPlayer player = survivalPlayer(helper);
            try {
                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                run(helper.getLevel().getServer(), helper.getLevel().getServer().createCommandSourceStack()
                        .withEntity(player).withSuppressedOutput(), "maxenchant");
                assertEquals(5, level(helper, Enchantments.SMITE, player.getMainHandItem()), "Expected the command to use edited rankings");
                Files.writeString(path, "{\"enchantment_rankings\":[[\"minecraft:not_an_enchantment\"]]}");
                player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                run(helper.getLevel().getServer(), helper.getLevel().getServer().createCommandSourceStack()
                        .withEntity(player).withSuppressedOutput(), "maxenchant");
                assertTrue(player.getMainHandItem().getEnchantments().isEmpty(), "Expected unknown IDs not to change the held item");
            } finally {
                removePlayer(player);
            }
            for (String invalid : List.of("{", "{}", "{\"enchantment_rankings\":[true]}",
                    "{\"enchantment_rankings\":[[false]]}",
                    "{\"enchantment_rankings\":[[\"minecraft:smite\"],[\"minecraft:smite\"]]}")) {
                Files.writeString(path, invalid);
                boolean rejected = false;
                try {
                    EnchantmentRankings.load();
                } catch (EnchantmentRankings.InvalidConfigException expected) {
                    rejected = true;
                }
                assertTrue(rejected, "Expected invalid rankings to be rejected: " + invalid);
            }
            Files.delete(path);
            assertTrue(EnchantmentRankings.load().equals(EnchantmentRankings.defaults()) && Files.exists(path),
                    "Expected a missing file to recreate the default rankings");
        } catch (IOException | EnchantmentRankings.InvalidConfigException exception) {
            throw new AssertionError("Could not exercise the test server's rankings file", exception);
        } finally {
            if (original != null) {
                try {
                    Files.write(path, original);
                } catch (IOException exception) {
                    throw new AssertionError("Could not restore the test server's rankings file", exception);
                }
            }
        }
        helper.succeed();
    }

    /// A loaded survival player joined through BrainageLib's GameTestPlayer, standing on the test floor.
    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        UUID id = UUID.randomUUID();
        GameTestPlayer fixture = GameTestPlayer.join(helper, new GameProfile(id, "bsu-" + id.toString().substring(0, 8)))
                .markLoaded()
                .gameMode(GameType.SURVIVAL)
                .moveTo(helper, new BlockPos(1, 2, 1));
        synchronized (JOINED_PLAYERS) {
            JOINED_PLAYERS.put(fixture.player(), fixture);
        }
        return fixture.player();
    }

    /// Removes the player from the server and releases its connection.
    private static void removePlayer(ServerPlayer player) {
        GameTestPlayer fixture;
        synchronized (JOINED_PLAYERS) {
            fixture = JOINED_PLAYERS.remove(player);
        }
        fixture.close();
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
