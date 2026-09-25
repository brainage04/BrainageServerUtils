package io.github.brainage04.brainageserverutils;

import io.github.brainage04.brainageserverutils.gametest.ServerUtilsGameTests;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

/// Each function has a matching `data/brainageserverutils/test_instance/<name>.json`.
@EventBusSubscriber(modid = BrainageServerUtils.MOD_ID)
public final class BrainageServerUtilsGameTest {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.of(
            "custom_rules_and_commands_are_registered", ServerUtilsGameTests::customRulesAndCommandsAreRegistered,
            "item_and_consumption_rules_change_runtime_behavior", ServerUtilsGameTests::itemAndConsumptionRulesChangeRuntimeBehavior,
            "max_enchant_resolves_conflicts_by_preference", ServerUtilsGameTests::maxEnchantResolvesConflictsByPreference,
            "hunger_and_cooldown_rules_affect_players", ServerUtilsGameTests::hungerAndCooldownRulesAffectPlayers,
            "enchanting_rules_waive_level_costs_and_cap", ServerUtilsGameTests::enchantingRulesWaiveLevelCostsAndCap,
            "player_commands_affect_their_targets", ServerUtilsGameTests::playerCommandsAffectTheirTargets
    );

    private BrainageServerUtilsGameTest() {
    }

    @SubscribeEvent
    public static void registerTestFunctions(RegisterEvent event) {
        TESTS.forEach((name, test) -> event.register(
                BuiltInRegistries.TEST_FUNCTION.key(),
                Identifier.fromNamespaceAndPath(BrainageServerUtils.MOD_ID, name),
                () -> test
        ));
    }
}
