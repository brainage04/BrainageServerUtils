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

## Coverage gaps and additions

Before this wave, bucket results, arrow pickup and scoreboard updates had no behavioral test. Step 1 adds those on both loaders.

Step 5 extends the shared suite:

- Real survival/adventure/creative `ServerPlayer` fixtures replace GameTest's always-creative mock subclass. Embedded channels provide only the transport; all player and command behavior is production code. Fixture removal closes the channel.
- `flightAndEnchantingAbilitiesFollowMenuAndGameMode`: keep. Covers toggle/set flight, flight revocation, adventure/creative behavior, game-mode reset, permission denial, free-enchanting ability changes and menu closure on both loaders.
- `rankingsReloadAndRejectInvalidConfiguration`: keep. Covers live file edits through `/maxenchant`, unknown registry IDs, malformed JSON, schema/type/duplicate rejection and default-file recreation. Restores the original file in a finally block.
- Existing command coverage now checks ranged and elytra kits, rocket components, healing air/saturation/freezing while preserving beneficial effects, all four environmental damage rules and the six common preset settings.
- Existing hunger coverage now applies exhaustion and checks saturation preservation. Enchanting checks lapis consumption.
- Every shared body snapshots and restores all gamerules at its loader boundary, including the vanilla rules changed by presets.

Remaining manual coverage: the NeoForge unmodified-client GUI has no client GameTest, although the shared abilities and menu tests execute its server hooks. Pixel appearance and translated vanilla tooltip layout remain visual checks; this mod has no custom renderer, background worker or shutdown-owned resource. Fabric's client test covers actual packet delivery and menu transitions. Full inventories, every possible third-party enchantment conflict, per-language command prose and disconnect/rejoin while an enchanting menu is open are not exhaustive automated scenarios.

## Code review

Reviewed the shared commands, ranking parser/selection, gamerule hooks, packet/ability handling, inventory resynchronization and both loader entrypoints. Lifecycle registration clears the current-server reference only for the server being stopped; the reference is volatile. Configuration readers/writers use try-with-resources. The mod owns no worker thread or closeable runtime client.

No clear production bug was changed in this review. Decisions left unchanged:

- `/setupscoreboard` reuses objectives named Health, Deaths or Kills even if another administrator created them with a different criterion. Replacing them would destroy existing score data. Recommendation: explicitly reject a conflicting criterion with a useful command error rather than replacing it; confirm that policy first.
- `disable_item_cooldowns` blocks new cooldowns, but does not clear a cooldown already running when the rule is enabled. Recommendation: keep that prospective behavior unless immediate clearing is wanted.
- Commands use English server-side literals. This avoids untranslated custom keys for clients without the mod, but does not localize the feedback. Recommendation: retain English for this server-only mod unless a client resource pack is planned.

The README now states that explicitly preferred curses are allowed, matching the existing selector behavior. No change to that behavior was made.
