package higtools.modules.main;

import higtools.modules.HIGTools;
import higtools.utils.server.HTServerUtils;
import higtools.utils.TimerUtils;
import higtools.utils.HTPlayerUtils;
import baritone.api.BaritoneAPI;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

public class KillAuraPlus extends Module {
    public enum Weapon {
        Sword,
        Axe,
        Both,
        Any
    }

    public enum RotationMode {
        Always,
        OnHit,
        None
    }

    public enum RotateTo {
        Head,
        Body,
        Feet
    }

    public enum DelayMode {
        Vanilla,
        Fixed,
        Custom
    }


    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgDelay = settings.createGroup("Delay");


    // General
    private final Setting<Weapon> weapon = sgGeneral.add(new EnumSetting.Builder<Weapon>()
            .name("weapons")
            .description("Only attacks an entity when a specified item is in your hand.")
            .defaultValue(Weapon.Both)
            .build()
    );

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-switch")
            .description("Switches to your selected weapon when attacking the target.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> onlyOnClick = sgGeneral.add(new BoolSetting.Builder()
            .name("only-on-click")
            .description("Only attacks when hold left click.")
            .defaultValue(false)
            .build()
    );

    private final Setting<RotationMode> rotation = sgGeneral.add(new EnumSetting.Builder<RotationMode>()
            .name("rotate")
            .description("Determines when you should rotate towards the target.")
            .defaultValue(RotationMode.Always)
            .build()
    );

    private final Setting<RotateTo> rotateTo = sgGeneral.add(new EnumSetting.Builder<RotateTo>()
            .name("rotate-to")
            .description("Where to rotate to when you are hitting the target.")
            .defaultValue(RotateTo.Body)
            .visible(() -> rotation.get() != RotationMode.None)
            .build()
    );

    private final Setting<Boolean> ghostSwing = sgGeneral.add(new BoolSetting.Builder()
            .name("ghost-swing")
            .description("Hides your hand swing server side.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> pauseOnCombat = sgGeneral.add(new BoolSetting.Builder()
            .name("pause-on-combat")
            .description("Freezes Baritone temporarily until you are finished attacking the entity.")
            .defaultValue(true)
            .build()
    );


    // Targeting
    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgTargeting.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Entities to attack.")
            .defaultValue(new Object2BooleanOpenHashMap<>(0))
            .onlyAttackable()
            .build()
    );

    private final Setting<Double> range = sgTargeting.add(new DoubleSetting.Builder()
            .name("range")
            .description("The maximum range the entity can be to attack it.")
            .defaultValue(4.5)
            .range(0,6)
            .sliderRange(0,6)
            .build()
    );

    private final Setting<Double> wallsRange = sgTargeting.add(new DoubleSetting.Builder()
            .name("walls-range")
            .description("The maximum range the entity can be attacked through walls.")
            .defaultValue(4.5)
            .range(0,6)
            .sliderRange(0,6)
            .build()
    );

    private final Setting<SortPriority> priority = sgTargeting.add(new EnumSetting.Builder<SortPriority>()
            .name("priority")
            .description("How to filter targets within range.")
            .defaultValue(SortPriority.LowestHealth)
            .build()
    );

    private final Setting<Integer> maxTargets = sgTargeting.add(new IntSetting.Builder()
            .name("max-targets")
            .description("How many entities to target at once.")
            .defaultValue(1)
            .min(1)
            .sliderRange(1, 10)
            .build()
    );

    private final Setting<Boolean> babies = sgTargeting.add(new BoolSetting.Builder()
            .name("babies")
            .description("Whether or not to attack baby variants of the entity.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> nametagged = sgTargeting.add(new BoolSetting.Builder()
            .name("nametagged")
            .description("Whether or not to attack mobs with a name tag.")
            .defaultValue(false)
            .build()
    );


    // Delay
    private final Setting<DelayMode> delayMode = sgDelay.add(new EnumSetting.Builder<DelayMode>()
            .name("delay-mode")
            .description("Mode to use for the delay to attack.")
            .defaultValue(DelayMode.Fixed)
            .build()
    );

    private final Setting<Integer> hitDelay = sgDelay.add(new IntSetting.Builder()
            .name("hit-delay")
            .description("How fast you hit the entity in ticks.")
            .defaultValue(0)
            .min(0)
            .sliderMax(60)
            .visible(() -> delayMode.get() == DelayMode.Custom)
            .build()
    );

    private final Setting<Boolean> TPSSync = sgDelay.add(new BoolSetting.Builder()
            .name("TPS-sync")
            .description("Tries to sync attack delay with the server's TPS.")
            .defaultValue(false)
            .visible(() -> delayMode.get() != DelayMode.Vanilla)
            .build()
    );

    private final Setting<Integer> switchDelay = sgDelay.add(new IntSetting.Builder()
            .name("switch-delay")
            .description("How many ticks to wait before hitting an entity after switching hotbar slots.")
            .defaultValue(0)
            .min(0)
            .sliderMax(10)
            .build()
    );


    public KillAuraPlus() {
        super(HIGTools.HIG, "kill-aura+", "Kill Aura with various improvements.");
    }


    private final List<Entity> targets = new ArrayList<>();
    private int hitDelayTimer, switchTimer;
    private final TimerUtils fixedHitTimer = new TimerUtils();
    private boolean wasPathing;


    @Override
    public void onDeactivate() {
        hitDelayTimer = 0;
        targets.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!mc.player.isAlive() || PlayerUtils.getGameMode() == GameMode.SPECTATOR) return;

        TargetUtils.getList(targets, this::entityCheck, priority.get(), maxTargets.get());

        if (targets.isEmpty()) {
            if (wasPathing) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
                wasPathing = false;
            }
            return;
        }
        // Changed
        if (pauseOnCombat.get() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing() && !wasPathing) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
            wasPathing = true;
        }

        Entity primary = targets.get(0);

        if (rotation.get() == RotationMode.Always) rotate(primary, null);

        if (onlyOnClick.get() && !mc.options.attackKey.isPressed()) return;

        if (autoSwitch.get()) {
            FindItemResult weaponResult = InvUtils.findInHotbar(itemStack -> {
                Item item = itemStack.getItem();

                return switch (weapon.get()) {
                    case Axe -> item instanceof AxeItem;
                    case Sword -> item instanceof SwordItem;
                    case Both -> item instanceof AxeItem || item instanceof SwordItem;
                    default -> true;
                };
            });

            InvUtils.swap(weaponResult.slot(), false);
        }

        if (!itemInHand()) return;

        if (delayCheck()) targets.forEach(this::attack);
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            switchTimer = switchDelay.get();
        }

        if (event.packet instanceof HandSwingC2SPacket && ghostSwing.get()) event.cancel();
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) return false;
        if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;
        if (HTPlayerUtils.distanceFromEye(entity) > range.get()) return false;
        if (!entities.get().getBoolean(entity.getType())) return false;
        if (!nametagged.get() && entity.hasCustomName()) return false;
        if (!PlayerUtils.canSeeEntity(entity) && HTPlayerUtils.distanceFromEye(entity) > wallsRange.get()) return false;
        if (entity instanceof PlayerEntity) {
            if (((PlayerEntity) entity).isCreative()) return false;
            if (!Friends.get().shouldAttack((PlayerEntity) entity)) return false;
        }
        return !(entity instanceof AnimalEntity) || babies.get() || !((AnimalEntity) entity).isBaby();
    }

    private boolean delayCheck() {
        if (switchTimer > 0) {
            switchTimer--;
            return false;
        }

        switch (delayMode.get()) {
            case Vanilla -> {
                return mc.player.getAttackCooldownProgress(0.5f) >= 1;
            }

            case Custom -> {
                if (hitDelayTimer > 0) {
                    hitDelayTimer--;
                    return false;
                } else {
                    hitDelayTimer = (int) (hitDelay.get() / HTServerUtils.getTPSMatch(TPSSync.get()));
                    return true;
                }
            }

            case Fixed -> {
                if (fixedHitTimer.passedMillis((long) (fixedDelay() / HTServerUtils.getTPSMatch(TPSSync.get())))) return true;
            }
        }

        return false;
    }

    private void attack(Entity target) {
        if (rotation.get() == RotationMode.OnHit) rotate(target, () -> hitEntity(target));
        else hitEntity(target);
    }

    private void hitEntity(Entity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        fixedHitTimer.reset();
    }

    private void rotate(Entity target, Runnable callback) {
        Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target, switch (rotateTo.get()) {
            case Head -> Target.Head;
            case Body -> Target.Body;
            case Feet -> Target.Feet;
        }), callback);
    }

    private boolean itemInHand() {
        return switch (weapon.get()) {
            case Axe -> mc.player.getMainHandStack().getItem() instanceof AxeItem;
            case Sword -> mc.player.getMainHandStack().getItem() instanceof SwordItem;
            case Both -> mc.player.getMainHandStack().getItem() instanceof AxeItem || mc.player.getMainHandStack().getItem() instanceof SwordItem;
            default -> true;
        };
    }

    private long fixedDelay() {
        Item activeItem = mc.player.getMainHandStack().getItem();

        if (activeItem instanceof SwordItem) return 625;
        else if (activeItem instanceof TridentItem) return (long) (1000 / 1.1);
        else if (activeItem instanceof PickaxeItem) return (long) (1000 / 1.2);
        else if (activeItem instanceof ShovelItem
                || activeItem == Items.GOLDEN_AXE || activeItem == Items.DIAMOND_AXE || activeItem == Items.NETHERITE_AXE
                || activeItem == Items.WOODEN_HOE || activeItem == Items.GOLDEN_HOE) return 1000;
        else if (activeItem == Items.WOODEN_AXE || activeItem == Items.STONE_AXE) return 1250;
        else if (activeItem == Items.IRON_AXE) return (long) (1000 / 0.9);
        else if (activeItem == Items.STONE_HOE) return 500;
        else if (activeItem == Items.IRON_HOE) return 1000 / 3;
        return 250;
    }

    public Entity getTarget() {
        if (!targets.isEmpty()) return targets.get(0);
        return null;
    }

    @Override
    public String getInfoString() {
        if (!targets.isEmpty()) {
            Entity targetFirst = targets.get(0);
            if (targetFirst instanceof PlayerEntity) return targetFirst.getEntityName();
            return targetFirst.getType().getName().getString();
        }
        return null;
    }
}
