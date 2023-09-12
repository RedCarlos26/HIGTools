package higtools;

import higtools.commands.Center;
import higtools.commands.Coordinates;
import higtools.commands.Disconnect;
import higtools.modules.borers.*;
import higtools.modules.hud.BindsHud;
import higtools.modules.hud.GreetingsHud;
import higtools.modules.main.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HIGTools extends MeteorAddon {
    private static final ModMetadata Metadata = FabricLoader.getInstance().getModContainer("higtools").orElseThrow(() -> new RuntimeException("HIGTools mod container not found!")).getMetadata();
    public static final String Version = Metadata.getVersion().toString();
    public static final Logger LOG = LoggerFactory.getLogger("HIGTools");
    public static final Category Main = new Category("HIG Tools", Items.NETHERITE_PICKAXE.getDefaultStack());
    public static final Category Borers = new Category(" Borers ", Items.NETHERITE_PICKAXE.getDefaultStack());
    public static final HudGroup Hud = new HudGroup("HIG Tools");

    @Override
    public void onInitialize() {
        LOG.info("Initializing HIGTools %s".formatted(HIGTools.Version));

        // Commands
        Commands.add(new Center());
        Commands.add(new Coordinates());
        Commands.add(new Disconnect());

        // HUD
        Hud hud = Systems.get(Hud.class);
        hud.register(BindsHud.INFO);
        hud.register(GreetingsHud.INFO);

        // Modules
        Modules modules = Modules.get();

        modules.add(new AfkLogout());
        modules.add(new ArmorNotify());
        modules.add(new AutoCenter());
        modules.add(new AutoWalkPlus());
        modules.add(new AxisViewer());
        modules.add(new HIGPrefix());
        modules.add(new DiscordRPC());
        modules.add(new HandManager());
        modules.add(new HighwayBuilderPlus());
        modules.add(new HighwayTools());
        modules.add(new InvManager());
        modules.add(new ScaffoldPlus());

        modules.add(new AxisBorer());
        modules.add(new NegNegBorer());
        modules.add(new NegPosBorer());
        modules.add(new PosNegBorer());
        modules.add(new PosPosBorer());
        modules.add(new RingRoadBorer());
    }

    @Override
    public String getPackage() {
        return "higtools";
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(Main);
        Modules.registerCategory(Borers);
    }
}
