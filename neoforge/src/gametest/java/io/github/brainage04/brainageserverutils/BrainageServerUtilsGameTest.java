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
    private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.ofEntries(
            Map.entry("item_and_consumption_rules_change_runtime_behavior", ServerUtilsGameTests::itemAndConsumptionRulesChangeRuntimeBehavior),
            Map.entry("durability_rule_prevents_item_damage", ServerUtilsGameTests::durabilityRulePreventsItemDamage),
            Map.entry("bucket_rule_preserves_source_without_duplicating_results", ServerUtilsGameTests::bucketRulePreservesSourceWithoutDuplicatingResults),
            Map.entry("preserved_ammunition_cannot_be_picked_up", ServerUtilsGameTests::preservedAmmunitionCannotBePickedUp),
            Map.entry("scoreboard_tracks_health_deaths_and_player_kills", ServerUtilsGameTests::scoreboardTracksHealthDeathsAndPlayerKills),
            Map.entry("max_enchant_resolves_conflicts_by_preference", ServerUtilsGameTests::maxEnchantResolvesConflictsByPreference),
            Map.entry("hunger_and_cooldown_rules_affect_players", ServerUtilsGameTests::hungerAndCooldownRulesAffectPlayers),
            Map.entry("enchanting_rules_waive_level_costs_and_cap", ServerUtilsGameTests::enchantingRulesWaiveLevelCostsAndCap),
            Map.entry("player_commands_affect_their_targets", ServerUtilsGameTests::playerCommandsAffectTheirTargets),
            Map.entry("flight_and_enchanting_abilities_follow_menu_and_game_mode", ServerUtilsGameTests::flightAndEnchantingAbilitiesFollowMenuAndGameMode),
            Map.entry("rankings_reload_and_reject_invalid_configuration", ServerUtilsGameTests::rankingsReloadAndRejectInvalidConfiguration)
    );

    private BrainageServerUtilsGameTest() {
    }

    @SubscribeEvent
    public static void registerTestFunctions(RegisterEvent event) {
        TESTS.forEach((name, test) -> event.register(
                BuiltInRegistries.TEST_FUNCTION.key(),
                Identifier.fromNamespaceAndPath(BrainageServerUtils.MOD_ID, name),
                () -> helper -> ServerUtilsGameTests.runIsolated(helper, test)
        ));
    }
}
