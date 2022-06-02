package higtools.events;

import meteordevelopment.meteorclient.events.Cancellable;

public class UpdateHeldItemEvent extends Cancellable {
    public static final UpdateHeldItemEvent INSTANCE = new UpdateHeldItemEvent();

    public static UpdateHeldItemEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
