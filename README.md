# BrainageServerUtils

A server-side utility mod for Fabric and NeoForge on Minecraft 26.2. Clients do not need to install the mod.

## Features

- Custom gamerules for durability, consumption, hunger, enchanting and cooldowns.
- Unbreakable equipment kits and player utility commands.
- Health, Deaths, and Kills objectives.

## Getting started

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer with Fabric API, or NeoForge 26.2.0.88 or newer
- Java 25 or newer
- Install on the server.

## Commands

All commands require game master permission (level 2). `[targets]` defaults to the player running the command.

- `/maxenchant [targets] [preferred...]` - replaces the enchantments on each target's held item with every applicable non-curse enchantment at its maximum level. Up to eight preferred enchantments may follow the targets; they win any conflict, in the order given. Explicit preferences can include curses.
- `/kit <netherite|ranged|elytra> [targets]` - gives unbreakable gear enchanted as by `/maxenchant`. `netherite`: full armour, sword, spear, mace, pickaxe, axe, shovel and shield. `ranged`: bow, crossbow, trident and 64 arrows. `elytra`: elytra and 64 flight-duration-3 rockets without explosions.
- `/heal [targets]` - restores health, hunger, saturation and air, puts out fire, thaws, and removes harmful effects.
- `/fly [targets] [true|false]` - toggles, or sets, flight for survival and adventure players. Changing game mode resets it, as in vanilla.
- `/more [targets]` - fills each target's held stack to its maximum stack size.
- `/setupgamerules <survival|sandbox>` - applies the server's preferred vanilla gamerules. `survival` keeps vanilla damage and turns every rule below off; `sandbox` turns every rule below on and disables fall, fire, drowning and freezing damage.
- `/setupscoreboard` - creates and displays Health, Deaths, and Kills objectives.

## Configuration

### Gamerules

All custom rules are disabled by default.

- `brainageserverutils:disable_durability` - prevents durability damage to item stacks.
- `brainageserverutils:disable_item_decrement` - prevents ordinary item and ammunition consumption. Fired arrows cannot be picked up, preventing duplication.
- `brainageserverutils:disable_bucket_decrement` - preserves the source bucket when creating a filled bucket result.
- `brainageserverutils:instant_consume` - reduces food, drink, and other consumable use time to one tick.
- `brainageserverutils:disable_hunger` - keeps hunger full and stops saturation from draining.
- `brainageserverutils:free_enchanting` - enchanting tables and anvils cost no levels and have no level requirement. Lapis and anvil materials are still used.
- `brainageserverutils:disable_too_expensive` - anvil costs of 40 levels or more are capped at 39 instead of showing "Too Expensive!". Stacked inputs are still refused.
- `brainageserverutils:disable_item_cooldowns` - removes item cooldowns (ender pearls, wind charges, chorus fruit, goat horns and similar), and shields can no longer be disabled by axes.

```text
/gamerule brainageserverutils:disable_durability true
```

Unmodified clients only let a player click an enchanting-table option they can afford. While `free_enchanting` is on, the server therefore tells the client the player has creative-style infinite materials while an enchanting table, or an anvil whose result can be taken, is open. Nothing changes on the server. An anvil costing 40 levels or more (unless `disable_too_expensive` caps it) drops the override so the client still shows "Too Expensive!", and ability changes made while a menu is open, such as `/fly`, keep it.

### Enchantment rankings

`config/brainageserverutils.json` decides between mutually exclusive enchantments for `/maxenchant` and `/kit`. Each ranking lists enchantments from most to least preferred, and an enchantment may appear in only one ranking. The file is created with these defaults and re-read on every use, so edits apply without a restart:

```json
{
  "enchantment_rankings": [
    ["minecraft:sharpness", "minecraft:density", "minecraft:breach", "minecraft:smite", "minecraft:bane_of_arthropods", "minecraft:impaling"],
    ["minecraft:protection", "minecraft:blast_protection", "minecraft:fire_protection", "minecraft:projectile_protection"],
    ["minecraft:infinity", "minecraft:mending"],
    ["minecraft:fortune", "minecraft:silk_touch"],
    ["minecraft:multishot", "minecraft:piercing"],
    ["minecraft:depth_strider", "minecraft:frost_walker"],
    ["minecraft:loyalty", "minecraft:channeling", "minecraft:riptide"]
  ]
}
```

Conflicts are resolved in this order: preferred enchantments passed to the command, then the rankings, then enchantment ID. When a conflict falls through to ID order, `/maxenchant` says which enchantment it kept and how to prefer the other, for example `/maxenchant @s minecraft:silk_touch`.

## Compatibility

No known incompatibilities.

## Documentation

- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) - building and GameTests.
- [docs/TEST_REVIEW.md](docs/TEST_REVIEW.md) - test classifications and coverage.
- [docs/RELEASE.md](docs/RELEASE.md) - release automation.
- [docs/MODRINTH.md](docs/MODRINTH.md) - optional Modrinth publishing.

BrainageServerUtils is available under the MIT License.
