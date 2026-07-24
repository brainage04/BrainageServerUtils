package com.github.brainage04.brainageserverutils;

import com.github.brainage04.brainageserverutils.command.core.ModCommands;
import com.github.brainage04.brainageserverutils.gamerule.ModGameRules;
import com.github.brainage04.brainageserverutils.listener.ServerTickListener;
import com.github.brainage04.brainageserverutils.util.RunnableScheduler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrainageServerUtils implements ModInitializer {
    public static final String MOD_ID = "brainageserverutils";
    public static final String MOD_NAME = "BrainageServerUtils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("{} initialising...", MOD_NAME);

        ModGameRules.initialize();
        ModCommands.initialize();
        ServerTickListener.initialize();
        RunnableScheduler.initialize();

        LOGGER.info("{} initialised.", MOD_NAME);
    }
}