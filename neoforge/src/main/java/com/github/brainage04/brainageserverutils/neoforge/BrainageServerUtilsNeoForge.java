package com.github.brainage04.brainageserverutils.neoforge;

import com.github.brainage04.brainageserverutils.BrainageServerUtils;
import com.github.brainage04.brainageserverutils.command.core.ModCommands;
import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.listener.ServerTickListener;
import com.github.brainage04.brainageserverutils.platform.LoaderPlatform;
import com.github.brainage04.brainageserverutils.util.RunnableScheduler;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(BrainageServerUtils.MOD_ID)
public final class BrainageServerUtilsNeoForge implements LoaderPlatform {
    private static final DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(Registries.GAME_RULE, BrainageServerUtils.MOD_ID);
    private static final DeferredHolder<GameRule<?>, GameRule<Boolean>> DISABLE_DURABILITY = register("disable_durability");
    private static final DeferredHolder<GameRule<?>, GameRule<Boolean>> DISABLE_ITEM_DECREMENT = register("disable_item_decrement");
    private static final DeferredHolder<GameRule<?>, GameRule<Boolean>> DISABLE_BUCKET_DECREMENT = register("disable_bucket_decrement");
    private static final DeferredHolder<GameRule<?>, GameRule<Boolean>> INSTANT_CONSUME = register("instant_consume");

    public BrainageServerUtilsNeoForge(IEventBus modBus) {
        GAME_RULES.register(modBus);
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> BrainageServerUtils.initialize(this)));
    }

    private static DeferredHolder<GameRule<?>, GameRule<Boolean>> register(String name) {
        return GAME_RULES.register(name, () -> new GameRule<>(
                GameRuleCategory.PLAYER, GameRuleType.BOOL, BoolArgumentType.bool(),
                (visitor, rule) -> visitor.visitBoolean(rule), Codec.BOOL, value -> value ? 1 : 0, false, FeatureFlagSet.of()
        ));
    }

    @Override
    public void registerGameRules() {
        ModGameRules.initialize(name -> switch (name) {
            case "disable_durability" -> DISABLE_DURABILITY.get();
            case "disable_item_decrement" -> DISABLE_ITEM_DECREMENT.get();
            case "disable_bucket_decrement" -> DISABLE_BUCKET_DECREMENT.get();
            case "instant_consume" -> INSTANT_CONSUME.get();
            default -> throw new IllegalArgumentException("Unknown BrainageServerUtils gamerule: " + name);
        });
    }

    @Override
    public void registerCommands() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> ModCommands.initialize(event.getDispatcher()));
    }
    @Override
    public void registerServerLifecycle() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> ServerTickListener.onServerStarting(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> ServerTickListener.onServerStopped(event.getServer()));
    }

    @Override
    public void registerServerTick() {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre event) -> RunnableScheduler.tick(event.getServer()));
    }
}
