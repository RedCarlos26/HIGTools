package higtools.modules.main;

import higtools.HIGTools;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.stream.IntStream;

import static higtools.utils.HIGUtils.csToPs;


public class InvManager extends Module {
    private final Identifier[] itemIds = IntStream.range(0, 9)
        .mapToObj(i -> new Identifier(""))
        .toArray(Identifier[]::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup slotsGroup = settings.createGroup("Slots", false);

    private final Setting<String> slot0 = slotsGroup.add(new StringSetting.Builder()
        .name("slot0")
        .defaultValue("")
        .visible(() -> false)
        .onChanged(cur -> {
            itemIds[0] = new Identifier(cur);
            System.out.printf("<HIGTools/InvManager> Loaded %s into slot0\n", itemIds[0]);
        })
        .build()
    );

    private final Setting<String> slot1 = slotsGroup.add(new StringSetting.Builder()
        .name("slot1")
        .defaultValue("")
        .visible(() -> false)
        .onChanged(cur -> {
            itemIds[1] = new Identifier(cur);
            System.out.printf("<HIGTools/InvManager> Loaded %s into slot1\n", itemIds[1]);
        })
        .build()
    );

    private final Setting<String> slot2 = slotsGroup.add(new StringSetting.Builder()
        .name("slot2")
        .defaultValue("")
        .visible(() -> false)
        .onChanged(cur -> {
            itemIds[2] = new Identifier(cur);
            System.out.printf("<HIGTools/InvManager> Loaded %s into slot2\n", itemIds[2]);
        })
        .build()
    );
    private final Setting<String> slot3 = slotsGroup.add(new StringSetting.Builder()
        .name("slot3")
        .defaultValue("")
        .visible(() -> false)
        .onChanged(cur -> {
            itemIds[3] = new Identifier(cur);
            System.out.printf("<HIGTools/InvManager> Loaded %s into slot3\n", itemIds[3]);
        })
        .build()
    );
    private final Setting<String> slot4 = slotsGroup.add(new StringSetting.Builder()
        .name("slot4")
        .defaultValue("")
        .visible(() -> false)
        .onChanged(cur -> {
            itemIds[4] = new Identifier(cur);
            System.out.printf("<HIGTools/InvManager> Loaded %s into slot4\n", itemIds[4]);
        })
        .build()
    );
    private final Setting<String> slot5 = slotsGroup.add(new StringSetting.Builder()
        .name("slot5")
        .defaultValue("")
        .visible(() -> false)
        .onChanged(cur -> {
            itemIds[5] = new Identifier(cur);
            System.out.printf("<HIGTools/InvManager> Loaded %s into slot5\n", itemIds[5]);
        })
        .build()
    );
    private final Setting<String> slot6 = slotsGroup.add(new StringSetting.Builder()
        .name("slot6")
        .defaultValue("")
        .visible(() -> false)
        .onChanged(cur -> {
            itemIds[6] = new Identifier(cur);
            System.out.printf("<HIGTools/InvManager> Loaded %s into slot6\n", itemIds[6]);
        })
        .build()
    );
    private final Setting<String> slot7 = slotsGroup.add(new StringSetting.Builder()
        .name("slot7")
        .defaultValue("")
        .visible(() -> false)
        .onChanged(cur -> {
            itemIds[7] = new Identifier(cur);
            System.out.printf("<HIGTools/InvManager> Loaded %s into slot7\n", itemIds[7]);
        })
        .build()
    );
    private final Setting<String> slot8 = slotsGroup.add(new StringSetting.Builder()
        .name("slot8")
        .defaultValue("")
        .visible(() -> false)
        .onChanged(cur -> {
            itemIds[8] = new Identifier(cur);
            System.out.printf("<HIGTools/InvManager> Loaded %s into slot8\n", itemIds[8]);
        })
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in ticks between moving items.")
        .defaultValue(1)
        .range(1, 35)
        .sliderRange(1, 35)
        .build()
    );

    private final Setting<Boolean> reset = sgGeneral.add(new BoolSetting.Builder()
        .name("reset")
        .description("Toggle this to reset, too lazy to make proper thing.")
        .defaultValue(false)
        .visible(() -> true)
        .onChanged(val -> reset())
        .build()
    );

    private final Setting<Boolean> save = sgGeneral.add(new BoolSetting.Builder()
        .name("save")
        .description("Toggle this to save, too lazy to make proper thing.")
        .defaultValue(true)
        .visible(() -> true)
        .onChanged(val -> save())
        .build()
    );

    public InvManager() {
        super(HIGTools.MAIN, "InvManager", "Automatically organize your inventory.");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        System.out.printf("<HIGTools/InvManager> %s%n", Arrays.toString(itemIds));
    }

    @EventHandler
    public void tick(TickEvent.Pre event) {
        if (mc.player.age % delay.get() != 0) return;

        for (int i = 1; i <= 8; i++) {
            if (itemIds[i].toString().replace("minecraft:", "").equals("")) continue;
            if (!Registries.ITEM.getId(mc.player.getInventory().getStack(i).getItem()).equals(itemIds[i])) {
                for (int j = 9; j <= 35; j++) {
                    if (Registries.ITEM.getId(mc.player.getInventory().getStack(j).getItem()).equals(itemIds[i])) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, csToPs(j), 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, csToPs(i), 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, csToPs(j), 0, SlotActionType.PICKUP, mc.player);
                        mc.interactionManager.tick();
                        break;
                    }
                }
            }
        }
    }

    private void reset() {
        slot0.set("");
        slot1.set("");
        slot2.set("");
        slot3.set("");
        slot4.set("");
        slot5.set("");
        slot6.set("");
        slot7.set("");
        slot8.set("");
    }

    private void save() {
        if (mc.player == null) return;

        slot0.set(Registries.ITEM.getId(mc.player.getInventory().getStack(0).getItem()).toString());
        slot1.set(Registries.ITEM.getId(mc.player.getInventory().getStack(1).getItem()).toString());
        slot2.set(Registries.ITEM.getId(mc.player.getInventory().getStack(2).getItem()).toString());
        slot3.set(Registries.ITEM.getId(mc.player.getInventory().getStack(3).getItem()).toString());
        slot4.set(Registries.ITEM.getId(mc.player.getInventory().getStack(4).getItem()).toString());
        slot5.set(Registries.ITEM.getId(mc.player.getInventory().getStack(5).getItem()).toString());
        slot6.set(Registries.ITEM.getId(mc.player.getInventory().getStack(6).getItem()).toString());
        slot7.set(Registries.ITEM.getId(mc.player.getInventory().getStack(7).getItem()).toString());
        slot8.set(Registries.ITEM.getId(mc.player.getInventory().getStack(8).getItem()).toString());

        mc.inGameHud.getChatHud().addMessage(
            Text.of("<%sHIGTools/InvManager%s> Saved inventory -> %s".formatted(
                Formatting.LIGHT_PURPLE,
                Formatting.RESET,
                Arrays.toString(itemIds).replace("minecraft:", ""))
            )
        );
    }
}
