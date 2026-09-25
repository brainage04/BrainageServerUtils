package io.github.brainage04.brainageserverutils.neoforge;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import io.github.brainage04.brainageserverutils.BrainageServerUtils;
import io.github.brainage04.brainageserverutils.command.core.ModCommands;
import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.platform.LoaderPlatform;
import io.github.brainage04.brainageserverutils.util.CurrentServer;
import java.nio.file.Path;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(BrainageServerUtils.MOD_ID)
public final class BrainageServerUtilsNeoForge implements LoaderPlatform {
    private final DeferredRegister<GameRule<?>> gameRules = DeferredRegister.create(Registries.GAME_RULE, BrainageServerUtils.MOD_ID);
    private final IEventBus modBus;

    public BrainageServerUtilsNeoForge(IEventBus modBus) {
        this.modBus = modBus;
        BrainageServerUtils.initialize(this);
    }

    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    /// Rules are created up front so common code can hold them directly; the registry receives them later.
    @Override
    public void registerGameRules() {
        ModGameRules.initialize(name -> {
            GameRule<Boolean> rule = new GameRule<>(
                    GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(),
                    (visitor, gameRule) -> visitor.visitBoolean(gameRule), Codec.BOOL, value -> value ? 1 : 0, false, FeatureFlagSet.of()
            );
            gameRules.register(name, () -> rule);
            return rule;
        });
        gameRules.register(modBus);
    }

    @Override
    public void registerCommands() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                ModCommands.initialize(event.getDispatcher(), event.getBuildContext()));
    }

    @Override
    public void registerServerLifecycle() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> CurrentServer.onServerStarting(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> CurrentServer.onServerStopped(event.getServer()));
    }
}
