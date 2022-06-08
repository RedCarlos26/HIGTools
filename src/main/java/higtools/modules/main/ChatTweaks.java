package higtools.modules.main;

import higtools.modules.HIGTools;
import higtools.utils.EmoteUtils;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class ChatTweaks extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> emotes = sgGeneral.add(new BoolSetting.Builder()
            .name("emotes")
            .description("Enables the HIG emote system.")
            .defaultValue(false)
            .build()
    );

    private final Setting<String> prefix = sgGeneral.add(new StringSetting.Builder()
            .name("prefix")
            .description("What to be displayed as HIG Tools Prefix")
            .defaultValue("HIG Tools")
            .build()
    );

    private final Setting<SettingColor> prefixColors = sgGeneral.add(new ColorSetting.Builder()
            .name("prefix-color")
            .description("Color display for the prefix")
            .defaultValue(new SettingColor(145, 61, 226, 255))
            .build()
    );

    private final Setting<String> leftBracket = sgGeneral.add(new StringSetting.Builder()
            .name("left-bracket")
            .description("What to be displayed as left bracket for the prefix")
            .defaultValue("[")
            .build()
    );

    private final Setting<String> rightBracket = sgGeneral.add(new StringSetting.Builder()
            .name("right-bracket")
            .description("What to be displayed as right bracket for the prefix")
            .defaultValue("]")
            .build()
    );

    public ChatTweaks() {
        super(HIGTools.HIG, "chat-tweaks", "Various chat tweaks.");
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;
        if (emotes.get()) message = EmoteUtils.applyEmotes(message);
        event.message = message;
    }

    @Override
    public void onActivate() {
        ChatUtils.registerCustomPrefix("higtools.modules", this::getPrefix);
    }

    public LiteralText getPrefix() {
        BaseText logo = new LiteralText(prefix.get());
        LiteralText prefix = new LiteralText("");
        logo.setStyle(logo.getStyle().withColor(TextColor.fromRgb(prefixColors.get().getPacked())));
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY));
        prefix.append(leftBracket.get());
        prefix.append(logo);
        prefix.append(rightBracket.get() + " ");
        return prefix;
    }
}
