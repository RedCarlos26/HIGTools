package higtools.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Disconnect extends Command {

    public Disconnect() {
        super("disconnect", "Disconnects you from the server.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("Disconnected via .disconnect command.")));
            return SINGLE_SUCCESS;
        });
    }
}
