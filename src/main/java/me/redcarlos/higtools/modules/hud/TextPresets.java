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
        addPreset("KM/H Speed", "Speed: #1{roundToString(player.speed*3.6, 1)} km/h");
    }

    private static void addPreset(String title, String text) {
        INFO.addPreset(title, textHud -> {
            if (text != null) textHud.text.set(text);
            textHud.updateDelay.set(0);
        });
    }
}
