package com.github.brainage04.brainageserverutils.fabric;

import com.github.brainage04.brainageserverutils.BrainageServerUtils;
import com.github.brainage04.brainageserverutils.command.core.ModCommands;
import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.listener.ServerTickListener;
import com.github.brainage04.brainageserverutils.platform.LoaderPlatform;
import com.github.brainage04.brainageserverutils.util.RunnableScheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public final class BrainageServerUtilsFabric implements ModInitializer, LoaderPlatform {
    @Override
    public void onInitialize() {
        BrainageServerUtils.initialize(this);
    }

    @Override
    public void registerGameRules() {
        ModGameRules.initialize(name -> GameRuleBuilder.forBoolean(false)
                .category(GameRuleCategory.PLAYER)
                .buildAndRegister(Identifier.fromNamespaceAndPath(BrainageServerUtils.MOD_ID, name)));
    }

    @Override
    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> ModCommands.initialize(dispatcher));
    }
    @Override
    public void registerServerLifecycle() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerTickListener::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(ServerTickListener::onServerStopped);
    }

    @Override
    public void registerServerTick() {
        ServerTickEvents.START_SERVER_TICK.register(RunnableScheduler::tick);
    }
}
