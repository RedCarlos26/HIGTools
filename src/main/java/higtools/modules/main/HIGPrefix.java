package higtools.modules.main;

import higtools.HIGTools;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class HIGPrefix extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> prefix = sgGeneral.add(new StringSetting.Builder()
        .name("prefix")
        .description("What to be displayed as HIGTools Prefix.")
        .defaultValue("HIGTools")
        .onChanged(reload -> setPrefixes())
        .build()
    );

    private final Setting<SettingColor> prefixColor = sgGeneral.add(new ColorSetting.Builder()
        .name("prefix-color")
        .description("Color display for the prefix.")
        .defaultValue(new SettingColor(145, 61, 226, 255))
        .build()
    );

    public HIGPrefix() {
        super(HIGTools.MAIN, "hig-prefix", "Set a prefix for the HighwayTools modules toggles.");
    }

    @Override
    public void onActivate() {
        setPrefixes();
    }

    @Override
    public void onDeactivate() {
        ChatUtils.unregisterCustomPrefix("higtools.modules");
    }

    public void setPrefixes() {
        if (isActive()) {
            ChatUtils.registerCustomPrefix("higtools.modules", this::getPrefix);
        }
    }

    public Text getPrefix() {
        MutableText value = Text.literal(prefix.get());
        MutableText prefix = Text.literal("");
        value.setStyle(value.getStyle().withColor(TextColor.fromRgb(prefixColor.get().getPacked())));
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY))
            .append(Text.literal("["))
            .append(value)
            .append(Text.literal("] "));
        return prefix;
    }
}
