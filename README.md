# BrainageServerUtils

A server-side Fabric utility mod for Minecraft 26.2. Clients do not need to install the mod.

The accelerated combat and damage rules formerly included here now live in the standalone [AcceleratedDamage](https://github.com/brainage04/AcceleratedDamage) mod. The two mods can be installed together without overlapping mixins.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Java 25 or newer

## Gamerules

All custom rules are disabled by default.

| Gamerule | Effect |
| --- | --- |
| `brainageserverutils:disable_durability` | Prevents durability damage to item stacks. |
| `brainageserverutils:disable_item_decrement` | Prevents ordinary item and ammunition consumption. Fired arrows cannot be picked up, preventing duplication. |
| `brainageserverutils:disable_bucket_decrement` | Preserves the source bucket when creating a filled bucket result. |
| `brainageserverutils:instant_consume` | Reduces food, drink, and other consumable use time to one tick. |

Use the vanilla gamerule command:

```text
/gamerule brainageserverutils:disable_durability true
```

The compatibility command exposes the same four rules with their original names:

```text
/brainagegamerule disableDurability true
/brainagegamerule all false
```

## Utility commands

- `/givefireworks <targets>` gives each target a stack of configured flight-duration-3 fireworks.
- `/setupgamerules` applies the server's preferred vanilla gamerule preset.
- `/setupscoreboard` creates and displays Health, Deaths, and Kills objectives.
- `/updateplayerhotbar` resynchronizes the executing player's hotbar.

## Building and verification

```shell
./gradlew build
```

The build starts a dedicated GameTest server, which validates entrypoint initialization and every required mixin target.

## License

BrainageServerUtils is available under the MIT License.
