package me.redcarlos.higtools.mixins;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Rotation.class, remap = false)
public abstract class RotationMixin extends Module {
    @Unique
    private Setting<Boolean> toggleOnLeave;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        SettingGroup sgGeneral = settings.getDefaultGroup();

        toggleOnLeave = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-on-leave")
            .description("Toggle the module off when you leave a world.")
            .defaultValue(false)
            .build()
        );
    }

    public RotationMixin() {
        super(Categories.Player, "rotation", "Changes/locks your yaw and pitch.");
    }

    @Unique
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!toggleOnLeave.get() || !isActive()) return;

        toggle();
    }

    @Unique
    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if (!toggleOnLeave.get() || !isActive()) return;
        if (!(event.screen instanceof DisconnectedScreen)) return;

        toggle();
    }
}
