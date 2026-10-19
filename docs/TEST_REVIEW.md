# Test review, 2026-10-19

## Frozen rubric

Classify a test before deciding whether to delete it:

- Keep: observes a player-visible state transition or failure using production behavior, including loader-specific execution and client synchronization.
- Redundant: another named test exercises the same production path, inputs and outcome, without a distinct failure mode.
- Tautological: checks a copied algorithm, mock echo, registration/wiring, or constant rather than behavior.

A loader registration is not another test body. Shared server bodies must still run on both Fabric and NeoForge. Classifying a recording scene does not make its packet synchronization assertions redundant with a server-only test.

## Classification

There are no standalone unit tests or NeoForge client GameTests. The shared server bodies have Fabric and NeoForge registrations.

- `customRulesAndCommandsAreRegistered`: tautological, flagged for reading. Counts eight rules and checks rule/command registration without executing gameplay. Removed the shared body, both loader registrations and its NeoForge test-instance JSON. Command and rule execution remains covered by the behavioral tests below, and `/fly` by the client test.
- `itemAndConsumptionRulesChangeRuntimeBehavior`: keep. Rule-on/rule-off consumption and use duration execute the actual mixins.
- `durabilityRulePreventsItemDamage`: keep. Held tools and worn armour exercise the previously divergent loader durability paths.
- `bucketRulePreservesSourceWithoutDuplicatingResults`: keep. Separates bucket preservation from ordinary item preservation and checks repeated filled results.
- `preservedAmmunitionCannotBePickedUp`: keep. Fires a real bow, observes inventory consumption and invokes the arrow pickup path, including after the rule is switched off.
- `scoreboardTracksHealthDeathsAndPlayerKills`: keep. Executes the command, changes player health, kills a player and checks score updates and repeated setup.
- `maxEnchantResolvesConflictsByPreference`: keep. Ranking, explicit preference, unranked conflict, curse exclusion and inapplicable-item behavior are distinct outcomes.
- `hungerAndCooldownRulesAffectPlayers`: keep. Hunger, item cooldowns and shield disabling exercise different runtime hooks.
- `enchantingRulesWaiveLevelCostsAndCap`: keep. Enchanting and anvil requirements, charging and the expensive-result cap have separate hooks.
- `playerCommandsAffectTheirTargets`: keep. Dispatcher execution covers inventory/effect mutation and both gamerule presets, not only helper calculations.
- Fabric `BrainageServerUtilsClientGameTest.runTest`: keep. Inventory synchronization, the free-enchanting client ability override, `/fly` during that override, anvil cost changes and menu-close restoration cannot be established by server-only tests.

No tests were classified as redundant. Only the registration test was flagged for deletion.
