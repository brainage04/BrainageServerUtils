package io.github.brainage04.brainageserverutils.gamerule;

import java.util.List;
import java.util.function.Function;
import net.minecraft.world.level.gamerules.GameRule;

public final class ModGameRules {
    /// Vanilla shows "Too Expensive!" and refuses the anvil result at this cost.
    public static final int TOO_EXPENSIVE_ANVIL_COST = 40;

    public static GameRule<Boolean> DISABLE_DURABILITY;
    public static GameRule<Boolean> DISABLE_ITEM_DECREMENT;
    public static GameRule<Boolean> DISABLE_BUCKET_DECREMENT;
    public static GameRule<Boolean> INSTANT_CONSUME;
    public static GameRule<Boolean> DISABLE_HUNGER;
    public static GameRule<Boolean> FREE_ENCHANTING;
    public static GameRule<Boolean> DISABLE_TOO_EXPENSIVE;
    public static GameRule<Boolean> DISABLE_ITEM_COOLDOWNS;

    public static List<GameRule<Boolean>> REGISTERED = List.of();

    private ModGameRules() {
    }

    /// Registers every boolean rule (all default to false) under the mod namespace with the given path.
    public static void initialize(Function<String, GameRule<Boolean>> registrar) {
        DISABLE_DURABILITY = registrar.apply("disable_durability");
        DISABLE_ITEM_DECREMENT = registrar.apply("disable_item_decrement");
        DISABLE_BUCKET_DECREMENT = registrar.apply("disable_bucket_decrement");
        INSTANT_CONSUME = registrar.apply("instant_consume");
        DISABLE_HUNGER = registrar.apply("disable_hunger");
        FREE_ENCHANTING = registrar.apply("free_enchanting");
        DISABLE_TOO_EXPENSIVE = registrar.apply("disable_too_expensive");
        DISABLE_ITEM_COOLDOWNS = registrar.apply("disable_item_cooldowns");
        REGISTERED = List.of(
                DISABLE_DURABILITY,
                DISABLE_ITEM_DECREMENT,
                DISABLE_BUCKET_DECREMENT,
                INSTANT_CONSUME,
                DISABLE_HUNGER,
                FREE_ENCHANTING,
                DISABLE_TOO_EXPENSIVE,
                DISABLE_ITEM_COOLDOWNS
        );
    }
}
