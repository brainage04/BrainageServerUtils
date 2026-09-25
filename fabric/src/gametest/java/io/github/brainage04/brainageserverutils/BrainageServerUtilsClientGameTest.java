package io.github.brainage04.brainageserverutils;

import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestRecorder;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestServers;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("UnstableApiUsage")
public final class BrainageServerUtilsClientGameTest implements FabricClientGameTest {
    private static final int DURABILITY_SLOT = 0;
    private static final int ITEM_PRESERVATION_SLOT = 1;
    private static final int INSTANT_CONSUMPTION_SLOT = 2;

    @Override
    public void runTest(ClientGameTestContext context) {
        Properties serverProperties = ClientGameTestServers.flatServerProperties();

        ClientGameTestServers.withDedicatedServer(context, serverProperties, "Brainage Server Utils inventory recording GameTest", server -> {
            RuleSnapshot rules = server.computeOnServer(BrainageServerUtilsClientGameTest::prepareDurabilityStage);
            try {
                ClientGameTestServers.assertClientWorldAndPlayerAvailable(context);
                context.waitTicks(20);
                assertSelectedClientItem(context, Items.DIAMOND_PICKAXE, DURABILITY_SLOT, 1);

                ClientGameTestRecorder.startRecording(context);
                ClientGameTestRecorder.showStep(
                        context,
                        "serverutils.durability",
                        "Durability preservation",
                        "The held diamond pickaxe remains undamaged while disable_durability is enabled"
                );
                context.waitTicks(35);

                server.runOnServer(BrainageServerUtilsClientGameTest::prepareItemPreservationStage);
                selectClientSlot(context, ITEM_PRESERVATION_SLOT);
                context.waitTicks(10);
                assertSelectedClientItem(context, Items.APPLE, ITEM_PRESERVATION_SLOT, 3);
                ClientGameTestRecorder.showStep(
                        context,
                        "serverutils.item-preservation",
                        "Item preservation",
                        "Consuming an apple leaves all three apples in the selected hotbar stack"
                );
                context.waitTicks(35);

                server.runOnServer(BrainageServerUtilsClientGameTest::prepareInstantConsumptionStage);
                selectClientSlot(context, INSTANT_CONSUMPTION_SLOT);
                context.waitTicks(10);
                assertSelectedClientItem(context, Items.GOLDEN_APPLE, INSTANT_CONSUMPTION_SLOT, 1);
                ClientGameTestRecorder.showStep(
                        context,
                        "serverutils.instant-consumption",
                        "Instant consumption",
                        "The selected golden apple uses the one-tick instant_consume behavior"
                );
                context.waitTicks(35);

                server.runOnServer(BrainageServerUtilsClientGameTest::openEnchantingTable);
                context.waitTicks(10);
                assertClientInfiniteMaterials(context, true, "while an enchanting table is open with free_enchanting");
                ClientGameTestRecorder.showStep(
                        context,
                        "serverutils.free-enchanting",
                        "Free enchanting",
                        "The unmodified client treats every enchanting table option as affordable at level 0"
                );
                context.waitTicks(35);

                server.runOnServer(minecraftServer -> runCommand(minecraftServer, "fly @a true"));
                context.waitTicks(10);
                assertClientMayFly(context, true);
                assertClientInfiniteMaterials(context, true, "after /fly changes abilities while the table is open");
                server.runOnServer(minecraftServer -> player(minecraftServer).closeContainer());
                context.waitTicks(10);
                assertClientInfiniteMaterials(context, false, "after the enchanting table closes");
                assertClientMayFly(context, true);
                server.runOnServer(minecraftServer -> runCommand(minecraftServer, "fly @a false"));
                context.waitTicks(10);
                assertClientMayFly(context, false);

                server.runOnServer(BrainageServerUtilsClientGameTest::openTooExpensiveAnvil);
                context.waitTicks(10);
                assertClientInfiniteMaterials(context, false, "while an anvil costs 40 levels without disable_too_expensive");
                server.runOnServer(BrainageServerUtilsClientGameTest::capAnvilCost);
                context.waitTicks(10);
                assertClientInfiniteMaterials(context, true, "once disable_too_expensive caps the anvil cost");
                server.runOnServer(minecraftServer -> player(minecraftServer).closeContainer());
                context.waitTicks(10);
                assertClientInfiniteMaterials(context, false, "after the anvil closes");
            } finally {
                server.runOnServer(minecraftServer -> {
                    rules.restore(minecraftServer);
                    player(minecraftServer).getInventory().clearContent();
                });
            }
        });
    }

    private static RuleSnapshot prepareDurabilityStage(MinecraftServer server) {
        ServerPlayer player = player(server);
        GameRules rules = player.level().getGameRules();
        RuleSnapshot snapshot = RuleSnapshot.capture(rules);
        rules.set(ModGameRules.DISABLE_DURABILITY, true, server);
        rules.set(ModGameRules.DISABLE_ITEM_DECREMENT, true, server);
        rules.set(ModGameRules.INSTANT_CONSUME, true, server);
        rules.set(ModGameRules.FREE_ENCHANTING, true, server);

        player.getInventory().clearContent();
        ItemStack pickaxe = named(Items.DIAMOND_PICKAXE, 1, "Durability preserved");
        player.getInventory().setItem(DURABILITY_SLOT, pickaxe);
        player.getInventory().setItem(ITEM_PRESERVATION_SLOT, named(Items.APPLE, 3, "Item count preserved"));
        player.getInventory().setItem(INSTANT_CONSUMPTION_SLOT, named(Items.GOLDEN_APPLE, 1, "Instant consumption"));
        player.getInventory().setSelectedSlot(DURABILITY_SLOT);

        pickaxe.hurtAndBreak(5, player, EquipmentSlot.MAINHAND);
        if (pickaxe.getDamageValue() != 0) {
            throw new AssertionError("Expected disable_durability to preserve the diamond pickaxe.");
        }
        return snapshot;
    }

    private static void prepareItemPreservationStage(MinecraftServer server) {
        ServerPlayer player = player(server);
        ItemStack apples = player.getInventory().getItem(ITEM_PRESERVATION_SLOT);
        apples.consume(1, player);
        if (apples.getCount() != 3) {
            throw new AssertionError("Expected disable_item_decrement to preserve the apple stack.");
        }
    }

    private static void prepareInstantConsumptionStage(MinecraftServer server) {
        ServerPlayer player = player(server);
        ItemStack goldenApple = player.getInventory().getItem(INSTANT_CONSUMPTION_SLOT);
        Consumable consumable = goldenApple.get(DataComponents.CONSUMABLE);
        if (consumable == null || consumable.consumeTicks() != 1) {
            throw new AssertionError("Expected instant_consume to make golden apples consume in one tick.");
        }
    }

    private static void openEnchantingTable(MinecraftServer server) {
        ServerPlayer player = player(server);
        player.setExperienceLevels(0);
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new EnchantmentMenu(containerId, inventory),
                Component.literal("Free enchanting")
        ));
    }

    /// Repairing a damaged sword with another, both already worked many times, costs well over 40 levels.
    private static void openTooExpensiveAnvil(MinecraftServer server) {
        ServerPlayer player = player(server);
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new AnvilMenu(containerId, inventory, ContainerLevelAccess.NULL),
                Component.literal("Too expensive")
        ));
        AnvilMenu anvil = (AnvilMenu) player.containerMenu;
        for (int slot = 0; slot < 2; slot++) {
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.set(DataComponents.REPAIR_COST, 39);
            sword.setDamageValue(slot == 0 ? 100 : 0);
            anvil.getSlot(slot).set(sword);
        }
        if (anvil.getCost() < ModGameRules.TOO_EXPENSIVE_ANVIL_COST) {
            throw new AssertionError("Expected the anvil fixture to cost at least 40 levels, found " + anvil.getCost() + ".");
        }
    }

    private static void capAnvilCost(MinecraftServer server) {
        ServerPlayer player = player(server);
        player.level().getGameRules().set(ModGameRules.DISABLE_TOO_EXPENSIVE, true, server);
        AnvilMenu anvil = (AnvilMenu) player.containerMenu;
        anvil.slotsChanged(anvil.getSlot(0).container);
        if (anvil.getCost() != ModGameRules.TOO_EXPENSIVE_ANVIL_COST - 1) {
            throw new AssertionError("Expected disable_too_expensive to cap the anvil cost at 39, found " + anvil.getCost() + ".");
        }
    }

    private static void runCommand(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
    }

    private static void assertClientMayFly(ClientGameTestContext context, boolean expected) {
        context.runOnClient(client -> {
            if (client.player.getAbilities().mayfly != expected) {
                throw new AssertionError("Expected /fly to set client flight permission to " + expected + ".");
            }
        });
    }

    private static void assertClientInfiniteMaterials(ClientGameTestContext context, boolean expected, String when) {
        context.runOnClient(client -> {
            if (client.player.hasInfiniteMaterials() != expected) {
                throw new AssertionError("Expected client infinite materials to be " + expected + " " + when + ".");
            }
        });
    }

    private static void selectClientSlot(ClientGameTestContext context, int slot) {
        context.runOnClient(client -> client.player.getInventory().setSelectedSlot(slot));
    }

    private static void assertSelectedClientItem(ClientGameTestContext context, net.minecraft.world.item.Item item, int slot, int count) {
        context.runOnClient(client -> {
            if (client.player.getInventory().getSelectedSlot() != slot
                    || !client.player.getInventory().getSelectedItem().is(item)
                    || client.player.getInventory().getSelectedItem().getCount() != count) {
                throw new AssertionError("Expected the labeled recording fixture to synchronize to the selected client hotbar slot.");
            }
        });
    }

    private static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    private static ItemStack named(net.minecraft.world.item.Item item, int count, String label) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(label));
        return stack;
    }

    private record RuleSnapshot(Map<GameRule<Boolean>, Boolean> values) {
        static RuleSnapshot capture(GameRules rules) {
            Map<GameRule<Boolean>, Boolean> values = new HashMap<>();
            for (GameRule<Boolean> rule : ModGameRules.REGISTERED) {
                values.put(rule, rules.get(rule));
            }
            return new RuleSnapshot(values);
        }

        void restore(MinecraftServer server) {
            values.forEach((rule, value) -> server.getGameRules().set(rule, value, server));
        }
    }
}
