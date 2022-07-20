package higtools.utils;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
        Item i = itemStack.getItem();
        return i == Items.NETHERITE_HELMET || i == Items.DIAMOND_HELMET || i == Items.GOLDEN_HELMET || i == Items.IRON_HELMET || i == Items.CHAINMAIL_HELMET || i == Items.LEATHER_HELMET;
    }

    public static boolean isChestplateArmor(ItemStack itemStack) {
        if (itemStack == null) return false;
        Item i = itemStack.getItem();
        return i == Items.NETHERITE_CHESTPLATE || i == Items.DIAMOND_CHESTPLATE || i == Items.GOLDEN_CHESTPLATE || i == Items.IRON_CHESTPLATE || i == Items.CHAINMAIL_CHESTPLATE || i == Items.LEATHER_CHESTPLATE;
    }

    public static boolean isLeggingsArmor(ItemStack itemStack) {
        if (itemStack == null) return false;
        Item i = itemStack.getItem();
        return i == Items.NETHERITE_LEGGINGS || i == Items.DIAMOND_LEGGINGS || i == Items.GOLDEN_LEGGINGS || i == Items.IRON_LEGGINGS || i == Items.CHAINMAIL_LEGGINGS || i == Items.LEATHER_LEGGINGS;
    }

    public static boolean isBootsArmor(ItemStack itemStack) {
        if (itemStack == null) return false;
        Item i = itemStack.getItem();
        return i == Items.NETHERITE_BOOTS || i == Items.DIAMOND_BOOTS || i == Items.GOLDEN_BOOTS || i == Items.IRON_BOOTS || i == Items.CHAINMAIL_BOOTS || i == Items.LEATHER_BOOTS;
    }


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
