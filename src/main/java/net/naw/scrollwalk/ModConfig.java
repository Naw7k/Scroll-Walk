package net.naw.scrollwalk;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

// Defines the configuration file named "scrollwalk.json"
@Config(name = "scrollwalk")
public class ModConfig implements ConfigData {
    // Toggles the mod functionality on or off
    public boolean modEnabled = true;
    // Determines if the speed display is shown on screen
    public boolean showSpeedometer = false;
    // The current walking speed set by the user
    public float scrollSpeed = 0.1f;
    // The maximum allowable walking speed
    public float maxSpeed = 0.1f;
    // The minimum allowable walking speed
    public float minSpeed = 0.05f;
    // The amount the speed changes with each scroll tick
    public float scrollStep = 0.02f;
    // Controls how quickly the speed transitions to the target value
    public float acceleration = 0.1f;
}
