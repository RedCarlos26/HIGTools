package higtools.modules.main;

import higtools.HIGTools;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.orbit.EventHandler;

public class AxisViewer extends Module {
    private final SettingGroup sgOw = settings.createGroup("Overworld");
    private final SettingGroup sgNether = settings.createGroup("Nether");
    private final SettingGroup sgEnd = settings.createGroup("End");

    // Overworld
    private final Setting<Boolean> overworld = sgOw.add(new BoolSetting.Builder()
        .name("overworld")
        .description("Displays a line on overworld axises.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> overY = sgOw.add(new IntSetting.Builder()
        .name("height")
        .description("Y position of the line.")
        .defaultValue(64)
        .sliderMin(-64)
        .sliderMax(319)
        .visible(overworld::get)
        .build()
    );

    private final Setting<Boolean> overAxis = sgOw.add(new BoolSetting.Builder()
        .name("axises")
        .description("Displays lines of axises.")
        .defaultValue(true)
        .visible(overworld::get)
        .build()
    );

    private final Setting<Boolean> overDiag = sgOw.add(new BoolSetting.Builder()
        .name("diagonals")
        .description("Displays lines of diagonals.")
        .defaultValue(false)
        .visible(overworld::get)
        .build()
    );

    private final Setting<SettingColor> overColor = sgOw.add(new ColorSetting.Builder()
        .name("color")
        .description("The line color.")
        .defaultValue(new SettingColor(73, 107, 255, 255))
        .visible(overworld::get)
        .build()
    );


    // Nether
    private final Setting<Boolean> nether = sgNether.add(new BoolSetting.Builder()
        .name("nether")
        .description("Displays a line on nether axis.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> netherY = sgNether.add(new IntSetting.Builder()
        .name("height")
        .description("Y position of the line.")
        .defaultValue(120)
        .sliderMin(0)
        .sliderMax(255)
        .visible(nether::get)
        .build()
    );

    private final Setting<Boolean> netherAxis = sgNether.add(new BoolSetting.Builder()
        .name("axises")
        .description("Displays lines of axises.")
        .defaultValue(true)
        .visible(nether::get)
        .build()
    );

    private final Setting<Boolean> netherDiag = sgNether.add(new BoolSetting.Builder()
        .name("diagonals")
        .description("Displays lines of diagonals.")
        .defaultValue(true)
        .visible(nether::get)
        .build()
    );

    private final Setting<SettingColor> netherColor = sgNether.add(new ColorSetting.Builder()
        .name("color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(nether::get)
        .build()
    );


    // End
    private final Setting<Boolean> end = sgEnd.add(new BoolSetting.Builder()
        .name("end")
        .description("Displays a line on nether axis.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> endY = sgEnd.add(new IntSetting.Builder()
        .name("height")
        .description("Y position of the line.")
        .defaultValue(64)
        .sliderMin(0)
        .sliderMax(255)
        .visible(end::get)
        .build()
    );

    private final Setting<Boolean> endAxis = sgEnd.add(new BoolSetting.Builder()
        .name("axises")
        .description("Displays lines of axises.")
        .defaultValue(true)
        .visible(end::get)
        .build()
    );

    private final Setting<Boolean> endDiag = sgEnd.add(new BoolSetting.Builder()
        .name("diagonals")
        .description("Displays lines of diagonals.")
        .defaultValue(false)
        .visible(end::get)
        .build()
    );

    private final Setting<SettingColor> endColor = sgEnd.add(new ColorSetting.Builder()
        .name("color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(end::get)
        .build()
    );

    public AxisViewer() {
        super(HIGTools.MAIN, "axis-viewer", "Render culling fucks it up :skull:");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.options.hudHidden) return;
        double length = 30_000_000;
        switch (PlayerUtils.getDimension()) {
            case Overworld -> {
                if (overworld.get()) {
                    if (overAxis.get()) {
                        event.renderer.line(0, overY.get(), 0, 0, overY.get(), length, overColor.get()); // Z+
                        event.renderer.line(0, overY.get(), 0, length, overY.get(), 0, overColor.get()); // X+
                        event.renderer.line(0, overY.get(), 0, 0, overY.get(), -length, overColor.get()); // -Z
                        event.renderer.line(0, overY.get(), 0, -length, overY.get(), 0, overColor.get()); // -X
                    }

                    if (overDiag.get()) {
                        event.renderer.line(0, overY.get(), 0, length, overY.get(), length, overColor.get());
                        event.renderer.line(0, overY.get(), 0, length, overY.get(), -length, overColor.get());
                        event.renderer.line(0, overY.get(), 0, -length, overY.get(), length, overColor.get());
                        event.renderer.line(0, overY.get(), 0, -length, overY.get(), -length, overColor.get());
                    }
                }
            }
            case Nether -> {
                if (nether.get()) {
                    if (netherAxis.get()) {
                        event.renderer.line(0, netherY.get(), 0, 0, netherY.get(), length, netherColor.get());
                        event.renderer.line(0, netherY.get(), 0, length, netherY.get(), 0, netherColor.get());
                        event.renderer.line(0, netherY.get(), 0, 0, netherY.get(), -length, netherColor.get());
                        event.renderer.line(0, netherY.get(), 0, -length, netherY.get(), 0, netherColor.get());
                    }

                    if (netherDiag.get()) {
                        event.renderer.line(0, netherY.get(), 0, length, netherY.get(), length, netherColor.get());
                        event.renderer.line(0, netherY.get(), 0, length, netherY.get(), -length, netherColor.get());
                        event.renderer.line(0, netherY.get(), 0, -length, netherY.get(), length, netherColor.get());
                        event.renderer.line(0, netherY.get(), 0, -length, netherY.get(), -length, netherColor.get());
                    }
                }
            }
            case End -> {
                if (end.get()) {
                    if (endAxis.get()) {
                        event.renderer.line(0, endY.get(), 0, 0, endY.get(), length, netherColor.get());
                        event.renderer.line(0, endY.get(), 0, length, endY.get(), 0, netherColor.get());
                        event.renderer.line(0, endY.get(), 0, 0, endY.get(), -length, netherColor.get());
                        event.renderer.line(0, endY.get(), 0, -length, endY.get(), 0, netherColor.get());
                    }

                    if (endDiag.get()) {
                        event.renderer.line(0, endY.get(), 0, length, endY.get(), length, endColor.get());
                        event.renderer.line(0, endY.get(), 0, length, endY.get(), -length, endColor.get());
                        event.renderer.line(0, endY.get(), 0, -length, endY.get(), length, endColor.get());
                        event.renderer.line(0, endY.get(), 0, -length, endY.get(), -length, endColor.get());
                    }
                }
            }
        }
    }
}
