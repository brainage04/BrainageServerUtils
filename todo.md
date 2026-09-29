# BrainageServerUtils todo

## Loader parity findings (2026-09-29)

From running the release NeoForge jar on a real NeoForge 26.2.0.41-beta server and client. Items marked *both loaders* come from shared code.

- [x] **High, NeoForge:** `brainageserverutils:disable_durability` (and `/setupgamerules sandbox`) does nothing on NeoForge; flint and steel still takes damage. `mixin/item/MixinItemStack.java:23-37` targets `processDurabilityChange(int, ServerLevel, ServerPlayer)`, but NeoForge's `ItemStack.hurtAndBreak` calls the `(int, ServerLevel, LivingEntity)` overload. The mixin applies without error and the only durability test runs on Fabric; add a NeoForge GameTest. Fixed: the rule now cancels `EnchantmentHelper.processDurabilityChange`, which both loaders' `hurtAndBreak`/`hurtWithoutBreaking` go through; shared GameTest `durability_rule_prevents_item_damage` runs on both loaders.
