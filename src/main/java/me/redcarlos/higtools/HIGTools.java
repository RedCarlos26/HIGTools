package me.redcarlos.higtools;

import com.mojang.logging.LogUtils;
import me.redcarlos.higtools.commands.Center;
import me.redcarlos.higtools.commands.Coordinates;
import me.redcarlos.higtools.modules.highwayborers.*;
import me.redcarlos.higtools.modules.hud.TextPresets;
import me.redcarlos.higtools.modules.main.*;
import me.redcarlos.higtools.system.HIGTab;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.value.ValueMap;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class HIGTools extends MeteorAddon {
    public static final String MOD_ID = "higtools";
    public static final ModMetadata METADATA;
    public static final String VERSION;
    public static final Category MAIN;
    public static final Category BORERS;
    public static final HudGroup HUD;

    static {
        METADATA = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();
        VERSION = METADATA.getVersion().getFriendlyString();

        MAIN = new Category("HIG Tools", Items.NETHERITE_PICKAXE.getDefaultStack());
        BORERS = new Category("  Borers  ", Items.NETHERITE_PICKAXE.getDefaultStack());
        HUD = new HudGroup("HIG Tools");
    }

    @Override
    public void onInitialize() {
        LogUtils.getLogger().info("Initializing HIGTools {}", HIGTools.VERSION);

        // Systems
        BetterChat.registerCustomHead("[HIG Tools]", identifier("icon.png"));
        MeteorStarscript.ss.set("higtools", new ValueMap().set("version", VERSION));
        Tabs.add(new HIGTab());

        // Commands
        Commands.add(new Center());
        Commands.add(new Coordinates());

        // Hud
        Hud hud = Systems.get(Hud.class);
        hud.register(TextPresets.INFO);

        // Modules
        Modules modules = Modules.get();

        modules.add(new AfkLogout());
        modules.add(new AutoCenter());
        modules.add(new AutoWalkHIG());
        modules.add(new AxisViewer());
        modules.add(new DiscordRPC());
        modules.add(new HighwayBuilderHIG());
        modules.add(new HighwayTools());
        modules.add(new HotbarManager());
        modules.add(new LiquidFillerHIG());
        modules.add(new OffhandManager());
        modules.add(new ScaffoldHIG());

        // Borers
        modules.add(new AxisBorer());
        modules.add(new NegNegBorer());
        modules.add(new NegPosBorer());
        modules.add(new PosNegBorer());
        modules.add(new PosPosBorer());
    }

    @Override
    public String getPackage() {
        return "me.redcarlos.higtools";
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(MAIN);
        Modules.registerCategory(BORERS);
    }

    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
