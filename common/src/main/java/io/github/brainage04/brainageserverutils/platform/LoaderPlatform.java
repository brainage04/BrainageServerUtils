package io.github.brainage04.brainageserverutils.platform;

import java.nio.file.Path;

public interface LoaderPlatform {
    Path configDirectory();

    void registerGameRules();

    void registerCommands();

    void registerServerLifecycle();
}
