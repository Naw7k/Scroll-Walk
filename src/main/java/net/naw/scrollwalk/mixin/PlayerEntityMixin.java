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

// Mixin into Player to apply scroll-based speed changes and display the speedometer HUD
@Mixin(Player.class)
public class PlayerEntityMixin {

    // Used to calculate km/h from position delta
    @Unique private Vec3 previousPosition = null;
    @Unique private double smoothKmh = 0.0;
    // Tracks whether the speedometer was visible last tick so we can clear it when disabled
    @Unique private boolean lastWasVisible = false;

    // Morphling compat — cached via reflection so we don't do Class.forName every tick
    @Unique private static Class<?> morphStateClass = null;
    @Unique private static boolean morphlingChecked = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;
        if (ScrollWalk.config == null || !ScrollWalk.config.modEnabled) return;

        // Check once if Morphling is installed and cache the result
        if (!morphlingChecked) {
            morphlingChecked = true;
            try {
                morphStateClass = Class.forName("net.naw.morphling.client.core.MorphState");
            } catch (Exception ignored) {}
        }

        // Check if player is currently morphed — if so, Morphling owns the speed attribute
        boolean morphlingActive = false;
        if (morphStateClass != null) {
            try {
                morphlingActive = (boolean) morphStateClass.getMethod("isMorphed").invoke(null);
            } catch (Exception ignored) {}
        }

        // Always lerp momentumSpeed toward the target so Morphling can read a smooth value too
        ScrollWalk.momentumSpeed += (ScrollWalk.currentSpeed - ScrollWalk.momentumSpeed) * ScrollWalk.config.acceleration;
        if (Math.abs(ScrollWalk.momentumSpeed - ScrollWalk.currentSpeed) < 0.00001) {
            ScrollWalk.momentumSpeed = ScrollWalk.currentSpeed;
        }

        // Only apply speed to the attribute when not morphed
        // When morphed, Morphling reads ScrollWalk.momentumSpeed via ScrollWalkCompat and applies it itself
        if (!morphlingActive) {
            if (Math.abs(ScrollWalk.momentumSpeed - attribute.getBaseValue()) > 0.00001) {
                attribute.setBaseValue(ScrollWalk.momentumSpeed);
            }
        }

        // Speedometer HUD — shows current speed multiplier and km/h in the action bar
        boolean shouldShow = ScrollWalk.config.showSpeedometer;
        if (shouldShow) {
            Vec3 currentPosition = new Vec3(player.getX(), player.getY(), player.getZ());
            if (previousPosition != null) {
                double deltaX = currentPosition.x - previousPosition.x;
                double deltaZ = currentPosition.z - previousPosition.z;
                // Convert blocks/tick to km/h (20 ticks/sec * 3.6)
                smoothKmh = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20 * 3.6;
            }
            previousPosition = currentPosition;

            String state = player.isSprinting() ? "Sprinting" : (player.isCrouching() ? "Sneaking" : "Walking");
            int tempColor = 0xFFFFFF;
            if (smoothKmh > 30) tempColor = 0xFF5555;
            else if (smoothKmh > 20) tempColor = 0xFFFF55;
            final int finalColor = tempColor;
            // Display multiplier is currentSpeed * 10 so 0.1 shows as 1.0x, 0.05 shows as 0.5x etc
            double displayMultiplier = ScrollWalk.currentSpeed * 10;
            String content = String.format("Speed: %.1fx | %.1f km/h (%s)", displayMultiplier, smoothKmh, state);
            player.sendOverlayMessage(Component.literal(content).withStyle(style -> style.withColor(finalColor)));
            lastWasVisible = true;
        } else if (lastWasVisible) {
            // Clear the action bar message when speedometer is turned off
            player.sendOverlayMessage(Component.literal(""));
            lastWasVisible = false;
        }
    }
}
