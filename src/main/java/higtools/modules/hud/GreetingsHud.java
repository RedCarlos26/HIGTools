package higtools.modules.hud;

import higtools.modules.HIGTools;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;

import java.util.Calendar;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GreetingsHud extends HudElement {
    public static final HudElementInfo<GreetingsHud> INFO = new HudElementInfo<>(HIGTools.HUD, "greetings-hud", "Display a friendly welcome to HIGTools.", GreetingsHud::new);

    public enum Mode {
        Normal,
        Custom
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("What text to show for the greeting.")
        .defaultValue(Mode.Normal)
        .build()
    );

    private final Setting<String> morningGreeting = sgGeneral.add(new StringSetting.Builder()
        .name("morning-greeting")
        .description("What to display as a greeting during morning hours.")
        .defaultValue("Good Morning")
        .visible(() -> mode.get() == Mode.Normal)
        .build()
    );

    private final Setting<String> afternoonGreeting = sgGeneral.add(new StringSetting.Builder()
        .name("afternoon-greeting")
        .description("What to display as a greeting during afternoon hours.")
        .defaultValue("Good Afternoon")
        .visible(() -> mode.get() == Mode.Normal)
        .build()
    );

    private final Setting<String> eveningGreeting = sgGeneral.add(new StringSetting.Builder()
        .name("evening-greeting")
        .description("What to display as a greeting during evening hours.")
        .defaultValue("Good Evening")
        .visible(() -> mode.get() == Mode.Normal)
        .build()
    );

    private final Setting<String> customGreeting = sgGeneral.add(new StringSetting.Builder()
        .name("custom-greeting")
        .description("What the greeting should say.")
        .defaultValue("Welcome to HIGTools")
        .visible(() -> mode.get() == Mode.Custom)
        .build()
    );


    public GreetingsHud() {
        super(INFO);
    }


    private String leftText;
    private String rightText;

    private double leftWidth;


    @Override
    public void tick(HudRenderer renderer) {
        Calendar calendar = Calendar.getInstance();
        int localTime = calendar.get(Calendar.HOUR_OF_DAY);

        if (mode.get() == Mode.Custom) {
            leftText = customGreeting.get();
        } else {
            if (localTime <= 12) leftText = morningGreeting.get();
            if (localTime >= 13 && localTime <= 16) leftText = afternoonGreeting.get();
            if (localTime >= 17) leftText = eveningGreeting.get();
        }

        leftText = leftText + ", ";
        rightText = Modules.get().get(NameProtect.class).getName(mc.getSession().getUsername());

        leftWidth = renderer.textWidth(leftText);
        double rightWidth = renderer.textWidth(rightText);

        box.setSize((leftWidth + rightWidth), renderer.textHeight());
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;

        if (isInEditor()) {
            renderer.text("GreetingsHud", x, y, TextHud.getSectionColor(0), true);
            return;
        }

        renderer.text(leftText, x, y, TextHud.getSectionColor(0), true);
        renderer.text(rightText, x + leftWidth, y, TextHud.getSectionColor(1), true);
    }
}
