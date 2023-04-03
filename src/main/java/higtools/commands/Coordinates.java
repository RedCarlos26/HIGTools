package higtools.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Coordinates extends Command {

    public Coordinates() {
        super("coords", "Copies your coordinates to the clipboard.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            mc.keyboard.setClipboard("%d, %d, %d".formatted(mc.player.getBlockPos().getX(), mc.player.getBlockPos().getY(), mc.player.getBlockPos().getZ()));
            mc.getToastManager().add(new MeteorToast(Items.NETHERITE_PICKAXE, "Coordinates", "Copied to clipboard."));
            return SINGLE_SUCCESS;
        });
    }
}
