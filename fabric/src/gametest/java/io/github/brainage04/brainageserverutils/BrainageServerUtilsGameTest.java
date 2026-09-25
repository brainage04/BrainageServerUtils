package io.github.brainage04.brainageserverutils;

import io.github.brainage04.brainageserverutils.gametest.ServerUtilsGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class BrainageServerUtilsGameTest {
    @GameTest
    public void customRulesAndCommandsAreRegistered(GameTestHelper helper) {
        ServerUtilsGameTests.customRulesAndCommandsAreRegistered(helper);
    }

    @GameTest
    public void itemAndConsumptionRulesChangeRuntimeBehavior(GameTestHelper helper) {
        ServerUtilsGameTests.itemAndConsumptionRulesChangeRuntimeBehavior(helper);
    }

    @GameTest
    public void maxEnchantResolvesConflictsByPreference(GameTestHelper helper) {
        ServerUtilsGameTests.maxEnchantResolvesConflictsByPreference(helper);
    }

    @GameTest
    public void hungerAndCooldownRulesAffectPlayers(GameTestHelper helper) {
        ServerUtilsGameTests.hungerAndCooldownRulesAffectPlayers(helper);
    }

    @GameTest
    public void enchantingRulesWaiveLevelCostsAndCap(GameTestHelper helper) {
        ServerUtilsGameTests.enchantingRulesWaiveLevelCostsAndCap(helper);
    }

    @GameTest
    public void playerCommandsAffectTheirTargets(GameTestHelper helper) {
        ServerUtilsGameTests.playerCommandsAffectTheirTargets(helper);
    }
}
