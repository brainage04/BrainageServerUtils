package io.github.brainage04.brainageserverutils.gamerule;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gamerules.GameRule;

public final class RuleChecks {
    private RuleChecks() {
    }

    /// False on the client side, where game rules are not authoritative.
    public static boolean isEnabledFor(Entity entity, GameRule<Boolean> rule) {
        return entity.level() instanceof ServerLevel level && level.getGameRules().get(rule);
    }
}
