package com.github.brainage04.brainageserverutils.gamerule;

import com.github.brainage04.brainageserverutils.BrainageServerUtils;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

import java.util.List;

public final class ModGameRules {
    public static final GameRule<Boolean> DISABLE_DURABILITY = register("disable_durability");
    public static final GameRule<Boolean> DISABLE_ITEM_DECREMENT = register("disable_item_decrement");
    public static final GameRule<Boolean> DISABLE_BUCKET_DECREMENT = register("disable_bucket_decrement");
    public static final GameRule<Boolean> INSTANT_CONSUME = register("instant_consume");

    public static final List<GameRule<Boolean>> REGISTERED = List.of(
            DISABLE_DURABILITY,
            DISABLE_ITEM_DECREMENT,
            DISABLE_BUCKET_DECREMENT,
            INSTANT_CONSUME
    );

    private ModGameRules() {
    }

    /**
     * Forces registration while the game-rule registry is still mutable.
     */
    public static void initialize() {
    }

    private static GameRule<Boolean> register(String name) {
        return GameRuleBuilder.forBoolean(false)
                .category(GameRuleCategory.PLAYER)
                .buildAndRegister(Identifier.fromNamespaceAndPath(BrainageServerUtils.MOD_ID, name));
    }

}
