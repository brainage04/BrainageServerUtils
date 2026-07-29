package com.github.brainage04.brainageserverutils.gamerule;

import net.minecraft.world.level.gamerules.GameRule;

import java.util.List;
import java.util.function.Function;

public final class ModGameRules {
    public static GameRule<Boolean> DISABLE_DURABILITY;
    public static GameRule<Boolean> DISABLE_ITEM_DECREMENT;
    public static GameRule<Boolean> DISABLE_BUCKET_DECREMENT;
    public static GameRule<Boolean> INSTANT_CONSUME;

    public static List<GameRule<Boolean>> REGISTERED = List.of();

    private ModGameRules() {
    }

    public static void initialize(Function<String, GameRule<Boolean>> registrar) {
        if (!REGISTERED.isEmpty()) {
            return;
        }
        DISABLE_DURABILITY = registrar.apply("disable_durability");
        DISABLE_ITEM_DECREMENT = registrar.apply("disable_item_decrement");
        DISABLE_BUCKET_DECREMENT = registrar.apply("disable_bucket_decrement");
        INSTANT_CONSUME = registrar.apply("instant_consume");
        REGISTERED = List.of(DISABLE_DURABILITY, DISABLE_ITEM_DECREMENT, DISABLE_BUCKET_DECREMENT, INSTANT_CONSUME);
    }

}
