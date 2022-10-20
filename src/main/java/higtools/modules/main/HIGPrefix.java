package higtools.modules.main;

import higtools.HIGTools;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Formatting;
import net.minecraft.text.*;

public class HIGPrefix extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> prefix = sgGeneral.add(new StringSetting.Builder()
            .name("prefix")
            .description("What to be displayed as HIG Tools Prefix.")
            .defaultValue("HIGTools")
            .build()
    );

    private final Setting<SettingColor> prefixColors = sgGeneral.add(new ColorSetting.Builder()
            .name("prefix-color")
            .description("Color display for the prefix.")
            .defaultValue(new SettingColor(145, 61, 226, 255))
            .build()
    );

    public HIGPrefix() {
        super(HIGTools.MAIN, "HIG-prefix", "Set a prefix for the HIG Tools modules toggles.");
    }

    @Override
    public void onActivate() {ChatUtils.registerCustomPrefix("higtools.modules", this::getPrefix);}

    @Override
    public void onDeactivate() {ChatUtils.unregisterCustomPrefix("higtools.modules");}

    public Text getPrefix() {
        MutableText value = Text.literal(prefix.get());
        MutableText prefix = Text.literal("");
        value.setStyle(value.getStyle().withColor(TextColor.fromRgb(prefixColors.get().getPacked())));
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY));
        prefix.append(Text.literal("["));
        prefix.append(value);
        prefix.append(Text.literal("] "));
        return prefix;
    }
}
