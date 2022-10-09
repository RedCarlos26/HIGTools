package higtools.commands;

import higtools.modules.borers.*;
import higtools.modules.kmain.*;
import higtools.modules.main.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.*;
import meteordevelopment.meteorclient.systems.modules.movement.*;
import meteordevelopment.meteorclient.systems.modules.player.*;
import meteordevelopment.meteorclient.systems.modules.render.*;
import meteordevelopment.meteorclient.systems.modules.world.*;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ToggleModules extends Command {
    public ToggleModules() {
        super("toggle-modules", "Disables active modules.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("higtools").executes(ctx -> {
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
        }));

        builder.then(literal("all").executes(ctx -> {
            new ArrayList<>(Modules.get().getActive()).forEach(Module::toggle);

            return SINGLE_SUCCESS;
        }));
    }
}
