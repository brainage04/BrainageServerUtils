package com.github.brainage04.brainageserverutils.config.core;

import com.github.brainage04.brainageserverutils.BrainageServerUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

// final fields will crash the game when saving
@SuppressWarnings("CanBeFinal")
@Config(name = BrainageServerUtils.MOD_ID)
public class ModConfig implements ConfigData {
    public static boolean test = true;
}
