package higtools.modules.main;

import higtools.HIGTools;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
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
        switch (PlayerUtils.getDimension()) {
            case Overworld -> drawLines(event, overworld.get(), overAxis.get(), overDiag.get(), overY.get(), overColor.get());
            case Nether -> drawLines(event, nether.get(), netherAxis.get(), netherDiag.get(), netherY.get(), netherColor.get());
            case End -> drawLines(event, end.get(), endAxis.get(), endDiag.get(), endY.get(), endColor.get());
        }
    }

    private void drawLines(Render3DEvent event, boolean dimension, boolean axis, boolean diag, int y, SettingColor color) {
        if (!dimension) return;

        if (axis) {
            event.renderer.line(0, y, 0, 0, y, 30_000_000, color); // Z+
            event.renderer.line(0, y, 0, 30_000_000, y, 0, color); // X+
            event.renderer.line(0, y, 0, 0, y, -30_000_000, color); // -Z
            event.renderer.line(0, y, 0, -30_000_000, y, 0, color); // -X
        }

        if (diag) {
            event.renderer.line(0, y, 0, 30_000_000, y, 30_000_000, color); // ++
            event.renderer.line(0, y, 0, 30_000_000, y, -30_000_000, color); // +-
            event.renderer.line(0, y, 0, -30_000_000, y, 30_000_000, color); // -+
            event.renderer.line(0, y, 0, -30_000_000, y, -30_000_000, color); // --
        }
    }
}
