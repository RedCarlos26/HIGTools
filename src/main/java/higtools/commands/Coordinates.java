package higtools.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Coordinates extends Command {

    public Coordinates() { super("coords", "Copies your coordinates to the clipboard."); }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            mc.keyboard.setClipboard(mc.player.getBlockPos().getX() + ", " + mc.player.getBlockPos().getY() + ", " + mc.player.getBlockPos().getZ());
            mc.getToastManager().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, Text.of("HIGTools"), Text.of("Coordinates copied to clipboard.")));
            return SINGLE_SUCCESS;
        });
    }
}
