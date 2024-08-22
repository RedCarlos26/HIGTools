package me.redcarlos.higtools.modules.hud;

import me.redcarlos.higtools.HIGTools;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;

public class TextPresets {
    public static final HudElementInfo<TextHud> INFO = new HudElementInfo<>(HIGTools.HUD, "higtools-presets", "Displays text with Starscript.", TextPresets::create);

    private static TextHud create() {
        return new TextHud(INFO);
    }

    static {
        // These use a different and more accurate rounding method than Meteor's, do not remove
        addPreset("Player Coords", "XYZ: #1{roundToString(player.pos.x, 1)}, {floor(player.pos.y)}, {roundToString(player.pos.z, 1)}");
        addPreset("Opposite Coords", "{player.opposite_dimension != \"End\" ? player.opposite_dimension + \":\" : \"\"} #1{player.opposite_dimension != \"End\" ? \"\" + floor(player.opposite_dim_pos.x) + \", \" + floor(player.opposite_dim_pos.z) : \"\"}");

        addPreset("Welcome Hud", "Welcome to HIG Tools, #1{meteor.is_module_active(\"name-protect\") ?  meteor.get_module_setting(\"name-protect\", \"name\") : player._toString}");
        addPreset("KM/H Speed", "Speed: #1{roundToString(player.speed * 3.6, 1)} km/h");
    }

    private static void addPreset(String title, String text) {
        INFO.addPreset(title, textHud -> {
            if (text != null) textHud.text.set(text);
            textHud.updateDelay.set(0);
        });
    }
}
