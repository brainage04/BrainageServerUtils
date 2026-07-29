package com.github.brainage04.brainageserverutils;

import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.gamerules.GameRules;

public final class BrainageServerUtilsGameTest {
    @GameTest
    public void customRulesAndCommandAreRegistered(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        GameRules rules = level.getGameRules();

        assertEquals(4, ModGameRules.REGISTERED.size(), "Expected four ServerUtils gamerules");
        for (var rule : ModGameRules.REGISTERED) {
            assertTrue(rules.availableRules().anyMatch(available -> available == rule), "Expected gamerule to be registered");
        }
        assertTrue(
                level.getServer().getCommands().getDispatcher().getRoot().getChild("brainagegamerule") != null,
                "Expected the compatibility gamerule command to be registered"
        );
        context.succeed();
    }

    @GameTest
    public void itemAndConsumptionRulesChangeRuntimeBehavior(GameTestHelper context) {
        ServerLevel level = context.getLevel();
        MinecraftServer server = level.getServer();
        GameRules rules = level.getGameRules();
        Zombie user = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        if (user == null) {
            throw new AssertionError("Expected to create a zombie");
        }

        ItemStack apples = new ItemStack(Items.APPLE, 3);
        Consumable consumable = apples.get(DataComponents.CONSUMABLE);
        if (consumable == null) {
            throw new AssertionError("Expected apples to have a consumable component");
        }

        try {
            rules.set(ModGameRules.DISABLE_ITEM_DECREMENT, false, server);
            apples.consume(1, user);
            assertEquals(2, apples.getCount(), "Expected vanilla item consumption");

            rules.set(ModGameRules.DISABLE_ITEM_DECREMENT, true, server);
            apples.consume(1, user);
            assertEquals(2, apples.getCount(), "Expected disabled item decrement to preserve the stack");

            rules.set(ModGameRules.INSTANT_CONSUME, false, server);
            assertTrue(consumable.consumeTicks() > 1, "Expected vanilla consumption duration");
            rules.set(ModGameRules.INSTANT_CONSUME, true, server);
            assertEquals(1, consumable.consumeTicks(), "Expected one-tick consumption");
        } finally {
            rules.set(ModGameRules.DISABLE_ITEM_DECREMENT, false, server);
            rules.set(ModGameRules.INSTANT_CONSUME, false, server);
        }

        context.succeed();
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
