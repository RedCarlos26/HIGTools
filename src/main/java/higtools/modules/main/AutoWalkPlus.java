/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 * Enhanced by RedCarlos#0001
 */

package higtools.modules.main;

import higtools.HIGTools;
import baritone.api.BaritoneAPI;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.GoalDirection;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.lwjgl.glfw.GLFW;

public class AutoWalkPlus extends Module {
    public enum Mode {
        Simple,
        Smart
    }

    public enum Direction {
        Forwards,
        Backwards,
        Left,
        Right
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .description("Walking mode.")
            .defaultValue(Mode.Simple)
            .onChanged(mode1 -> {
                if (isActive()) {
                    if (mode1 == Mode.Simple) {
                        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
                        goal = null;
                    } else {
                        timer = 0;
                        createGoal();
                    }

                    unpress();
                }
            })
            .build()
    );

    private final Setting<Direction> direction = sgGeneral.add(new EnumSetting.Builder<Direction>()
            .name("simple-direction")
            .description("The direction to walk in simple mode.")
            .defaultValue(Direction.Forwards)
            .onChanged(direction1 -> {
                if (isActive()) unpress();
            })
            .visible(() -> mode.get() == Mode.Simple)
            .build()
    );

    private final Setting<Boolean> lagPause = sgGeneral.add(new BoolSetting.Builder()
            .name("lag-pause")
            .description("Whether to pause if the server is not responding.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> picktoggle = sgGeneral.add(new BoolSetting.Builder()
            .name("pickaxe-toggle")
            .description("Automatically disables AutoWalk+ when you run out of pickaxes.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> highwaytools = sgGeneral.add(new BoolSetting.Builder()
            .name("highway-tools-toggle")
            .description("Automatically disables HighwayTools when you run out of pickaxes.")
            .defaultValue(false)
            .visible(picktoggle::get)
            .build()
    );

    private int timer = 0;
    private GoalDirection goal;
    private boolean sentMessage;

    public AutoWalkPlus() {
        super(HIGTools.MAIN, "auto-walk+", "Automatically walks forward.");
    }

    @Override
    public AutoWalkPlus fromTag(NbtCompound tag) {
        super.fromTag(tag);
        if (!this.keybind.isValid()) this.keybind.set(Keybind.fromKey(GLFW.GLFW_KEY_I));
        return this;
    }

    @Override
    public void onActivate() {
        if (mode.get() == Mode.Smart) createGoal();
        sentMessage = false;
    }

    @Override
    public void onDeactivate() {
        if (mode.get() == Mode.Simple) unpress();
        else BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();

        goal = null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTick(TickEvent.Pre event) {
        float timeSinceLastTick = TickRate.INSTANCE.getTimeSinceLastTick();
        if (timeSinceLastTick >= 1.5f && lagPause.get()) {
            if (!sentMessage) error("Server is lagging, pausing AutoWalk+.");
            sentMessage = true;
            unpress();
            return;
        }

        if (mode.get() == Mode.Simple) {
            switch (direction.get()) {
                case Forwards -> setPressed(mc.options.forwardKey, true);
                case Backwards -> setPressed(mc.options.backKey, true);
                case Left -> setPressed(mc.options.leftKey, true);
                case Right -> setPressed(mc.options.rightKey, true);
            }
        } else {
            if (timer > 20) {
                timer = 0;
                goal.recalculate(mc.player.getPos());
            }

            timer++;
        }
        sentMessage = false;

        if (picktoggle.get()) {
            FindItemResult pickaxe = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);
            if (!pickaxe.found()) {
                error("No pickaxe found, disabling AutoWalk+.");
                toggle();
            }
        }

        if (highwaytools.get()) {
            FindItemResult pickaxe = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);
            if (!pickaxe.found()) {
                if (Modules.get().isActive(higtools.modules.main.HighwayTools.class)) {
                    Modules.get().get(higtools.modules.main.HighwayTools.class).toggle();
                    error("No pickaxe found, disabling HighwayTools.");
                    toggle();
                }
            }
        }
    }

    private void unpress() {
        setPressed(mc.options.forwardKey, false);
        setPressed(mc.options.backKey, false);
        setPressed(mc.options.leftKey, false);
        setPressed(mc.options.rightKey, false);
    }

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }

    private void createGoal() {
        timer = 0;
        goal = new GoalDirection(mc.player.getPos(), mc.player.getYaw());
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
    }
}
