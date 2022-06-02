package higtools.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class DeathEvent {
    public PlayerEntity player;
    public String name;
    public Vec3d pos;

    public static class KillEvent extends DeathEvent {
        private static final KillEvent INSTANCE = new KillEvent();

        public static KillEvent get(PlayerEntity player, Vec3d pos) {
            INSTANCE.player = player;
            INSTANCE.name = player.getEntityName();
            INSTANCE.pos = pos;
            return INSTANCE;
        }
    }

}
