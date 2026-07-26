package com.github.brainage04.brainageserverutils;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestRecorder;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestServers;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.Properties;

@SuppressWarnings("UnstableApiUsage")
public final class BrainageServerUtilsClientGameTest implements FabricClientGameTest {
    private static final int DURABILITY_SLOT = 0;
    private static final int ITEM_PRESERVATION_SLOT = 1;
    private static final int INSTANT_CONSUMPTION_SLOT = 2;

    @Override
    public void runTest(ClientGameTestContext context) {
        Properties serverProperties = ClientGameTestServers.flatServerProperties();

        try (TestDedicatedServerContext server = context.worldBuilder().createServer(serverProperties)) {
            ClientGameTestServers.connectToDedicatedServer(context, server, "Brainage Server Utils inventory recording GameTest");
            try {
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
                } finally {
                    server.runOnServer(minecraftServer -> {
                        rules.restore(minecraftServer);
                        player(minecraftServer).getInventory().clearContent();
                    });
                }
            } finally {
                ClientGameTestServers.disconnectFromDedicatedServer(context);
            }
        }
    }

    private static RuleSnapshot prepareDurabilityStage(MinecraftServer server) {
        ServerPlayer player = player(server);
        GameRules rules = player.level().getGameRules();
        RuleSnapshot snapshot = RuleSnapshot.capture(rules);
        rules.set(ModGameRules.DISABLE_DURABILITY, true, server);
        rules.set(ModGameRules.DISABLE_ITEM_DECREMENT, true, server);
        rules.set(ModGameRules.INSTANT_CONSUME, true, server);

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

    private record RuleSnapshot(boolean durability, boolean itemDecrement, boolean bucketDecrement, boolean instantConsume) {
        static RuleSnapshot capture(GameRules rules) {
            return new RuleSnapshot(
                    rules.get(ModGameRules.DISABLE_DURABILITY),
                    rules.get(ModGameRules.DISABLE_ITEM_DECREMENT),
                    rules.get(ModGameRules.DISABLE_BUCKET_DECREMENT),
                    rules.get(ModGameRules.INSTANT_CONSUME)
            );
        }

        void restore(MinecraftServer server) {
            GameRules rules = server.overworld().getGameRules();
            rules.set(ModGameRules.DISABLE_DURABILITY, durability, server);
            rules.set(ModGameRules.DISABLE_ITEM_DECREMENT, itemDecrement, server);
            rules.set(ModGameRules.DISABLE_BUCKET_DECREMENT, bucketDecrement, server);
            rules.set(ModGameRules.INSTANT_CONSUME, instantConsume, server);
        }
    }
}
