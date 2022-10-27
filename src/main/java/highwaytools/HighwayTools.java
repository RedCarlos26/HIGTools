package highwaytools;

import highwaytools.commands.Center;
import highwaytools.commands.Coordinates;
import highwaytools.commands.Disconnect;
import highwaytools.modules.hud.BindsHud;
import highwaytools.modules.hud.GreetingsHud;
import highwaytools.modules.main.*;
import highwaytools.modules.borers.*;
import highwaytools.modules.kmain.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static highwaytools.AdapterKt.*;

public class HighwayTools extends MeteorAddon {
    public static String VERSION = "2.2";
	public static final Logger LOG = LoggerFactory.getLogger("Highway Tools");
    public static final Category MAIN = new Category("Highway Tools", Items.NETHERITE_PICKAXE.getDefaultStack());
    public static final Category BORERS = new Category(" Borers ", Items.NETHERITE_PICKAXE.getDefaultStack());
    public static final HudGroup HUD = new HudGroup("Highway Tools");

	@Override
	public void onInitialize() {
	    LOG.info("Initializing HighwayTools-Meteor " + HighwayTools.VERSION);

		MeteorClient.EVENT_BUS.registerLambdaFactory("highwaytools", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        setCs2Ps();

        // Modules
        Modules modules = Modules.get();

        // Main (Java)
        modules.add(new AfkLogout());
        modules.add(new ArmorNotify());
        modules.add(new AutoWalkPlus());
        modules.add(new ChatPrefix());
        modules.add(new DiscordRPC());
        modules.add(new HandManager());
        modules.add(new HighwayBuilderPlus());
        modules.add(new highwaytools.modules.main.HighwayTools());
        modules.add(new TPSSync());
        // Main (Kotlin)
        modules.add(new AutoCenter());
        modules.add(new InvManager());
        modules.add(new NoCaveCulling());
        modules.add(AutoEatPlus.INSTANCE);
        modules.add(ScaffoldPlus.INSTANCE);

        // Borers (Kotlin)
        modules.add(new AxisBorer());
        modules.add(new NegNegBorer());
        modules.add(new NegPosBorer());
        modules.add(new PosNegBorer());
        modules.add(new PosPosBorer());
        modules.add(new RingRoadBorer());

        // Commands
        Commands commands = Commands.get();
        commands.add(new Center());
        commands.add(new Coordinates());
        commands.add(new Disconnect());

        // HUD
        Hud hud = Systems.get(Hud.class);
        hud.register(BindsHud.INFO);
        hud.register(GreetingsHud.INFO);
	}

    @Override
    public String getPackage() {
        return "highwaytools";
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(MAIN);
        Modules.registerCategory(BORERS);
    }
}
