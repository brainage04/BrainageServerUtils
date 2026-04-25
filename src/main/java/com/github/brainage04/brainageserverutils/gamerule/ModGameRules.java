package com.github.brainage04.brainageserverutils.gamerule;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.TridentItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

import java.util.ArrayList;
import java.util.List;

public class ModGameRules {
    private static final int TRIDENT_MIN_DRAW_DURATION_OLD = TridentItem.MIN_DRAW_DURATION;

    public static void updateAttackCooldowns(MinecraftServer server, GameRules.BooleanRule rule) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            modifyAttackSpeed(rule.get(), player);
        }
    }

    public static void updateTridentCooldown(MinecraftServer server) {
        if (server.getGameRules().getBoolean(ModGameRules.INSTANT_SHOOT)) {
            TridentItem.MIN_DRAW_DURATION = 0;
        } else {
            TridentItem.MIN_DRAW_DURATION = TRIDENT_MIN_DRAW_DURATION_OLD;
        }
    }

    public static void modifyAttackSpeed(boolean noCooldown, ServerPlayerEntity player) {
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.ATTACK_SPEED);
        if (attribute == null) return;

        if (noCooldown) {
            attribute.setBaseValue(24);
        } else {
            player.getAttributes().resetToBaseValue(EntityAttributes.ATTACK_SPEED);
        }
    }

    public static void initialize() {

    }

    public static final List<GameRules.Key<?>> REGISTERED = new ArrayList<>();

    private static <T extends GameRules.Rule<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type) {
        GameRules.Key<T> key = GameRuleRegistry.register(name, category, type);
        REGISTERED.add(key);
        return key;
    }

    public static final GameRules.Key<GameRules.BooleanRule> DISABLE_DURABILITY =
            register("disableDurability", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanRule> DISABLE_ITEM_DECREMENT =
            register("disableItemDecrement", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanRule> DISABLE_BUCKET_DECREMENT =
            register("disableBucketDecrement", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanRule> DISABLE_IFRAMES =
            register("disableIFrames", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanRule> FASTER_EFFECT_DAMAGE_TICKING =
            register("fasterEffectDamageTicking", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanRule> DISABLE_ATTACK_COOLDOWN =
            register("disableAttackCooldown", GameRules.Category.PLAYER,
                    GameRuleFactory.createBooleanRule(false, ModGameRules::updateAttackCooldowns));

    public static final GameRules.Key<GameRules.BooleanRule> INSTANT_SHOOT =
            register("instantShoot", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanRule> INSTANT_CONSUME =
            register("instantConsume", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
}