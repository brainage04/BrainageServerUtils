# BrainageServerUtils

A server-side utility mod for Fabric and NeoForge on Minecraft 26.2. Clients do not need to install the mod.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer with Fabric API, or NeoForge 26.2.0.41-beta or newer
- Java 25 or newer

## Gamerules

All custom rules are disabled by default.

| Gamerule | Effect |
| --- | --- |
| `brainageserverutils:disable_durability` | Prevents durability damage to item stacks. |
| `brainageserverutils:disable_item_decrement` | Prevents ordinary item and ammunition consumption. Fired arrows cannot be picked up, preventing duplication. |
| `brainageserverutils:disable_bucket_decrement` | Preserves the source bucket when creating a filled bucket result. |
| `brainageserverutils:instant_consume` | Reduces food, drink, and other consumable use time to one tick. |
| `brainageserverutils:disable_hunger` | Keeps hunger full and stops saturation from draining. |
| `brainageserverutils:free_enchanting` | Enchanting tables and anvils cost no levels and have no level requirement. Lapis and anvil materials are still used. |
| `brainageserverutils:disable_too_expensive` | Anvil costs of 40 levels or more are capped at 39 instead of showing "Too Expensive!". Stacked inputs are still refused. |
| `brainageserverutils:disable_item_cooldowns` | Removes item cooldowns (ender pearls, wind charges, chorus fruit, goat horns and similar), and shields can no longer be disabled by axes. |

```text
/gamerule brainageserverutils:disable_durability true
```

Unmodified clients only let a player click an enchanting-table option they can afford. While `free_enchanting` is on, the server therefore tells the client the player has creative-style infinite materials while an enchanting table, or an anvil whose result can be taken, is open. Nothing changes on the server. An anvil costing 40 levels or more (unless `disable_too_expensive` caps it) drops the override so the client still shows "Too Expensive!", and ability changes made while a menu is open, such as `/fly`, keep it.

## Commands

All commands require game master permission (level 2). `[targets]` defaults to the player running the command.

| Command | Effect |
| --- | --- |
| `/maxenchant [targets] [preferred...]` | Replaces the enchantments on each target's held item with every applicable non-curse enchantment at its maximum level. Up to eight preferred enchantments may follow the targets; they win any conflict, in the order given. |
| `/kit <netherite\|ranged\|elytra> [targets]` | Gives unbreakable gear enchanted as by `/maxenchant`. `netherite`: full armour, sword, spear, mace, pickaxe, axe, shovel and shield. `ranged`: bow, crossbow, trident and 64 arrows. `elytra`: elytra and 64 flight-duration-3 rockets without explosions. |
| `/heal [targets]` | Restores health, hunger, saturation and air, puts out fire, thaws, and removes harmful effects. |
| `/fly [targets] [true\|false]` | Toggles, or sets, flight for survival and adventure players. Changing game mode resets it, as in vanilla. |
| `/more [targets]` | Fills each target's held stack to its maximum stack size. |
| `/setupgamerules <survival\|sandbox>` | Applies the server's preferred vanilla gamerules. `survival` keeps vanilla damage and turns every rule above off; `sandbox` turns every rule above on and disables fall, fire, drowning and freezing damage. |
| `/setupscoreboard` | Creates and displays Health, Deaths, and Kills objectives. |

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

## Building and verification

```shell
./gradlew build
```

The build produces separate Fabric and NeoForge JARs in `build/libs`; production GameTests validate entrypoint initialization, gameplay mixins, and commands on each loader. The shared GameTest bodies live in `common/src/gametest` and are compiled into each loader's GameTest source set.

## License

BrainageServerUtils is available under the MIT License.
