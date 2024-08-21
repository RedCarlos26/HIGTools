package me.redcarlos.higtools.mixins;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.redcarlos.higtools.modules.highwayborers.*;
import me.redcarlos.higtools.modules.main.*;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.commands.ToggleCommand;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoLog;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@SuppressWarnings("unchecked")
@Mixin(value = ToggleCommand.class, remap = false)
public abstract class ToggleCommandMixin extends Command {
    public ToggleCommandMixin(String name, String description, String... aliases) {
        super(name, description, aliases);
    }

    @Unique
    private final Class<? extends Module>[] borerModules = new Class[]{
        AxisBorer.class,
        NegNegBorer.class,
        NegPosBorer.class,
        PosNegBorer.class,
        PosPosBorer.class
    };

    @Unique
    private final Class<? extends Module>[] otherModules = new Class[]{
        AutoCenter.class,
        AutoLog.class,
        AutoWalkHIG.class,
        FreeLook.class,
        HighwayBuilderHIG.class,
        HighwayTools.class,
        HotbarManager.class,
        LiquidFillerHIG.class,
        OffhandManager.class,
        SafeWalk.class,
        ScaffoldHIG.class,
    };

    @Inject(method = "build", at = @At("HEAD"))
    private void inject(LiteralArgumentBuilder<CommandSource> builder, CallbackInfo ci) {
        builder.then(literal("higtools").then(literal("off").executes(context -> {
            Modules modules = Modules.get();

            Arrays.stream(borerModules).forEach(module -> {
                if (!modules.get(module).isActive()) return;
                modules.get(module).toggle();
            });

            Arrays.stream(otherModules).forEach(module -> {
                if (!modules.get(module).isActive()) return;
                modules.get(module).toggle();
            });

            return SINGLE_SUCCESS;
        })));
    }
}
