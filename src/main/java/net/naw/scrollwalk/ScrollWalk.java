package net.naw.scrollwalk;

import net.fabricmc.api.ModInitializer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

// Main initializer class for the mod
public class ScrollWalk implements ModInitializer {
    // Stores the current calculated walking speed
    public static double currentSpeed = 0.1;
    // Holds the configuration instance for accessing settings
    public static ModConfig config;

    @Override
    public void onInitialize() {
        // Registers the configuration class with AutoConfig to handle saving/loading
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        // Loads the config instance into the static variable
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        // Sets the initial speed based on the config file
        currentSpeed = config.scrollSpeed;
    }
}