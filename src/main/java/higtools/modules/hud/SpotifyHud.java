package higtools.modules.hud;

import higtools.modules.HIGTools;
import higtools.utils.SpotifyService;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;

public class SpotifyHud extends HudElement {
    public static final HudElementInfo<SpotifyHud> INFO = new HudElementInfo<>(HIGTools.HUD, "spotify-HUD", "Display the current song playing in spotify.", SpotifyHud::new);


    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    // General
    private final Setting<String> format = sgGeneral.add(new StringSetting.Builder()
        .name("format")
        .description("The format for the hud.")
        .defaultValue("Playing {track} - {artist}")
        .build()
    );

    public SpotifyHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        double width = 0;
        double height = 0;

        String t;

        if (!SpotifyService.isSpotifyRunning) {
            t = "Spotify is not running.";
        } else if (SpotifyService.currentTrack == null || SpotifyService.currentArtist == null) {
            t = "No song playing.";
        } else {
            t = "Playing " + SpotifyService.currentTrack + " - " + SpotifyService.currentArtist;
        }

        width = Math.max(width, renderer.textWidth(t));
        box.setSize((width), renderer.textHeight());
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;

        if (isInEditor()) {
            renderer.text("Spotify HUD", x, y, TextHud.getSectionColor(0), true);
            return;
        }

        String t;
        if (SpotifyService.hasMedia()) t = format.get().replace("{track}", SpotifyService.currentTrack).replace("{artist}", SpotifyService.currentArtist);
        else t = "No song playing.";

        renderer.text(t, x, y, TextHud.getSectionColor(0), true);
    }
}
