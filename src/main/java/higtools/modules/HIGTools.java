package higtools.modules;

import higtools.commands.*;
import higtools.modules.hud.*;
import higtools.modules.main.*;
import higtools.utils.*;
import higtools.modules.borers.*;
import higtools.modules.kmain.*;
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

import static higtools.AdapterKt.*;

public class HIGTools extends MeteorAddon {
    public static String VERSION = "1.8";
	public static final Logger LOG = LoggerFactory.getLogger("HIG Tools");
    public static final Category MAIN = new Category("HIG Tools", Items.NETHERITE_PICKAXE.getDefaultStack());
    public static final Category BORERS = new Category(" Borers ", Items.NETHERITE_PICKAXE.getDefaultStack());
    public static final HudGroup HUD = new HudGroup("HIG Tools");

	@Override
	public void onInitialize() {
	    LOG.info("Initializing HIG Tools " + HIGTools.VERSION);

		MeteorClient.EVENT_BUS.registerLambdaFactory("higtools", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        ServiceLoader.load();
        setCs2Ps();

        // Modules
        Modules modules = Modules.get();

        // Main (Java)
        modules.add(new AfkLogout());
        modules.add(new ArmorNotify());
        modules.add(new Aura());
        modules.add(new AutoWalkPlus());
        modules.add(new HIGPrefix());
        modules.add(new DiscordRPC());
        modules.add(new HandManager());
        modules.add(new HighwayBuilderPlus());
        modules.add(new HighwayTools());
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
        commands.add(new Disconnect());
        commands.add(new ToggleModules());

        // HUD
        Hud hud = Systems.get(Hud.class);
        hud.register(BindsHud.INFO);
        hud.register(HIGWelcomeHud.INFO);
        hud.register(SpotifyHud.INFO);
	}

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(MAIN);
        Modules.registerCategory(BORERS);
    }
}
