/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 * Enhanced by RedCarlos26
 */

package me.redcarlos.higtools.modules.main;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import me.redcarlos.higtools.HIGTools;
import me.redcarlos.higtools.utils.HIGUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.systems.modules.player.AutoTool;
import meteordevelopment.meteorclient.systems.modules.player.InstantRebreak;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.HorizontalDirection;
import meteordevelopment.meteorclient.utils.misc.MBlockPos;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.EmptyBlockView;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("ConstantConditions")
public class HighwayBuilderHIG extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDigging = settings.createGroup("Digging");
    private final SettingGroup sgPaving = settings.createGroup("Paving");
    private final SettingGroup sgInventory = settings.createGroup("Inventory");
    private final SettingGroup sgRenderDigging = settings.createGroup("Render Digging");
    private final SettingGroup sgRenderPaving = settings.createGroup("Render Paving");
    private final SettingGroup sgStatistics = settings.createGroup("Statistics");

    private final Setting<Integer> width = sgGeneral.add(new IntSetting.Builder()
        .name("width")
        .description("Width of the highway.")
        .defaultValue(4)
        .range(1, 6)
        .sliderRange(1, 6)
        .build()
    );

    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("height")
        .description("Height of the highway.")
        .defaultValue(3)
        .range(2, 5)
        .sliderRange(2, 5)
        .build()
    );

    private final Setting<Floor> floor = sgGeneral.add(new EnumSetting.Builder<Floor>()
        .name("floor")
        .description("What floor placement mode to use.")
        .defaultValue(Floor.Replace)
        .build()
    );

    private final Setting<Boolean> railings = sgGeneral.add(new BoolSetting.Builder()
        .name("railings")
        .description("Builds railings next to the highway.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> mineAboveRailings = sgGeneral.add(new BoolSetting.Builder()
        .name("mine-above-railings")
        .description("Mines blocks above railings.")
        .visible(railings::get)
        .defaultValue(true)
        .build()
    );

    private final Setting<Rotation> rotation = sgGeneral.add(new EnumSetting.Builder<Rotation>()
        .name("rotation")
        .description("Mode of rotation.")
        .defaultValue(Rotation.Both)
        .build()
    );

    private final Setting<Boolean> disconnectOnToggle = sgGeneral.add(new BoolSetting.Builder()
        .name("disconnect-on-toggle")
        .description("Automatically disconnects when the module is turned off, for example for not having enough blocks.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> disconnectDelay = sgGeneral.add(new IntSetting.Builder()
        .name("disconnect-delay")
        .description("Not disconnect if has been running for less than this many seconds.")
        .defaultValue(60)
        .range(1, 3600)
        .sliderRange(1, 60)
        .visible(disconnectOnToggle::get)
        .build()
    );

    private final Setting<Boolean> pauseOnLag = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-lag")
        .description("Pauses the current process while the server stops responding.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> resumeTPS = sgGeneral.add(new IntSetting.Builder()
        .name("resume-tps")
        .description("Server tick speed at which to resume building.")
        .defaultValue(16)
        .range(1, 19)
        .sliderRange(1, 19)
        .visible(pauseOnLag::get)
        .build()
    );

    // Digging

    private final Setting<Boolean> ignoreSigns = sgDigging.add(new BoolSetting.Builder()
        .name("ignore-signs")
        .description("Ignore breaking signs = preserving history (based).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> dontBreakTools = sgDigging.add(new BoolSetting.Builder()
        .name("dont-break-tools")
        .description("Don't break tools.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> endDurability = sgDigging.add(new IntSetting.Builder()
        .name("end-durability")
        .description("What durability do you want your tools to end up at?")
        .defaultValue(3)
        .range(1, 2031)
        .sliderRange(1, 100)
        .visible(() -> dontBreakTools.get())
        .build()
    );

    private final Setting<Integer> savePickaxes = sgDigging.add(new IntSetting.Builder()
        .name("save-pickaxes")
        .description("How many pickaxes to ensure are saved.")
        .defaultValue(0)
        .range(0, 36)
        .sliderRange(0, 36)
        .visible(() -> !dontBreakTools.get())
        .build()
    );

    private final Setting<Integer> breakDelay = sgDigging.add(new IntSetting.Builder()
        .name("break-delay")
        .description("The delay between breaking blocks.")
        .defaultValue(1)
        .min(0)
        .build()
    );

    private final Setting<Integer> blocksPerTick = sgDigging.add(new IntSetting.Builder()
        .name("blocks-per-tick")
        .description("The maximum amount of blocks that can be mined in a tick. Only applies to blocks instantly breakable.")
        .defaultValue(1)
        .range(1, 100)
        .sliderRange(1, 25)
        .build()
    );

    // Paving

    private final Setting<List<Block>> blocksToPlace = sgPaving.add(new BlockListSetting.Builder()
        .name("blocks-to-place")
        .description("Blocks it is allowed to place.")
        .defaultValue(Blocks.OBSIDIAN)
        .filter(block -> Block.isShapeFullCube(block.getDefaultState().getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)))
        .build()
    );

    private final Setting<Integer> placeDelay = sgPaving.add(new IntSetting.Builder()
        .name("place-delay")
        .description("The delay between placing blocks.")
        .defaultValue(1)
        .min(0)
        .build()
    );

    private final Setting<Integer> placementsPerTick = sgPaving.add(new IntSetting.Builder()
        .name("placements-per-tick")
        .description("The maximum amount of blocks that can be placed in a tick.")
        .defaultValue(1)
        .min(1)
        .build()
    );

    // Inventory

    private final Setting<List<Item>> trashItems = sgInventory.add(new ItemListSetting.Builder()
        .name("trash-items")
        .description("Items that are considered trash and can be thrown out.")
        .defaultValue(
            Items.NETHERRACK, Items.QUARTZ, Items.GOLD_NUGGET, Items.GOLDEN_SWORD, Items.GLOWSTONE_DUST,
            Items.GLOWSTONE, Items.BLACKSTONE, Items.BASALT, Items.GHAST_TEAR, Items.SOUL_SAND, Items.SOUL_SOIL,
            Items.ROTTEN_FLESH
        )
        .build()
    );

    private final Setting<Boolean> mineEnderChests = sgInventory.add(new BoolSetting.Builder()
        .name("mine-ender-chests")
        .description("Mines ender chests for obsidian.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> saveEchests = sgInventory.add(new IntSetting.Builder()
        .name("save-ender-chests")
        .description("How many ender chests to ensure are saved.")
        .defaultValue(1)
        .range(0, 64)
        .sliderRange(0, 64)
        .visible(mineEnderChests::get)
        .build()
    );

    private final Setting<Boolean> instaMineEchests = sgInventory.add(new BoolSetting.Builder()
        .name("instant-rebreak-echests")
        .description("Uses the instaMine exploit to break echests.")
        .defaultValue(false)
        .visible(mineEnderChests::get)
        .build()
    );

    private final Setting<Integer> instaMineDelay = sgInventory.add(new IntSetting.Builder()
        .name("rebreak-delay")
        .description("Delay between instant rebreak attempts.")
        .defaultValue(0)
        .sliderMax(20)
        .visible(() -> mineEnderChests.get() && instaMineEchests.get())
        .build()
    );

    // Render Digging

    private final Setting<Boolean> renderMine = sgRenderDigging.add(new BoolSetting.Builder()
        .name("render-blocks-to-mine")
        .description("Render blocks to be mined.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> renderMineShape = sgRenderDigging.add(new EnumSetting.Builder<ShapeMode>()
        .name("blocks-to-mine-shape-mode")
        .description("How the blocks to be mined are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> renderMineSideColor = sgRenderDigging.add(new ColorSetting.Builder()
        .name("blocks-to-mine-side-color")
        .description("Color of blocks to be mined.")
        .defaultValue(new SettingColor(225, 25, 25, 25))
        .build()
    );

    private final Setting<SettingColor> renderMineLineColor = sgRenderDigging.add(new ColorSetting.Builder()
        .name("blocks-to-mine-line-color")
        .description("Color of blocks to be mined.")
        .defaultValue(new SettingColor(225, 25, 25, 255))
        .build()
    );

    // Render Paving

    private final Setting<Boolean> renderPlace = sgRenderPaving.add(new BoolSetting.Builder()
        .name("render-blocks-to-place")
        .description("Render blocks to be placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> renderPlaceShape = sgRenderPaving.add(new EnumSetting.Builder<ShapeMode>()
        .name("blocks-to-place-shape-mode")
        .description("How the blocks to be placed are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> renderPlaceSideColor = sgRenderPaving.add(new ColorSetting.Builder()
        .name("blocks-to-place-side-color")
        .description("Color of blocks to be placed.")
        .defaultValue(new SettingColor(25, 25, 225, 25))
        .build()
    );

    private final Setting<SettingColor> renderPlaceLineColor = sgRenderPaving.add(new ColorSetting.Builder()
        .name("blocks-to-place-line-color")
        .description("Color of blocks to be placed.")
        .defaultValue(new SettingColor(25, 25, 225, 255))
        .build()
    );

    // Statistics

    private final Setting<Boolean> printStatistics = sgStatistics.add(new BoolSetting.Builder()
        .name("print-statistics")
        .description("Prints statistics in chat or disconnect screen when disabling Highway Builder.")
        .defaultValue(true)
        .build()
    );

    private HorizontalDirection dir, leftDir, rightDir;

    private IBlockPosProvider blockPosProvider;

    private final MBlockPos start = new MBlockPos();
    private final MBlockPos currentPos = new MBlockPos();
    private final MBlockPos movePos = new MBlockPos();
    private final MBlockPos lastBreakingPos = new MBlockPos();

    private boolean displayInfo;
    private boolean sentLagMessage;
    private boolean moduleAttacking;
    private boolean moduleEating;
    private boolean btExit;

    private int blocksBroken;
    private int blocksPlaced;
    private int placeTimer;
    private int breakTimer;
    private int count;
    private long startTime;

    private boolean btSettingAllowBreak;
    private boolean btSettingAllowInventory;
    private boolean btSettingAllowPlace;
    private boolean btSettingRenderGoal;

    private State state, lastState;

    public HighwayBuilderHIG() {
        super(HIGTools.MAIN, "highway-builder-HIG", "Automatically builds highways.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) return;

        dir = HorizontalDirection.get(mc.player.getYaw());
        leftDir = dir.rotateLeftSkipOne();
        rightDir = leftDir.opposite();

        blockPosProvider = dir.diagonal ? new DiagonalBlockPosProvider() : new StraightBlockPosProvider();

        start.set(playerPos());
        currentPos.set(start);
        movePos.set(start);
        lastBreakingPos.set(0, 0, 0);

        displayInfo = true;
        sentLagMessage = false;
        moduleEating = false;
        moduleAttacking = false;
        btExit = false;

        blocksBroken = 0;
        blocksPlaced = 0;
        placeTimer = 0;
        breakTimer = 0;
        count = 0;
        startTime = Instant.now().getEpochSecond();

        btSettingAllowBreak = BaritoneAPI.getSettings().allowBreak.value;
        btSettingAllowInventory = BaritoneAPI.getSettings().allowInventory.value;
        btSettingAllowPlace = BaritoneAPI.getSettings().allowPlace.value;
        btSettingRenderGoal = BaritoneAPI.getSettings().renderGoal.value;

        BaritoneAPI.getSettings().allowBreak.value = false;
        BaritoneAPI.getSettings().allowInventory.value = false;
        BaritoneAPI.getSettings().allowPlace.value = false;
        BaritoneAPI.getSettings().renderGoal.value = false;

        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().registerProcess(new btProcess());

        state = State.CheckTasks;
        setState(State.CheckTasks);
    }

    @Override
    public void onDeactivate() {
        if (mc.player == null || mc.world == null) return;

        btExit = true;
        BaritoneAPI.getSettings().allowBreak.value = btSettingAllowBreak;
        BaritoneAPI.getSettings().allowInventory.value = btSettingAllowInventory;
        BaritoneAPI.getSettings().allowPlace.value = btSettingAllowPlace;
        BaritoneAPI.getSettings().renderGoal.value = btSettingRenderGoal;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (width.get() < 3 && dir.diagonal) {
            displayInfo = false;
            exit("Diagonal highways less than 3 blocks wide are not supported, please change the width setting");
            return;
        }

        if (blocksPerTick.get() > 1 && rotation.get().mine) {
            displayInfo = false;
            exit("With rotations enabled, you can break at most 1 block per tick");
            return;
        }

        if (placementsPerTick.get() > 1 && rotation.get().place) {
            displayInfo = false;
            exit("With rotations enabled, you can place at most 1 block per tick");
            return;
        }

        if (Modules.get().get(InstantRebreak.class).isActive()) Modules.get().get(InstantRebreak.class).toggle();

        if (Modules.get().get(KillAura.class).attacking) return;

        if (pauseOnLag.get()) {
            if (TickRate.INSTANCE.getTimeSinceLastTick() > 1.4f) {
                if (!sentLagMessage) {
                    error("Server isn't responding, pausing.");
                    setState(State.Wait);
                }
                sentLagMessage = true;
            }

            if (sentLagMessage) {
                if (TickRate.INSTANCE.getTickRate() > resumeTPS.get()) {
                    setState(lastState);
                    sentLagMessage = false;
                }
            }
        }

        if (!moduleEating && (Modules.get().get(AutoEat.class).eating || Modules.get().get(AutoGap.class).isEating() || Modules.get().get(OffhandManager.class).isEating())) {
            setState(State.Wait);
            moduleEating = true;
        }

        if (moduleEating && (!Modules.get().get(AutoEat.class).eating && !Modules.get().get(AutoGap.class).isEating() && !Modules.get().get(OffhandManager.class).isEating())) {
            setState(lastState);
            moduleEating = false;
        }

        if (!moduleAttacking && Modules.get().get(KillAura.class).attacking) {
            setState(State.Wait);
            moduleAttacking = true;
        }

        if (moduleAttacking && !Modules.get().get(KillAura.class).attacking) {
            setState(lastState);
            moduleAttacking = false;
        }

        if (state != State.CollectObsidian)
            movePos.set(currentPos);

        count = 0;
        state.tick(this);

        if (breakTimer > 0) breakTimer--;
        if (placeTimer > 0) placeTimer--;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (renderMine.get()) {
            render(event, blockPosProvider.getFront(), mBlockPos -> canMine(mBlockPos, true), true);
            if (floor.get() == Floor.Replace) render(event, blockPosProvider.getFloor(), mBlockPos -> canMine(mBlockPos, false), true);
            if (railings.get()) render(event, blockPosProvider.getRailings(true), mBlockPos -> canMine(mBlockPos, false), true);
        }

        if (renderPlace.get()) {
            render(event, blockPosProvider.getLiquids(), mBlockPos -> canPlace(mBlockPos, true), false);
            if (railings.get()) render(event, blockPosProvider.getRailings(false), mBlockPos -> canPlace(mBlockPos, false), false);
            render(event, blockPosProvider.getFloor(), mBlockPos -> canPlace(mBlockPos, false), false);
        }
    }

    private void render(Render3DEvent event, MBPIterator it, Predicate<MBlockPos> predicate, boolean mine) {
        Color sideColor = mine ? renderMineSideColor.get() : renderPlaceSideColor.get();
        Color lineColor = mine ? renderMineLineColor.get() : renderPlaceLineColor.get();
        ShapeMode shapeMode = mine ? renderMineShape.get() : renderPlaceShape.get();

        MBlockPos posRender1 = new MBlockPos();
        MBlockPos posRender2 = new MBlockPos();

        for (MBlockPos pos : it) {
            posRender1.set(pos);

            if (predicate.test(posRender1)) {
                int excludeDir = 0;

                for (Direction side : Direction.values()) {
                    posRender2.set(posRender1).add(side.getOffsetX(), side.getOffsetY(), side.getOffsetZ());

                    it.save();
                    for (MBlockPos p : it) {
                        if (p.equals(posRender2) && predicate.test(p)) excludeDir |= Dir.get(side);
                    }
                    it.restore();
                }

                event.renderer.box(posRender1.getBlockPos(), sideColor, lineColor, shapeMode, excludeDir);
            }
        }
    }

    private void setState(State state) {
        lastState = this.state;
        this.state = state;
        state.start(this);
    }

    private int getWidthLeft() {
        return switch (width.get()) {
            case 2, 3 -> 1;
            case 4, 5 -> 2;
            case 6 -> 3;
            default -> 0;
        };
    }

    private int getWidthRight() {
        return switch (width.get()) {
            case 3, 4 -> 1;
            case 5, 6 -> 2;
            default -> 0;
        };
    }

    private boolean canMine(MBlockPos pos, boolean ignoreBlocksToPlace) {
        BlockState state = pos.getState();
        if (!BlockUtils.canBreak(pos.getBlockPos(), state) || ignoreSigns.get() && (state.getBlock() instanceof SignBlock || state.getBlock() instanceof WallSignBlock) && (pos.getBlockPos().getY() >= mc.player.getBlockY())) {
            return false;
        }
        if (pos.getBlockPos().getY() > mc.player.getY() && !state.isAir()) {
            return true;
        }
        return ignoreBlocksToPlace || !blocksToPlace.get().contains(state.getBlock());
    }

    private boolean canPlace(MBlockPos pos, boolean liquids) {
        return liquids ? !pos.getState().getFluidState().isEmpty() : HIGUtils.canPlaceHIG(pos.getBlockPos());
    }

    private void exit(String reason) {
        if (displayInfo && disconnectOnToggle.get() && Instant.now().getEpochSecond() - startTime > disconnectDelay.get()) {
            if (printStatistics.get()) {
                mc.getNetworkHandler().getConnection().disconnect(Text.of(String.format(
                    reason +
                    "\nDistance: %.0f" +
                    "\nBlocks broken: %d" +
                    "\nBlocks placed: %d",
                    distance(start, playerPos()), blocksBroken, blocksPlaced
                )));
            } else {
                mc.getNetworkHandler().getConnection().disconnect(Text.of(reason));
            }
        } else {
            error(reason);
            if (displayInfo && printStatistics.get()) {
                info("Distance: (highlight)%.0f", distance(start, playerPos()));
                info("Blocks broken: (highlight)%d", blocksBroken);
                info("Blocks placed: (highlight)%d", blocksPlaced);
            }
        }

        toggle();
    }

    private MBlockPos playerPos() {
        int x = (int)Math.floor(mc.player.getX());
        int y = (int)Math.floor(mc.player.getY());
        int z = (int)Math.floor(mc.player.getZ());
        return new MBlockPos().set(x, y, z);
    }

    private double distance(MBlockPos a, MBlockPos b) {
        int xDiff = a.x - b.x;
        int yDiff = a.y - b.y;
        int zDiff = a.z - b.z;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }

    private void updatePosition() {
        MBlockPos next = new MBlockPos().set(currentPos).add(dir.offsetX, 0, dir.offsetZ);

        if (distance(next, playerPos()) < 3)
            currentPos.set(next);
    }

    private class btProcess implements IBaritoneProcess {
        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean isTemporary() {
            return true;
        }

        @Override
        public PathingCommand onTick(boolean b1, boolean b2) {
            if (!btExit) {
                BlockPos goal = new BlockPos(movePos.x, movePos.y, movePos.z);
                return new PathingCommand(new GoalNear(goal, 0), PathingCommandType.SET_GOAL_AND_PATH);
            } else {
                return new PathingCommand(null, PathingCommandType.DEFER);
            }
        }

        @Override
        public void onLostControl() {}

        @Override
        public String displayName0() {
            return "HighwayToolsHIG";
        }

        @Override
        public double priority() {
            return 2.0;
        }
    }

    private enum State {
        CheckTasks {
            @Override
            protected void start(HighwayBuilderHIG b) {
                b.state.tick(b);
            }

            @Override
            protected void tick(HighwayBuilderHIG b) {
                if (needsToPlace(b, b.blockPosProvider.getLiquids(), true)) b.setState(FillLiquids); // Fill Liquids
                else if (needsToMine(b, b.blockPosProvider.getFront(), true)) b.setState(MineFront); // Mine Front
                else if (b.floor.get() == Floor.Replace && needsToMine(b, b.blockPosProvider.getFloor(), false)) b.setState(MineFloor); // Mine Floor
                else if (b.railings.get() && needsToMine(b, b.blockPosProvider.getRailings(true), false)) b.setState(MineRailings); // Mine Railings
                else if (b.railings.get() && needsToPlace(b, b.blockPosProvider.getRailings(false), false)) b.setState(PlaceRailings); // Place Railings
                else if (needsToPlace(b, b.blockPosProvider.getFloor(), false)) b.setState(PlaceFloor); // Place Floor
                else {
                    b.updatePosition();
                    if (b.lastState != CheckTasks)
                        b.setState(CheckTasks);
                }
            }

            private boolean needsToMine(HighwayBuilderHIG b, MBPIterator it, boolean ignoreBlocksToPlace) {
                for (MBlockPos pos : it) {
                    if (b.canMine(pos, ignoreBlocksToPlace)) return true;
                }

                return false;
            }

            private boolean needsToPlace(HighwayBuilderHIG b, MBPIterator it, boolean liquids) {
                for (MBlockPos pos : it) {
                    if (b.canPlace(pos, liquids)) return true;
                }

                return false;
            }
        },

        FillLiquids {
            @Override
            protected void tick(HighwayBuilderHIG b) {
                int slot = findBlocksToPlacePrioritizeTrash(b);
                if (slot == -1) return;

                place(b, new MBPIteratorFilter(b.blockPosProvider.getLiquids(), pos -> !pos.getState().getFluidState().isEmpty()), slot, CheckTasks);
            }
        },

        MineFront {
            @Override
            protected void start(HighwayBuilderHIG b) {
                b.state.tick(b);
            }

            @Override
            protected void tick(HighwayBuilderHIG b) {
                mine(b, b.blockPosProvider.getFront(), true, MineFloor, false);
            }
        },

        MineFloor {
            @Override
            protected void start(HighwayBuilderHIG b) {
                b.state.tick(b);
            }

            @Override
            protected void tick(HighwayBuilderHIG b) {
                if (b.railings.get())
                    mine(b, b.blockPosProvider.getFloor(), false, MineRailings, false);
                else
                    mine(b, b.blockPosProvider.getFloor(), false, PlaceFloor, false);
            }
        },

        MineRailings {
            @Override
            protected void start(HighwayBuilderHIG b) {
                b.state.tick(b);
            }

            @Override
            protected void tick(HighwayBuilderHIG b) {
                mine(b, b.blockPosProvider.getRailings(true), false, PlaceRailings, true);
            }
        },

        PlaceRailings {
            @Override
            protected void start(HighwayBuilderHIG b) {
                b.state.tick(b);
            }

            @Override
            protected void tick(HighwayBuilderHIG b) {
                int slot = findBlocksToPlace(b);
                if (slot == -1) return;

                place(b, b.blockPosProvider.getRailings(false), slot, PlaceFloor);
            }
        },

        PlaceFloor {
            @Override
            protected void start(HighwayBuilderHIG b) {
                b.state.tick(b);
            }

            @Override
            protected void tick(HighwayBuilderHIG b) {
                int slot = findBlocksToPlace(b);
                if (slot == -1) return;

                place(b, b.blockPosProvider.getFloor(), slot, CheckTasks);
            }
        },

        CollectObsidian {
            private final MBlockPos pos = new MBlockPos();
            private int timer;
            @Override
            protected void start(HighwayBuilderHIG b) {
                pos.set(b.mc.player);
                timer = 100;
                b.state.tick(b);
            }

            @Override
            protected void tick(HighwayBuilderHIG b) {
                MBlockPos itemPos = new MBlockPos();
                boolean itemFound = false;

                Rotations.rotate(b.dir.opposite().yaw, 0);

                for (Entity entity : b.mc.world.getOtherEntities(b.mc.player, new Box(pos.x - 5, pos.y - 2, pos.z - 5, pos.x + 5, pos.y + 2, pos.z + 5))) {
                    if (entity instanceof ItemEntity itemEntity && itemEntity.getStack().getItem() == Items.OBSIDIAN) {
                        int x = (int)Math.floor(itemEntity.getX());
                        int y = (int)Math.floor(itemEntity.getY());
                        int z = (int)Math.floor(itemEntity.getZ());
                        itemPos.set(x, y, z);
                        itemFound = true;
                        break;
                    }
                }

                if (b.movePos.x == itemPos.x && b.movePos.z == itemPos.z)
                    timer--;
                else
                    timer = 100;

                if (itemFound) {
                    b.movePos.set(itemPos);
                } else {
                    b.movePos.set(b.currentPos);
                    b.setState(CheckTasks);
                    return;
                }

                if (!b.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    InvUtils.dropHand();
                    return;
                }

                for (int i = 0; i < b.mc.player.getInventory().main.size(); i++) {
                    ItemStack itemStack = b.mc.player.getInventory().getStack(i);

                    if (b.trashItems.get().contains(itemStack.getItem())) {
                        InvUtils.drop().slot(i);
                        return;
                    }
                }

                if (timer == 0) {
                    b.error("Obsidian collection timed out");
                    b.movePos.set(b.currentPos);
                    b.setState(CheckTasks);
                }
            }
        },

        MineEnderChests {
            private static final MBlockPos pos = new MBlockPos();
            private int counter;
            private boolean first, primed;
            private int instaMineTimer;
            private double prevPlayerX, prevPlayerZ;

            @Override
            protected void start(HighwayBuilderHIG b) {
                int emptySlots = 0;
                for (int i = 0; i < b.mc.player.getInventory().main.size(); i++) {
                    ItemStack itemStack = b.mc.player.getInventory().getStack(i);
                    if (itemStack.isEmpty() ||
                        b.trashItems.get().contains(itemStack.getItem()))
                        emptySlots++;
                }

                if (emptySlots == 0) {
                    b.exit("No empty slots");
                    return;
                }

                int minimumSlots = Math.max(emptySlots - 4, 1);
                counter = minimumSlots * 8;

                first = true;
                primed = false;
            }

            @Override
            protected void tick(HighwayBuilderHIG b) {
                if (b.mc.player.getX() != prevPlayerX || b.mc.player.getZ() != prevPlayerZ) {
                    prevPlayerX = b.mc.player.getX();
                    prevPlayerZ = b.mc.player.getZ();
                    first = true;
                    primed = false;
                    return;
                }

                HorizontalDirection dir = b.dir.diagonal ? b.dir.rotateLeft().rotateLeftSkipOne() : b.dir.opposite();
                pos.set(b.mc.player).offset(dir, 2);

                BlockPos bp = pos.getBlockPos();

                // Check block state
                BlockState blockState = b.mc.world.getBlockState(bp);

                if (blockState.getBlock() == Blocks.ENDER_CHEST) {
                    // Mine ender chest
                    int slot = findAndMoveBestToolToHotbar(b, blockState, true);
                    if (slot == -1) {
                        b.exit("Cannot find pickaxe without silk touch to mine ender chests");
                        return;
                    }

                    InvUtils.swap(slot, false);

                    if (b.instaMineEchests.get() && primed) {
                        if (instaMineTimer > 0) {
                            instaMineTimer--;
                            return;
                        }

                        PlayerActionC2SPacket p = new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, bp, BlockUtils.getDirection(bp));
                        instaMineTimer = b.instaMineDelay.get();

                        if (b.rotation.get().mine) Rotations.rotate(Rotations.getYaw(bp), Rotations.getPitch(bp), () -> b.mc.getNetworkHandler().sendPacket(p));
                        else b.mc.getNetworkHandler().sendPacket(p);
                    } else {
                        if (b.rotation.get().mine) Rotations.rotate(Rotations.getYaw(bp), Rotations.getPitch(bp), () -> BlockUtils.breakBlock(bp, true));
                        else BlockUtils.breakBlock(bp, true);
                    }
                } else {
                    // Place ender chest
                    int slot = findAndMoveToHotbar(b, itemStack -> itemStack.getItem() == Items.ENDER_CHEST, false);
                    if (slot == -1 || countItem(b, stack -> stack.getItem().equals(Items.ENDER_CHEST)) <= b.saveEchests.get() || counter == 0) {
                        b.setState(CollectObsidian);
                        return;
                    }
                    counter--;

                    if (!first)
                        primed = true;
                    else
                        first = false;

                    BlockUtils.place(bp, Hand.MAIN_HAND, slot, b.rotation.get().place, 0, true, true, false);
                }
            }
        },

        Wait {
            @Override
            protected void tick(HighwayBuilderHIG b) {}
        };

        protected void start(HighwayBuilderHIG b) {}

        protected abstract void tick(HighwayBuilderHIG b);

        protected void mine(HighwayBuilderHIG b, MBPIterator it, boolean ignoreBlocksToPlace, State nextState, boolean railMode) {
            boolean breaking = false;
            boolean finishedBreaking = false; // If you can multi break this lets you mine blocks between tasks in a single tick

            for (MBlockPos pos : it) {
                if (b.count >= b.blocksPerTick.get()) return;
                if (b.breakTimer > 0) return;

                BlockState state = pos.getState();

                if (railMode && pos.getBlockPos().getY() > b.mc.player.getY()) {
                    if (state.isAir()) {
                        continue;
                    }
                } else if (state.isAir() || (!ignoreBlocksToPlace && b.blocksToPlace.get().contains(state.getBlock()))) {
                    continue;
                }

                int slot = findAndMoveBestToolToHotbar(b, state, false);
                if (slot == -1) return;

                InvUtils.swap(slot, false);

                BlockPos mcPos = pos.getBlockPos();
                if (BlockUtils.canBreak(mcPos)) {
                    if (b.rotation.get().mine) Rotations.rotate(Rotations.getYaw(mcPos), Rotations.getPitch(mcPos), () -> BlockUtils.breakBlock(mcPos, true));
                    else BlockUtils.breakBlock(mcPos, true);

                    breaking = true;

                    b.breakTimer = b.breakDelay.get();

                    if (!b.lastBreakingPos.equals(pos)) {
                        b.lastBreakingPos.set(pos);
                        b.blocksBroken++;
                    }

                    b.count++;

                    // Can only multi break if we aren't rotating and the block can be instamined
                    if (b.blocksPerTick.get() == 1 || !BlockUtils.canInstaBreak(mcPos) || b.rotation.get().mine) break;
                }

                if (!it.hasNext() && BlockUtils.canInstaBreak(mcPos)) finishedBreaking = true;
            }

            if (finishedBreaking || !breaking)
                b.setState(nextState);
        }

        protected void place(HighwayBuilderHIG b, MBPIterator it, int slot, State nextState) {
            boolean placed = false;
            boolean finishedPlacing = false;

            for (MBlockPos pos : it) {
                if (b.count >= b.placementsPerTick.get()) return;
                if (b.placeTimer > 0) return;

                if (BlockUtils.place(pos.getBlockPos(), Hand.MAIN_HAND, slot, b.rotation.get().place, 0, true, false, false)) {
                    placed = true;
                    b.blocksPlaced++;
                    b.placeTimer = b.placeDelay.get();

                    b.count++;
                    if (b.placementsPerTick.get() == 1) break;
                }

                if (!it.hasNext()) finishedPlacing = true;
            }

            if (finishedPlacing || !placed) b.setState(nextState);
        }

        private int findSlot(HighwayBuilderHIG b, Predicate<ItemStack> predicate, boolean hotbar) {
            for (int i = hotbar ? 0 : 9; i < (hotbar ? 9 : b.mc.player.getInventory().main.size()); i++) {
                if (predicate.test(b.mc.player.getInventory().getStack(i))) return i;
            }

            return -1;
        }

        private int findHotbarSlot(HighwayBuilderHIG b, boolean replaceTools) {
            int trashSlot = -1;
            int slotsWithBlocks = 0;
            int slotWithLeastBlocks = -1;
            int slotWithLeastBlocksCount = Integer.MAX_VALUE;

            // Loop hotbar
            for (int i = 0; i < 9; i++) {
                ItemStack itemStack = b.mc.player.getInventory().getStack(i);

                // Return if the slot is empty
                if (itemStack.isEmpty()) return i;

                // Return if the slot contains a tool and replacing tools is enabled
                if (replaceTools && AutoTool.isTool(itemStack)) return i;

                // Store the slot if it contains thrash
                if (b.trashItems.get().contains(itemStack.getItem())) trashSlot = i;

                // Update tracked stats about slots that contain building blocks
                if (itemStack.getItem() instanceof BlockItem blockItem && b.blocksToPlace.get().contains(blockItem.getBlock())) {
                    slotsWithBlocks++;

                    if (itemStack.getCount() < slotWithLeastBlocksCount) {
                        slotWithLeastBlocksCount = itemStack.getCount();
                        slotWithLeastBlocks = i;
                    }
                }
            }

            // Return thrash slot if found
            if (trashSlot != -1) return trashSlot;

            // If there are more than 1 slots with building blocks return the slot with the lowest amount of blocks
            if (slotsWithBlocks > 1) return slotWithLeastBlocks;

            // No space found in hotbar
            b.exit("No empty space in hotbar");
            return -1;
        }

        private boolean hasItem(HighwayBuilderHIG b, Item item) {
            for (int i = 0; i < b.mc.player.getInventory().main.size(); i++) {
                if (b.mc.player.getInventory().getStack(i).getItem() == item) return true;
            }

            return false;
        }

        protected int countItem(HighwayBuilderHIG b, Predicate<ItemStack> predicate) {
            int count = 0;
            for (int i = 0; i < b.mc.player.getInventory().main.size(); i++) {
                ItemStack stack = b.mc.player.getInventory().getStack(i);
                if (predicate.test(stack)) count += stack.getCount();
            }

            return count;
        }

        protected int findAndMoveToHotbar(HighwayBuilderHIG b, Predicate<ItemStack> predicate, boolean required) {
            // Check hotbar
            int slot = findSlot(b, predicate, true);
            if (slot != -1) return slot;

            // Find hotbar slot to move to
            int hotbarSlot = findHotbarSlot(b, false);
            if (hotbarSlot == -1) return -1;

            // Check inventory
            slot = findSlot(b, predicate, false);

            // Stop if no items were found and are required
            if (slot == -1) {
                if (required) {
                    b.exit("Out of items");
                }

                return -1;
            }

            // Move items from inventory to hotbar
            InvUtils.move().from(slot).toHotbar(hotbarSlot);
            InvUtils.dropHand();

            return hotbarSlot;
        }

        protected int findAndMoveBestToolToHotbar(HighwayBuilderHIG b, BlockState blockState, boolean noSilkTouch) {
            // Check for creative
            if (b.mc.player.isCreative()) return b.mc.player.getInventory().selectedSlot;

            // Find best tool
            double bestScore = -1;
            int bestSlot = -1;

            for (int i = 0; i < b.mc.player.getInventory().main.size(); i++) {
                double score = AutoTool.getScore(b.mc.player.getInventory().getStack(i), blockState, false, false, AutoTool.EnchantPreference.None, itemStack -> {
                    if (noSilkTouch && Utils.hasEnchantment(itemStack, Enchantments.SILK_TOUCH)) return false;
                    return !b.dontBreakTools.get() || itemStack.getMaxDamage() - itemStack.getDamage() > b.endDurability.get();
                });

                if (score > bestScore) {
                    bestScore = score;
                    bestSlot = i;
                }
            }

            if (bestSlot == -1) return b.mc.player.getInventory().selectedSlot;

            if (b.mc.player.getInventory().getStack(bestSlot).getItem() instanceof PickaxeItem ){
                int count = countItem(b, stack -> stack.getItem() instanceof PickaxeItem);

                if (count <= b.savePickaxes.get()) {
                    b.exit("Found less than the selected amount of pickaxes required: " + count + "/" + (b.savePickaxes.get() + 1));
                    return -1;
                }
            }

            // Check if the tool is already in hotbar
            if (bestSlot < 9) return bestSlot;

            // Find hotbar slot to move to
            int hotbarSlot = findHotbarSlot(b, true);
            if (hotbarSlot == -1) return -1;

            // Move tool from inventory to hotbar
            InvUtils.move().from(bestSlot).toHotbar(hotbarSlot);
            InvUtils.dropHand();

            return hotbarSlot;
        }

        protected int findBlocksToPlace(HighwayBuilderHIG b) {
            int slot = findAndMoveToHotbar(b, itemStack -> itemStack.getItem() instanceof BlockItem blockItem && b.blocksToPlace.get().contains(blockItem.getBlock()), false);

            if (slot == -1) {
                if (!b.mineEnderChests.get() || !hasItem(b, Items.ENDER_CHEST) || countItem(b, stack -> stack.getItem().equals(Items.ENDER_CHEST)) <= b.saveEchests.get()) {
                    b.exit("Out of blocks to place");
                    return -1;
                }
                else b.setState(MineEnderChests);

                return -1;
            }

            return slot;
        }

        protected int findBlocksToPlacePrioritizeTrash(HighwayBuilderHIG b) {
            int slot = findAndMoveToHotbar(b, itemStack -> {
                if (!(itemStack.getItem() instanceof BlockItem)) return false;
                return b.trashItems.get().contains(itemStack.getItem());
            }, false);

            return slot != -1 ? slot : findBlocksToPlace(b);
        }
    }

    private interface MBPIterator extends Iterator<MBlockPos>, Iterable<MBlockPos> {
        void save();

        void restore();

        @NotNull
        @Override
        default Iterator<MBlockPos> iterator() {
            return this;
        }
    }

    private static class MBPIteratorFilter implements MBPIterator {
        private final MBPIterator it;
        private final Predicate<MBlockPos> predicate;

        private MBlockPos pos;
        private boolean isOld = true;

        private boolean pisOld = true;

        public MBPIteratorFilter(MBPIterator it, Predicate<MBlockPos> predicate) {
            this.it = it;
            this.predicate = predicate;
        }

        @Override
        public void save() {
            it.save();
            pisOld = isOld;
            isOld = true;
        }

        @Override
        public void restore() {
            it.restore();
            isOld = pisOld;
        }

        @Override
        public boolean hasNext() {
            if (isOld) {
                isOld = false;
                pos = null;

                while (it.hasNext()) {
                    pos = it.next();

                    if (predicate.test(pos)) return true;
                    else pos = null;
                }
            }

            return pos != null && predicate.test(pos);
        }

        @Override
        public MBlockPos next() {
            isOld = true;
            return pos;
        }
    }

    private interface IBlockPosProvider {
        MBPIterator getFront();

        MBPIterator getFloor();

        MBPIterator getRailings(boolean mine);

        MBPIterator getLiquids();
    }

    private class StraightBlockPosProvider implements IBlockPosProvider {
        private final MBlockPos pos = new MBlockPos();
        private final MBlockPos pos2 = new MBlockPos();

        @Override
        public MBPIterator getFront() {
            pos.set(currentPos).offset(dir).offset(leftDir, getWidthLeft());

            return new MBPIterator() {
                private int w, y;
                private int pw, py;

                @Override
                public boolean hasNext() {
                    return w < width.get() && y < height.get();
                }

                @Override
                public MBlockPos next() {
                    pos2.set(pos).offset(rightDir, w).add(0, y, 0);

                    w++;
                    if (w >= width.get()) {
                        w = 0;
                        y++;
                    }

                    return pos2;
                }

                @Override
                public void save() {
                    pw = w;
                    py = y;
                    w = y = 0;
                }

                @Override
                public void restore() {
                    w = pw;
                    y = py;
                }
            };
        }

        @Override
        public MBPIterator getFloor() {
            pos.set(currentPos).offset(dir).offset(leftDir, getWidthLeft()).add(0, -1, 0);

            return new MBPIterator() {
                private int w;
                private int pw;

                @Override
                public boolean hasNext() {
                    return w < width.get();
                }

                @Override
                public MBlockPos next() {
                    return pos2.set(pos).offset(rightDir, w++);
                }

                @Override
                public void save() {
                    pw = w;
                    w = 0;
                }

                @Override
                public void restore() {
                    w = pw;
                }
            };
        }

        @Override
        public MBPIterator getRailings(boolean mine) {
            boolean mineAll = mine && mineAboveRailings.get();
            pos.set(currentPos).offset(dir);

            return new MBPIterator() {
                private int i, y;
                private int pi, py;

                @Override
                public boolean hasNext() {
                    return i < 2 && y < (mineAll ? height.get() : 1);
                }

                @Override
                public MBlockPos next() {
                    if (i == 0) pos2.set(pos).offset(leftDir, getWidthLeft() + 1).add(0, y, 0);
                    else pos2.set(pos).offset(rightDir, getWidthRight() + 1).add(0, y, 0);

                    y++;
                    if (y >= (mineAll ? height.get() : 1)) {
                        y = 0;
                        i++;
                    }

                    return pos2;
                }

                @Override
                public void save() {
                    pi = i;
                    py = y;
                    i = y = 0;
                }

                @Override
                public void restore() {
                    i = pi;
                    y = py;
                }
            };
        }

        @Override
        public MBPIterator getLiquids() {
            pos.set(currentPos).offset(dir, 2).offset(leftDir, getWidthLeft() + (railings.get() && mineAboveRailings.get() ? 2 : 1));

            return new MBPIterator() {
                private int w, y;
                private int pw, py;

                private int getWidth() {
                    return width.get() + (railings.get() && mineAboveRailings.get() ? 2 : 0);
                }

                @Override
                public boolean hasNext() {
                    return w < getWidth() + 2 && y < height.get() + 1;
                }

                @Override
                public MBlockPos next() {
                    pos2.set(pos).offset(rightDir, w).add(0, y, 0);

                    w++;
                    if (w >= getWidth() + 2) {
                        w = 0;
                        y++;
                    }

                    return pos2;
                }

                @Override
                public void save() {
                    pw = w;
                    py = y;
                    w = y = 0;
                }

                @Override
                public void restore() {
                    w = pw;
                    y = py;
                }
            };
        }
    }

    private class DiagonalBlockPosProvider implements IBlockPosProvider {
        private final MBlockPos pos = new MBlockPos();
        private final MBlockPos pos2 = new MBlockPos();

        @Override
        public MBPIterator getFront() {
            pos.set(currentPos).offset(dir.rotateLeft()).offset(leftDir, getWidthLeft() - 1);

            return new MBPIterator() {
                private int i, w, y;
                private int pi, pw, py;

                @Override
                public boolean hasNext() {
                    return i < 2 && w < width.get() && y < height.get();
                }

                @Override
                public MBlockPos next() {
                    pos2.set(pos).offset(rightDir, w).add(0, y++, 0);

                    if (y >= height.get()) {
                        y = 0;
                        w++;

                        if (w >= (i == 0 ? width.get() - 1 : width.get())) {
                            w = 0;
                            i++;

                            pos.set(currentPos).offset(dir).offset(leftDir, getWidthLeft());
                        }
                    }

                    return pos2;
                }

                private void initPos() {
                    if (i == 0) pos.set(currentPos).offset(dir.rotateLeft()).offset(leftDir, getWidthLeft() - 1);
                    else pos.set(currentPos).offset(dir).offset(leftDir, getWidthLeft());
                }

                @Override
                public void save() {
                    pi = i;
                    pw = w;
                    py = y;
                    i = w = y = 0;

                    initPos();
                }

                @Override
                public void restore() {
                    i = pi;
                    w = pw;
                    y = py;

                    initPos();
                }
            };
        }

        @Override
        public MBPIterator getFloor() {
            pos.set(currentPos).add(0, -1, 0).offset(dir.rotateLeft()).offset(leftDir, getWidthLeft() - 1);

            return new MBPIterator() {
                private int i, w;
                private int pi, pw;

                @Override
                public boolean hasNext() {
                    return i < 2 && w < width.get();
                }

                @Override
                public MBlockPos next() {
                    pos2.set(pos).offset(rightDir, w++);

                    if (w >= (i == 0 ? width.get() - 1 : width.get())) {
                        w = 0;
                        i++;

                        pos.set(currentPos).add(0, -1, 0).offset(dir).offset(leftDir, getWidthLeft());
                    }

                    return pos2;
                }

                private void initPos() {
                    if (i == 0) pos.set(currentPos).add(0, -1, 0).offset(dir.rotateLeft()).offset(leftDir, getWidthLeft() - 1);
                    else pos.set(currentPos).add(0, -1, 0).offset(dir).offset(leftDir, getWidthLeft());
                }

                @Override
                public void save() {
                    pi = i;
                    pw = w;
                    i = w = 0;

                    initPos();
                }

                @Override
                public void restore() {
                    i = pi;
                    w = pw;

                    initPos();
                }
            };
        }

        @Override
        public MBPIterator getRailings(boolean mine) {
            boolean mineAll = mine && mineAboveRailings.get();
            pos.set(currentPos).offset(dir.rotateLeft()).offset(leftDir, getWidthLeft());

            return new MBPIterator() {
                private int i, y;
                private int pi, py;

                @Override
                public boolean hasNext() {
                    return i < 2 && y < (mineAll ? height.get() : 1);
                }

                @Override
                public MBlockPos next() {
                    pos2.set(pos).add(0, y++, 0);

                    if (y >= (mineAll ? height.get() : 1)) {
                        y = 0;
                        i++;

                        pos.set(currentPos).offset(dir.rotateRight()).offset(rightDir, getWidthRight());
                    }

                    return pos2;
                }

                private void initPos() {
                    if (i == 0) pos.set(currentPos).offset(dir.rotateLeft()).offset(leftDir, getWidthLeft());
                    else pos.set(currentPos).offset(dir.rotateRight()).offset(rightDir, getWidthRight());
                }

                @Override
                public void save() {
                    pi = i;
                    py = y;
                    i = y = 0;

                    initPos();
                }

                @Override
                public void restore() {
                    i = pi;
                    y = py;

                    initPos();
                }
            };
        }

        @Override
        public MBPIterator getLiquids() {
            boolean m = railings.get() && mineAboveRailings.get();
            pos.set(currentPos).offset(dir).offset(dir.rotateLeft()).offset(leftDir, getWidthLeft());

            return new MBPIterator() {
                private int i, w, y;
                private int pi, pw, py;

                private int getWidth() {
                    return width.get() + (i == 0 ? 1 : 0) + (m && i == 1 ? 2 : 0);
                }

                @Override
                public boolean hasNext() {
                    if (m && i == 1 && y == height.get() &&  w == getWidth() - 1) return false;
                    return i < 2 && w < getWidth() && y < height.get() + 1;
                }

                private void updateW() {
                    w++;

                    if (w >= getWidth()) {
                        w = 0;
                        i++;

                        pos.set(currentPos).offset(dir, 2).offset(leftDir, getWidthLeft() + (m ? 1 : 0));
                    }
                }

                @Override
                public MBlockPos next() {
                    if (i == (m ? 1 : 0) && y == height.get() && (w == 0 || w == getWidth() - 1)) {
                        y = 0;
                        updateW();
                    }

                    pos2.set(pos).offset(rightDir, w).add(0, y++, 0);

                    if (y >= height.get() + 1) {
                        y = 0;
                        updateW();
                    }

                    return pos2;
                }

                private void initPos() {
                    if (i == 0) pos.set(currentPos).offset(dir).offset(dir.rotateLeft()).offset(leftDir, getWidthLeft());
                    else pos.set(currentPos).offset(dir, 2).offset(leftDir, getWidthLeft() + (m ? 1 : 0));
                }

                @Override
                public void save() {
                    pi = i;
                    pw = w;
                    py = y;
                    i = w = y = 0;

                    initPos();
                }

                @Override
                public void restore() {
                    i = pi;
                    w = pw;
                    y = py;

                    initPos();
                }
            };
        }
    }

    public enum Rotation {
        None(false, false),
        Mine(true, false),
        Place(false, true),
        Both(true, true);

        public final boolean mine, place;

        Rotation(boolean mine, boolean place) {
            this.mine = mine;
            this.place = place;
        }
    }

    public enum Floor {
        Replace,
        PlaceMissing
    }
}
