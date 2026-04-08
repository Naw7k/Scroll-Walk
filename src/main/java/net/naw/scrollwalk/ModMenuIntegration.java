package net.naw.scrollwalk;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

// Handles the creation of the configuration screen for ModMenu
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            // Creates the builder for the configuration screen
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Settings"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            // Creates the general category in settings
            ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
            // Loads the current configuration instance
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

            // 1. MOD ENABLED - Toggles the whole mod
            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Mod Enabled"), config.modEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> {
                        config.modEnabled = newValue;
                        // Reset speed immediately when mod is disabled
                        if (!newValue) {
                            ScrollWalk.currentSpeed = 0.1;
                        }
                    })
                    .build());

            // 2. SHOW SPEEDOMETER - Toggles HUD display
            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Show Speedometer"), config.showSpeedometer)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> config.showSpeedometer = newValue)
                    .build());

            // 3. MAX SPEED LIMIT - Slider for maximum possible speed
            general.addEntry(entryBuilder.startIntSlider(Component.literal("Max Speed Limit"), (int)(config.maxSpeed * 100), 1, 10)
                    .setDefaultValue(10)
                    .setTextGetter(value -> Component.literal(String.format("%.1fx", value / 10.0f)))
                    .setSaveConsumer(newValue -> config.maxSpeed = newValue / 100f)
                    .build());

            // 4. MIN SPEED LIMIT - Slider for minimum possible speed
            general.addEntry(entryBuilder.startIntSlider(Component.literal("Min Speed Limit"), (int)(config.minSpeed * 100), 1, 10)
                    .setDefaultValue(5)
                    .setTextGetter(value -> Component.literal(String.format("%.1fx", value / 10.0f)))
                    .setSaveConsumer(newValue -> config.minSpeed = newValue / 100f)
                    .build());

            // 5. SCROLL STEP - How much speed changes per scroll notch
            general.addEntry(entryBuilder.startIntSlider(Component.literal("Scroll Step"), (int)(config.scrollStep * 100), 1, 5)
                    .setDefaultValue(2)
                    .setTextGetter(value -> Component.literal(value + "%"))
                    .setSaveConsumer(newValue -> config.scrollStep = newValue / 100f)
                    .build());

            // 6. ACCELERATION - Controls how snappy or gradual the speed transition feels
            general.addEntry(entryBuilder.startIntSlider(Component.literal("Acceleration"), (int)(config.acceleration * 100), 1, 100)
                    .setDefaultValue(10)
                    .setTextGetter(value -> Component.literal(value + "%"))
                    .setSaveConsumer(newValue -> config.acceleration = newValue / 100f)
                    .build());

            // Saves the config file when the screen is closed
            builder.setSavingRunnable(() -> AutoConfig.getConfigHolder(ModConfig.class).save());

            return builder.build();
        };
    }
}
