package com.github.brainage04.brainageserverutils.platform;

public interface LoaderPlatform {
    void registerGameRules();

    void registerCommands();

    void registerServerLifecycle();

    void registerServerTick();
}
