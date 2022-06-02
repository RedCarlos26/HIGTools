package higtools.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HTArmorUtils {

public static boolean checkThreshold(ItemStack i, double threshold) {
    return getDamage(i) <= threshold;
    }
public static double getDamage(ItemStack i) { return (((double) (i.getMaxDamage() - i.getDamage()) / i.getMaxDamage()) * 100); }
public static ItemStack getArmor(int slot) {return mc.player.getInventory().armor.get(slot);}


public static boolean isHelm(ItemStack itemStack) {
    if (itemStack == null) return false;
    Item i = itemStack.getItem();
    return i == Items.NETHERITE_HELMET || i == Items.DIAMOND_HELMET || i == Items.GOLDEN_HELMET || i == Items.IRON_HELMET || i == Items.CHAINMAIL_HELMET || i == Items.LEATHER_HELMET;
    }

public static boolean isChest(ItemStack itemStack) {
    if (itemStack == null) return false;
    Item i = itemStack.getItem();
    return i == Items.NETHERITE_CHESTPLATE || i == Items.DIAMOND_CHESTPLATE || i == Items.GOLDEN_CHESTPLATE || i == Items.IRON_CHESTPLATE || i == Items.CHAINMAIL_CHESTPLATE || i == Items.LEATHER_CHESTPLATE;
    }

public static boolean isLegs(ItemStack itemStack) {
    if (itemStack == null) return false;
    Item i = itemStack.getItem();
    return i == Items.NETHERITE_LEGGINGS || i == Items.DIAMOND_LEGGINGS || i == Items.GOLDEN_LEGGINGS || i == Items.IRON_LEGGINGS || i == Items.CHAINMAIL_LEGGINGS || i == Items.LEATHER_LEGGINGS;
    }

public static boolean isBoots(ItemStack itemStack) {
    if (itemStack == null) return false;
    Item i = itemStack.getItem();
    return i == Items.NETHERITE_BOOTS || i == Items.DIAMOND_BOOTS || i == Items.GOLDEN_BOOTS || i == Items.IRON_BOOTS || i == Items.CHAINMAIL_BOOTS || i == Items.LEATHER_BOOTS;
    }
}
