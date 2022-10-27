package higtools.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import meteordevelopment.meteorclient.utils.world.TickRate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HIGUtils {

    // Armor Notify
    public static boolean checkNotifyThreshold(ItemStack i, double threshold) {
        return getArmorDamage(i) <= threshold;
    }

    public static double getArmorDamage(ItemStack i) {
        return (((double) (i.getMaxDamage() - i.getDamage()) / i.getMaxDamage()) * 100);
    }

    public static boolean isHelmetArmor(ItemStack itemStack) {
        if (itemStack == null) return false;
        Item item = itemStack.getItem();
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.HEAD;
    }

    public static boolean isChestplateArmor(ItemStack itemStack) {
        if (itemStack == null) return false;
        Item item = itemStack.getItem();
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.CHEST;    }

    public static boolean isLeggingsArmor(ItemStack itemStack) {
        if (itemStack == null) return false;
        Item item = itemStack.getItem();
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.LEGS;    }

    public static boolean isBootsArmor(ItemStack itemStack) {
        if (itemStack == null) return false;
        Item item = itemStack.getItem();
        return item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.FEET;    }

    // Player
    public static double distanceFromPlayerEye(Entity entity) {
        double feet = distanceFromPlayerEye(entity.getX(), entity.getY(), entity.getZ());
        double head = distanceFromPlayerEye(entity.getX(), entity.getY() + entity.getHeight(), entity.getZ());
        return Math.min(head, feet);
    }

    public static double distanceFromPlayerEye(double x, double y, double z) {
        double f = (mc.player.getX() - x);
        double g = (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) - y);
        double h = (mc.player.getZ() - z);
        return Math.sqrt(f * f + g * g + h * h);
    }

    // Server
    public static double getServerTPSMatch(boolean TPSSync) {
        return TPSSync ? (TickRate.INSTANCE.getTickRate() / 20) : 1;
    }
}
