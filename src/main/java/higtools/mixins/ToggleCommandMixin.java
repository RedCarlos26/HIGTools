package higtools.mixins;

import higtools.modules.borers.*;
import higtools.modules.kmain.AutoEatPlus;
import higtools.modules.kmain.InvManager;
import higtools.modules.kmain.ScaffoldPlus;
import higtools.modules.main.HandManager;
import higtools.modules.main.HighwayBuilderPlus;
import higtools.modules.main.HighwayTools;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoLog;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.world.LiquidFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.commands.commands.ToggleCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

@Mixin(value = ToggleCommand.class, remap = false)
public abstract class ToggleCommandMixin extends Command {
    public ToggleCommandMixin () {
        super("toggle", "Toggles a module.", "t");
    }

    @Inject(method = "build", at = @At("HEAD"))
        private void inject(LiteralArgumentBuilder<CommandSource> builder, CallbackInfo ci) {
        builder
            .then(literal("higtools")
                .executes(context -> {
                    Modules modules = Modules.get();

                    // Highway Tools
                    modules.get(HighwayTools.class).toggle();

                    // Borers & HighwayBuilder
                    modules.get(HighwayBuilderPlus.class).toggle();
                    modules.get(AxisBorer.class).toggle();
                    modules.get(NegNegBorer.class).toggle();
                    modules.get(NegPosBorer.class).toggle();
                    modules.get(PosNegBorer.class).toggle();
                    modules.get(PosPosBorer.class).toggle();
                    modules.get(RingRoadBorer.class).toggle();

                    // HighwayTools Modules
                    modules.get(AutoEatPlus.class).toggle();
                    modules.get(AutoLog.class).toggle();
                    modules.get(FreeLook.class).toggle();
                    modules.get(HandManager.class).toggle();
                    modules.get(InvManager.class).toggle();
                    modules.get(LiquidFiller.class).toggle();
                    modules.get(Rotation.class).toggle();
                    modules.get(SafeWalk.class).toggle();
                    modules.get(ScaffoldPlus.class).toggle();

                    return SINGLE_SUCCESS;
                })
                .then(literal("on")
                    .executes(context -> {
                        Modules modules = Modules.get();

                        // Highway Tools
                        if (!modules.get(HighwayTools.class).isActive())
                            modules.get(HighwayTools.class).toggle();

                        // Borers & HighwayBuilder
                        if (!modules.get(HighwayBuilderPlus.class).isActive())
                            modules.get(HighwayBuilderPlus.class).toggle();
                        if (!modules.get(AxisBorer.class).isActive())
                            modules.get(AxisBorer.class).toggle();
                        if (!modules.get(NegNegBorer.class).isActive())
                            modules.get(NegNegBorer.class).toggle();
                        if (!modules.get(NegPosBorer.class).isActive())
                            modules.get(NegPosBorer.class).toggle();
                        if (!modules.get(PosNegBorer.class).isActive())
                            modules.get(PosNegBorer.class).toggle();
                        if (!modules.get(PosPosBorer.class).isActive())
                            modules.get(PosPosBorer.class).toggle();
                        if (!modules.get(RingRoadBorer.class).isActive())
                            modules.get(RingRoadBorer.class).toggle();

                        // HighwayTools Modules
                        if (!modules.get(AutoEatPlus.class).isActive())
                            modules.get(AutoEatPlus.class).toggle();
                        if (!modules.get(AutoLog.class).isActive())
                            modules.get(AutoLog.class).toggle();
                        if (!modules.get(FreeLook.class).isActive())
                            modules.get(FreeLook.class).toggle();
                        if (!modules.get(HandManager.class).isActive())
                            modules.get(HandManager.class).toggle();
                        if (!modules.get(InvManager.class).isActive())
                            modules.get(InvManager.class).toggle();
                        if (!modules.get(LiquidFiller.class).isActive())
                            modules.get(LiquidFiller.class).toggle();
                        if (!modules.get(Rotation.class).isActive())
                            modules.get(Rotation.class).toggle();
                        if (!modules.get(SafeWalk.class).isActive())
                            modules.get(SafeWalk.class).toggle();
                        if (!modules.get(ScaffoldPlus.class).isActive())
                            modules.get(ScaffoldPlus.class).toggle();

                        return SINGLE_SUCCESS;
                    })
                ).then(literal("off")
                    .executes(context -> {
                        Modules modules = Modules.get();

                        // Highway Tools
                        if (modules.get(HighwayTools.class).isActive())
                            modules.get(HighwayTools.class).toggle();

                        // Borers & HighwayBuilder
                        if (modules.get(HighwayBuilderPlus.class).isActive())
                            modules.get(HighwayBuilderPlus.class).toggle();
                        if (modules.get(AxisBorer.class).isActive())
                            modules.get(AxisBorer.class).toggle();
                        if (modules.get(NegNegBorer.class).isActive())
                            modules.get(NegNegBorer.class).toggle();
                        if (modules.get(NegPosBorer.class).isActive())
                            modules.get(NegPosBorer.class).toggle();
                        if (modules.get(PosNegBorer.class).isActive())
                            modules.get(PosNegBorer.class).toggle();
                        if (modules.get(PosPosBorer.class).isActive())
                            modules.get(PosPosBorer.class).toggle();
                        if (modules.get(RingRoadBorer.class).isActive())
                            modules.get(RingRoadBorer.class).toggle();

                        // HighwayTools Modules
                        if (modules.get(AutoEatPlus.class).isActive())
                            modules.get(AutoEatPlus.class).toggle();
                        if (modules.get(AutoLog.class).isActive())
                            modules.get(AutoLog.class).toggle();
                        if (modules.get(FreeLook.class).isActive())
                            modules.get(FreeLook.class).toggle();
                        if (modules.get(HandManager.class).isActive())
                            modules.get(HandManager.class).toggle();
                        if (modules.get(InvManager.class).isActive())
                            modules.get(InvManager.class).toggle();
                        if (modules.get(LiquidFiller.class).isActive())
                            modules.get(LiquidFiller.class).toggle();
                        if (modules.get(Rotation.class).isActive())
                            modules.get(Rotation.class).toggle();
                        if (modules.get(SafeWalk.class).isActive())
                            modules.get(SafeWalk.class).toggle();
                        if (modules.get(ScaffoldPlus.class).isActive())
                            modules.get(ScaffoldPlus.class).toggle();

                        return SINGLE_SUCCESS;
                    })
                )
            );
    }
}
