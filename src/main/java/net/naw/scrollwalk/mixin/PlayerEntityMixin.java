package net.naw.scrollwalk.mixin;

import net.naw.scrollwalk.ScrollWalk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin into PlayerEntity to modify player movement behavior
@Mixin(Player.class)
public class PlayerEntityMixin {

    // Unique fields for tracking state across ticks
    @Unique
    private Vec3 previousPosition = null;
    @Unique
    private double smoothKmh = 0.0;
    @Unique
    private boolean lastWasVisible = false;

    // The "Internal" speed that slides toward the target to create smooth acceleration
    @Unique
    private double momentumSpeed = 0.1;

    // Inject into the tick method to run code every game tick
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // 1. MOMENTUM LOGIC - Smoothly interpolate current speed towards target speed
        // Using ScrollWalk instead of ScrollSpeed
        momentumSpeed += (ScrollWalk.currentSpeed - momentumSpeed) * ScrollWalk.config.acceleration;

        // Apply the calculated momentum speed to the player's movement attribute
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.setBaseValue(momentumSpeed);
        }

        // 2. Speedometer Logic - Calculates and displays current speed HUD
        boolean shouldShow = ScrollWalk.config != null && ScrollWalk.config.modEnabled && ScrollWalk.config.showSpeedometer;

        if (shouldShow) {
            Vec3 currentPosition = new Vec3(player.getX(), player.getY(), player.getZ());

            // Calculate speed based on distance moved between ticks
            if (previousPosition != null) {
                double deltaX = currentPosition.x - previousPosition.x;
                double deltaZ = currentPosition.z - previousPosition.z;
                // Convert blocks per tick to kilometers per hour
                smoothKmh = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20 * 3.6;
            }
            previousPosition = currentPosition;

            // Determine current movement state for display
            String state = player.isSprinting() ? "Sprinting" : (player.isCrouching() ? "Sneaking" : "Walking");

            // Change color based on speed severity
            int tempColor = 0xFFFFFF; // White
            if (smoothKmh > 30) tempColor = 0xFF5555; // Red
            else if (smoothKmh > 20) tempColor = 0xFFFF55; // Yellow

            final int finalColor = tempColor;

            // Target speed display multiplier
            double displayMultiplier = ScrollWalk.currentSpeed * 10;

            String content = String.format("Speed: %.1fx | %.1f km/h (%s)", displayMultiplier, smoothKmh, state);

            // Send message to action bar (true = hotbar message)
            player.sendOverlayMessage(Component.literal(content).withStyle(style -> style.withColor(finalColor)));
            lastWasVisible = true;
        } else if (lastWasVisible) {
            // Clear message when speedometer is disabled
            player.sendOverlayMessage(Component.literal(""));
            lastWasVisible = false;
        }
    }
}
