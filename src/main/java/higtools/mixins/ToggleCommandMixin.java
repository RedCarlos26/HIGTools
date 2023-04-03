package higtools.mixins;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import higtools.modules.borers.*;
import higtools.modules.main.*;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.commands.commands.ToggleCommand;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoLog;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.world.LiquidFiller;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

@Mixin(value = ToggleCommand.class, remap = false)
public abstract class ToggleCommandMixin extends Command {
    public ToggleCommandMixin() {
        super("toggle", "Toggles a module.", "t");
    }

    /**
     * Borers & HighwayBuilder
     */
    private final List<Class<? extends Module>> borerClasses = List.of(
        HighwayBuilderPlus.class,
        AxisBorer.class,
        NegNegBorer.class,
        NegPosBorer.class,
        PosNegBorer.class,
        PosPosBorer.class,
        RingRoadBorer.class
    );

    /**
     * HighwayTools Modules
     */
    private final List<Class<? extends Module>> higToolsClasses = List.of(
        AutoEatPlus.class,
        AutoLog.class,
        FreeLook.class,
        HandManager.class,
        InvManager.class,
        LiquidFiller.class,
        Rotation.class,
        SafeWalk.class,
        ScaffoldPlus.class
    );

    @Inject(method = "build", at = @At("HEAD"))
    private void inject(LiteralArgumentBuilder<CommandSource> builder, CallbackInfo ci) {
        builder.then(literal("higtools")
            .executes(context -> {
                Modules modules = Modules.get();

                // Highway Tools
                modules.get(HighwayTools.class).toggle();

                // Borers & HighwayBuilder
                borerClasses.forEach(borer -> modules.get(borer).toggle());

                // HighwayTools Modules
                higToolsClasses.forEach(higTool -> modules.get(higTool).toggle());

                return SINGLE_SUCCESS;
            })
            .then(literal("on")
                .executes(context -> {
                    Modules modules = Modules.get();

                    // Highway Tools
                    if (!modules.get(HighwayTools.class).isActive()) modules.get(HighwayTools.class).toggle();

                    // Borers & HighwayBuilder
                    borerClasses.stream()
                        .filter(borer -> !modules.get(borer).isActive())
                        .forEach(borer -> modules.get(borer).toggle());

                    // HighwayTools Modules
                    higToolsClasses.stream()
                        .filter(higTool -> !modules.get(higTool).isActive())
                        .forEach(higTool -> modules.get(higTool).toggle());

                    return SINGLE_SUCCESS;
                })
            ).then(literal("off")
                .executes(context -> {
                    Modules modules = Modules.get();

                    // Highway Tools
                    if (modules.get(HighwayTools.class).isActive()) modules.get(HighwayTools.class).toggle();

                    // Borers & HighwayBuilder
                    borerClasses.stream()
                        .filter(borer -> modules.get(borer).isActive())
                        .forEach(borer -> modules.get(borer).toggle());

                    // HighwayTools Modules
                    higToolsClasses.stream()
                        .filter(higTool -> modules.get(higTool).isActive())
                        .forEach(higTool -> modules.get(higTool).toggle());

                    return SINGLE_SUCCESS;
                })
            )
        );
    }
}
