# BrainageServerUtils

A server-side utility mod for Fabric and NeoForge on Minecraft 26.2. Clients do not need to install the mod.

The accelerated combat and damage rules formerly included here now live in the standalone [AcceleratedDamage](https://github.com/brainage04/AcceleratedDamage) mod. The two mods can be installed together without overlapping mixins.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer with Fabric API, or NeoForge 26.2.0.23-beta or newer
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

## Migrating from the Fabric-only release

Install exactly one matching JAR: the Fabric JAR with Fabric Loader and Fabric API, or the NeoForge JAR with NeoForge. Remove the previous loader's JAR before switching loaders. The mod ID remains `brainageserverutils`, so existing gamerule names and world data paths are retained. This remains a server-side mod; vanilla clients do not need it.

## Building and verification

```shell
./gradlew build
```

The build produces separate Fabric and NeoForge JARs in `build/libs`; production GameTests validate entrypoint initialization and gameplay mixins on each loader.

## License

BrainageServerUtils is available under the MIT License.
