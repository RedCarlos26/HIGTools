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

import java.util.List;

@Mixin(value = ToggleCommand.class, remap = false)
public abstract class ToggleCommandMixin extends Command {
    public ToggleCommandMixin(String name, String description, String... aliases) {
        super(name, description, aliases);
    }

    /**
     * Borers & HighwayBuilder
     */
    @Unique
    private final List<Class<? extends Module>> borerClasses = List.of(
        HighwayBuilderPlus.class,
        AxisBorer.class,
        NegNegBorer.class,
        NegPosBorer.class,
        PosNegBorer.class,
        PosPosBorer.class
    );

    /**
     * HighwayTools Modules
     */
    @Unique
    private final List<Class<? extends Module>> higToolsClasses = List.of(
        AutoCenter.class,
        AutoLog.class,
        AutoWalkHig.class,
        FreeLook.class,
        OffhandManager.class,
        HotbarManager.class,
        LiquidFillerHig.class,
        RotationLock.class,
        SafeWalk.class,
        ScaffoldPlus.class
    );

    @Inject(method = "build", at = @At("HEAD"))
    private void inject(LiteralArgumentBuilder<CommandSource> builder, CallbackInfo ci) {
        builder.then(literal("higtools").then(literal("off").executes(context -> {
            Modules modules = Modules.get();

            if (modules.get(HighwayTools.class).isActive()) modules.get(HighwayTools.class).toggle();

            borerClasses.stream().filter(borer -> modules.get(borer).isActive()).forEach(borer -> modules.get(borer).toggle());

            higToolsClasses.stream().filter(higTool -> modules.get(higTool).isActive()).forEach(higTool -> modules.get(higTool).toggle());

            return SINGLE_SUCCESS;
        })));
    }
}
