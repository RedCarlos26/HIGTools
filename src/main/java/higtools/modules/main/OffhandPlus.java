package higtools.modules.main;

import higtools.modules.HIGTools;
import higtools.utils.HTEntityUtils;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.*;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class OffhandPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgGap = settings.createGroup("Gap");
    private final SettingGroup sgCrystal = settings.createGroup("Crystal");
    private final SettingGroup sgMisc = settings.createGroup("Misc");


    // General
    private final Setting<Item> item = sgGeneral.add(new EnumSetting.Builder<Item>()
            .name("item")
            .description("Which item to hold in your offhand.")
            .defaultValue(Item.Crystal)
            .build()
    );

    private final Setting<Item> fallbackItem = sgGeneral.add(new EnumSetting.Builder<Item>()
            .name("fallback-item")
            .description("Which item to hold if main item is not found.")
            .defaultValue(Item.EGap)
            .build()
    );

    private final Setting<Boolean> hotbar = sgGeneral.add(new BoolSetting.Builder()
            .name("hotbar")
            .description("Whether to use items from your hotbar.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> rightClick = sgGeneral.add(new BoolSetting.Builder()
            .name("right-click")
            .description("Only holds the item in your offhand when you are holding right click.")
            .defaultValue(false)
            .build()
    );


    // Gap
    private final Setting<Boolean> allowCrapples = sgGap.add(new BoolSetting.Builder()
            .name("allow-crapples")
            .description("Holds a crapple instead of an EGap when no Egap is found.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> swordGap = sgGap.add(new BoolSetting.Builder()
            .name("sword-gap")
            .description("Holds an Enchanted Golden Apple when you are holding a sword.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> axeGap = sgGap.add(new BoolSetting.Builder()
            .name("axe-gap")
            .description("Holds an Enchanted Golden Apple when you are holding an axe.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> smartGap = sgGap.add(new BoolSetting.Builder()
            .name("smart-gap")
            .description("Only allows to hold a gap if you are in a hole.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> inSingleHole = sgGap.add(new BoolSetting.Builder()
            .name("in-single-hole")
            .description("Allow gap in a single hole.")
            .defaultValue(true)
            .visible(smartGap::get)
            .build()
    );

    private final Setting<Boolean> inSingleBedrock = sgGap.add(new BoolSetting.Builder()
            .name("in-single-bedrock")
            .description("Allow gap in a single bedrock hole.")
            .defaultValue(true)
            .visible(smartGap::get)
            .build()
    );

    private final Setting<Boolean> inDoubleHole = sgGap.add(new BoolSetting.Builder()
            .name("in-double-hole")
            .description("Allow gap in a double hole.")
            .defaultValue(true)
            .visible(smartGap::get)
            .build()
    );

    private final Setting<Boolean> inDoubleBedrock = sgGap.add(new BoolSetting.Builder()
            .name("in-double-bedrock")
            .description("Allow gap in a double bedrock hole.")
            .defaultValue(true)
            .visible(smartGap::get)
            .build()
    );


    // Crystal
    private final Setting<Boolean> crystalCa = sgCrystal.add(new BoolSetting.Builder()
            .name("crystal-on-ca")
            .description("Holds a crystal when you have Crystal Aura enabled.")
            .defaultValue(true)
            .build()
    );


    private final Setting<Boolean> crystalMine = sgCrystal.add(new BoolSetting.Builder()
            .name("crystal-on-mine")
            .description("Holds a crystal when you are mining.")
            .defaultValue(false)
            .build()
    );


    // Misc
    private final Setting<Boolean> AutoXP = sgMisc.add(new BoolSetting.Builder()
            .name("Xp-on-Auto-XP")
            .description("Holds Bottles of Enchanting when Auto XP is on.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> RocketBow = sgMisc.add(new BoolSetting.Builder()
            .name("Crossbow-rocket")
            .description("Holds a rocket if you are holding a crossbow.")
            .defaultValue(false)
            .build()
    );


    public OffhandPlus() {
        super(HIGTools.HIG, "offhand+", "Allows you to hold specified items in your offhand.");
    }


    private boolean isClicking;
    private boolean sentMessage;
    private Item currentItem;


    @Override
    public void onActivate() {
        sentMessage = false;
        isClicking = false;
        currentItem = item.get();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        net.minecraft.item.Item mainHand = mc.player.getMainHandStack().getItem();
        Modules modules = Modules.get();
        meteordevelopment.meteorclient.systems.modules.combat.AutoTotem autoTotem = modules.get(meteordevelopment.meteorclient.systems.modules.combat.AutoTotem.class);

        // Gap
        if (mainHand instanceof SwordItem && swordGap.get() && allowGap()) currentItem = Item.EGap;
        else if (mainHand instanceof  AxeItem && axeGap.get() && allowGap()) currentItem = Item.EGap;

        // Ca mining
        else if ((modules.isActive(CrystalAura.class) && crystalCa.get())
                || mc.interactionManager.isBreakingBlock() && crystalMine.get()) currentItem = Item.Crystal;

        // Xp
        else if (modules.isActive(AutoXpPlus.class) && modules.get(AutoXpPlus.class).isOnStandby && modules.get(AutoXpPlus.class).isRepairing && AutoXP.get()) currentItem = Item.Exp;
        // Rocket
        else if ((mc.player.getMainHandStack().getItem() instanceof CrossbowItem) && RocketBow.get()) currentItem = Item.Firework;
        else currentItem = item.get();

        // Checking offhand item
        if (mc.player.getOffHandStack().getItem() != currentItem.item) {
            FindItemResult item = InvUtils.find(itemStack -> itemStack.getItem() == currentItem.item, hotbar.get() ? 0 : 9, 35);

            // No offhand item
            if (!item.found()) item = InvUtils.find(itemStack -> itemStack.getItem() == fallbackItem.get().item, hotbar.get() ? 0 : 9, 35);

            if (currentItem == Item.EGap && allowCrapples.get()) {
                if (!item.found()) item = InvUtils.find(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE, hotbar.get() ? 0 : 9, 35);
            }

            if (!item.found()) {
                if (!sentMessage) {
                    warning("Chosen item not found.");
                    sentMessage = true;
                }
            }

            // Swap to offhand
            else if ((isClicking || !rightClick.get()) && (!autoTotem.isLocked()) && !item.isOffhand()) {
                InvUtils.move().from(item.slot()).toOffhand();
                sentMessage = false;
            }
        }

        // If not clicking, set to totem if auto totem is on
        else if (!isClicking && rightClick.get()) {
            if (autoTotem.isActive()) {
                FindItemResult totem = InvUtils.find(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING, hotbar.get() ? 0 : 9, 35);

                if (totem.found() && !totem.isOffhand()) {
                    InvUtils.move().from(totem.slot()).toOffhand();
                }
            } else {
                FindItemResult empty = InvUtils.find(ItemStack::isEmpty, hotbar.get() ? 0 : 9, 35);
                if (empty.found()) InvUtils.move().fromOffhand().to(empty.slot());
            }
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        isClicking = mc.currentScreen == null && (!Modules.get().get(meteordevelopment.meteorclient.systems.modules.combat.AutoTotem.class).isLocked()) && !usableItem() && !mc.player.isUsingItem() && event.action == KeyAction.Press && event.button == GLFW_MOUSE_BUTTON_RIGHT;
    }

    private boolean usableItem() {
        return mc.player.getMainHandStack().getItem() == Items.BOW
                || mc.player.getMainHandStack().getItem() == Items.TRIDENT
                || mc.player.getMainHandStack().getItem() == Items.CROSSBOW
                || mc.player.getMainHandStack().getItem().isFood();
    }

    private boolean allowGap() {
        if (!smartGap.get()) return true;
        else if (inSingleBedrock.get() && HTEntityUtils.isSurrounded(mc.player, HTEntityUtils.BlastResistantType.Unbreakable)) return true;
        else if (inSingleHole.get() && HTEntityUtils.isSurrounded(mc.player, HTEntityUtils.BlastResistantType.Any)) return true;
        else if (inDoubleBedrock.get() && HTEntityUtils.isInHole(mc.player, true, HTEntityUtils.BlastResistantType.Unbreakable)) return true;
        else if (inDoubleHole.get() && HTEntityUtils.isInHole(mc.player, true, HTEntityUtils.BlastResistantType.Any)) return true;
        return false;
    }

    @Override
    public String getInfoString() {
        return item.get().name();
    }

    public enum Item {
        Totem(Items.TOTEM_OF_UNDYING),
        EGap(Items.ENCHANTED_GOLDEN_APPLE),
        Gap(Items.GOLDEN_APPLE),
        Crystal(Items.END_CRYSTAL),
        Exp(Items.EXPERIENCE_BOTTLE),
        Obsidian(Items.OBSIDIAN),
        Firework(Items.FIREWORK_ROCKET),
        Web(Items.COBWEB),
        Shield(Items.SHIELD);

        net.minecraft.item.Item item;

        Item(net.minecraft.item.Item item) {
            this.item = item;
        }
    }
}
