package com.github.brainage04.brainageserverutils;

import com.github.brainage04.brainageserverutils.command.core.ModCommands;
import com.github.brainage04.brainageserverutils.config.core.ModConfig;
import com.github.brainage04.brainageserverutils.key.core.ModKeys;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrainageServerUtils implements ClientModInitializer {
	public static final String MOD_ID = "brainageserverutils";
	public static final String MOD_NAME = "BrainageServerUtils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("{} initialising...", MOD_NAME);

		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		ModCommands.initialize();
		ModKeys.initialize();

		LOGGER.info("{} initialised.", MOD_NAME);
	}
}