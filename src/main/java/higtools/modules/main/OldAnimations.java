package higtools.modules.main;

import higtools.modules.HIGTools;
import higtools.events.UpdateHeldItemEvent;
import higtools.mixins.HeldItemRendererAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public class OldAnimations extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showSwapping = sgGeneral.add(new BoolSetting.Builder().name("show-swapping").description("Whether or not to show the item swapping animation.").defaultValue(true).build());

    private static int slotMainHand = 0;

    public OldAnimations() {
        super(HIGTools.HIG, "old-animations", "Changes inventory close and hit animations to those of 1.8");
    }

    @EventHandler
    private void onUpdateHeldItem(UpdateHeldItemEvent event) {
        event.setCancelled(true);
        HeldItemRendererAccessor heldItemRenderer = ((HeldItemRendererAccessor) mc.getHeldItemRenderer());
        ItemStack mainHandStack = mc.player.getMainHandStack();
        ItemStack offHandStack = mc.player.getOffHandStack();

        heldItemRenderer.setPrevEquipProgressMainHand(heldItemRenderer.getEquipProgressMainHand());
        heldItemRenderer.setPrevEquipProgressOffHand(heldItemRenderer.getEquipProgressOffHand());

        if (mc.player.isRiding()) {
            heldItemRenderer.setEquipProgressMainHand(MathHelper.clamp(heldItemRenderer.getEquipProgressMainHand() - 0.4F, 0.0F, 1.0F));
            heldItemRenderer.setEquipProgressOffHand(MathHelper.clamp(heldItemRenderer.getEquipProgressOffHand() - 0.4F, 0.0F, 1.0F));
        } else {
            boolean reequipM = showSwapping.get() && shouldCauseReequipAnimation(heldItemRenderer.getMainHand(), mainHandStack, mc.player.getInventory().selectedSlot);
            boolean reequipO = showSwapping.get() && shouldCauseReequipAnimation(heldItemRenderer.getOffHand(), offHandStack, -1);
            if (!reequipM && !Objects.equals(heldItemRenderer.getMainHand(), mainHandStack)) heldItemRenderer.setMainHand(mainHandStack);
            if (!reequipO && !Objects.equals(heldItemRenderer.getMainHand(), offHandStack)) heldItemRenderer.setOffHand(offHandStack);
            heldItemRenderer.setEquipProgressMainHand(heldItemRenderer.getEquipProgressMainHand() + MathHelper.clamp((!reequipM ? 1.0F : 0.0F) - heldItemRenderer.getEquipProgressMainHand(), -0.4F, 0.4F));
            heldItemRenderer.setEquipProgressOffHand(heldItemRenderer.getEquipProgressOffHand() + MathHelper.clamp((!reequipO ? 1.0F : 0.0F) - heldItemRenderer.getEquipProgressOffHand(), -0.4F, 0.4F));
        }
        if (heldItemRenderer.getPrevEquipProgressMainHand() < 0.1F) heldItemRenderer.setMainHand(mainHandStack);
        if (heldItemRenderer.getEquipProgressOffHand() < 0.1F) heldItemRenderer.setOffHand(offHandStack);
    }

    private boolean shouldCauseReequipAnimation(ItemStack from, ItemStack to, int slot) {
        boolean fromInvalid = from.isEmpty();
        boolean toInvalid = to.isEmpty();
        if (fromInvalid && toInvalid) return false;
        if (fromInvalid || toInvalid) return true;
        if (slot != -1) slotMainHand = slot;
        return !from.equals(to);
    }
}
