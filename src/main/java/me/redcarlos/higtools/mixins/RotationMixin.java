package me.redcarlos.higtools.mixins;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Rotation.class, remap = false)
public abstract class RotationMixin extends Module {

    public RotationMixin(Category category, String name, String description) {
        super(category, name, description);
    }

    @Unique
    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if (event.screen instanceof DisconnectedScreen) {
            toggle();
        }
    }

    @Unique
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        toggle();
    }
}
