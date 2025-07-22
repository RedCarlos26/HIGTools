package me.redcarlos.higtools.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;

public class Coordinates extends Command {
    public Coordinates() {
        super("coordinates", "Copies your coordinates to the clipboard.", "coords");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            mc.keyboard.setClipboard("%d, %d, %d".formatted(mc.player.getBlockPos().getX(), mc.player.getBlockPos().getY(), mc.player.getBlockPos().getZ()));
            MeteorToast toast = new MeteorToast.Builder("Coordinates").icon(Items.NETHERITE_PICKAXE).text("Copied to clipboard.").build();
            mc.getToastManager().add(toast);
            return SINGLE_SUCCESS;
        });
    }
}
