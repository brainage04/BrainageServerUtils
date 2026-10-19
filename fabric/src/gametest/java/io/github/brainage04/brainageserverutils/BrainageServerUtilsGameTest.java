package io.github.brainage04.brainageserverutils;

import io.github.brainage04.brainageserverutils.gametest.ServerUtilsGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class BrainageServerUtilsGameTest {
    @GameTest
    public void itemAndConsumptionRulesChangeRuntimeBehavior(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::itemAndConsumptionRulesChangeRuntimeBehavior);
    }

    @GameTest
    public void durabilityRulePreventsItemDamage(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::durabilityRulePreventsItemDamage);
    }

    @GameTest
    public void bucketRulePreservesSourceWithoutDuplicatingResults(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::bucketRulePreservesSourceWithoutDuplicatingResults);
    }

    @GameTest
    public void preservedAmmunitionCannotBePickedUp(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::preservedAmmunitionCannotBePickedUp);
    }

    @GameTest
    public void scoreboardTracksHealthDeathsAndPlayerKills(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::scoreboardTracksHealthDeathsAndPlayerKills);
    }

    @GameTest
    public void maxEnchantResolvesConflictsByPreference(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::maxEnchantResolvesConflictsByPreference);
    }

    @GameTest
    public void hungerAndCooldownRulesAffectPlayers(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::hungerAndCooldownRulesAffectPlayers);
    }

    @GameTest
    public void enchantingRulesWaiveLevelCostsAndCap(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::enchantingRulesWaiveLevelCostsAndCap);
    }

    @GameTest
    public void playerCommandsAffectTheirTargets(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::playerCommandsAffectTheirTargets);
    }

    @GameTest
    public void flightAndEnchantingAbilitiesFollowMenuAndGameMode(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::flightAndEnchantingAbilitiesFollowMenuAndGameMode);
    }

    @GameTest
    public void rankingsReloadAndRejectInvalidConfiguration(GameTestHelper helper) {
        ServerUtilsGameTests.runIsolated(helper, ServerUtilsGameTests::rankingsReloadAndRejectInvalidConfiguration);
    }
}
