package com.github.brainage04.brainageserverutils;

import com.github.brainage04.brainageserverutils.platform.LoaderPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BrainageServerUtils {
    public static final String MOD_ID = "brainageserverutils";
    public static final String MOD_NAME = "BrainageServerUtils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private BrainageServerUtils() {
    }

    public static void initialize(LoaderPlatform platform) {
        LOGGER.info("{} initialising...", MOD_NAME);
        platform.registerGameRules();
        platform.registerCommands();
        platform.registerServerLifecycle();
        platform.registerServerTick();
        LOGGER.info("{} initialised.", MOD_NAME);
    }
}