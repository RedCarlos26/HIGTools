package me.redcarlos.higtools.mixins;

import me.redcarlos.higtools.system.HIGSystem;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Systems.class, remap = false)
public abstract class SystemsMixin {
    @Shadow
    private static System<?> add(System<?> system) {
        throw new AssertionError();
    }

    @Inject(method = "init", at = @At("HEAD"))
    private static void injectSystem(CallbackInfo ci) {
        System<?> higSystem = add(new HIGSystem());
        higSystem.init();
        higSystem.load();
    }
}
