package highwaytools.modules.main;

import highwaytools.HighwayTools;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Formatting;
import net.minecraft.text.*;

public class ChatPrefix extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> prefix = sgGeneral.add(new StringSetting.Builder()
            .name("prefix")
            .description("What to be displayed as HighwayTools Prefix.")
            .defaultValue("HighwayTools")
            .build()
    );

    private final Setting<SettingColor> prefixColors = sgGeneral.add(new ColorSetting.Builder()
            .name("prefix-color")
            .description("Color display for the prefix.")
            .defaultValue(new SettingColor(145, 61, 226, 255))
            .build()
    );

    public ChatPrefix() {
        super(HighwayTools.MAIN, "chat-prefix", "Set a prefix for the HighwayTools modules toggles.");
    }

    @Override
    public void onActivate() {ChatUtils.registerCustomPrefix("highwaytools.modules", this::getPrefix);}

    @Override
    public void onDeactivate() {ChatUtils.unregisterCustomPrefix("highwaytools.modules");}

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
