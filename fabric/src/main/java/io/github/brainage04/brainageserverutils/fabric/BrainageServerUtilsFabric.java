package io.github.brainage04.brainageserverutils.fabric;

import io.github.brainage04.brainageserverutils.BrainageServerUtils;
import io.github.brainage04.brainageserverutils.command.core.ModCommands;
import io.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import io.github.brainage04.brainageserverutils.platform.LoaderPlatform;
import io.github.brainage04.brainageserverutils.util.CurrentServer;
import java.nio.file.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public final class BrainageServerUtilsFabric implements ModInitializer, LoaderPlatform {
    @Override
    public void onInitialize() {
        BrainageServerUtils.initialize(this);
    }

    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void registerGameRules() {
        ModGameRules.initialize(name -> GameRuleBuilder.forBoolean(false)
                .category(GameRuleCategory.PLAYER)
                .buildAndRegister(Identifier.fromNamespaceAndPath(BrainageServerUtils.MOD_ID, name)));
    }

    @Override
    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
                ModCommands.initialize(dispatcher, buildContext));
    }

    @Override
    public void registerServerLifecycle() {
        ServerLifecycleEvents.SERVER_STARTING.register(CurrentServer::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(CurrentServer::onServerStopped);
    }
}
