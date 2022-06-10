package higtools.modules;

import higtools.commands.*;
import higtools.modules.main.*;
import higtools.modules.hud.*;
import higtools.modules.player.*;
import higtools.modules.world.*;
import higtools.utils.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static higtools.AdapterKt.*;

public class HIGTools extends MeteorAddon {
	public static final Logger LOG = LoggerFactory.getLogger("HIG Tools");
    public static final String VERSION = "1.1";
    public static final Category HIG = new Category("HIG Tools", Items.NETHERITE_PICKAXE.getDefaultStack());

	@Override
	public void onInitialize() {
	    LOG.info("Initializing HIG Tools");

		MeteorClient.EVENT_BUS.registerLambdaFactory("higtools", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        HTDamageUtils.init();
        ServiceLoader.load();
        PacketFlyUtils.init();
        setCs2Ps();

        // Modules
		// Java
        Modules modules = Modules.get();
        modules.add(new AfkLogout());
        modules.add(new ArmorNotify());
        modules.add(new Aura());
        modules.add(new AutoDisable());
        modules.add(new ChatTweaks());
        modules.add(new DiscordRPC());
        modules.add(new HandManager());
        modules.add(new HighwayBuilderPlus());
        modules.add(new OldAnimations());
        modules.add(new OneClickEat());
        modules.add(new PacketFly());
        modules.add(new Strafe());
        modules.add(new TPSSync());
        // Kotlin
        modules.add(new NetherrackDiggingMontageMaker());
        modules.add(new NoCaveCulling());
        modules.add(Scaffold.INSTANCE);
        modules.add(AutoEat.INSTANCE);
        modules.add(new AutoReconnect());
        modules.add(new InvManager());
        modules.add(new AutoCenter());

        //Commands
        Commands commands = Commands.get();
        commands.add(new ClearChat());
        commands.add(new Kick());

        // HUD
        HUD hud = Systems.get(HUD.class);
        hud.elements.add(new BaritoneHud(hud));
        hud.elements.add(new BindsHud(hud));
        hud.elements.add(new EchestHud(hud));
        hud.elements.add(new GapHud(hud));
        hud.elements.add(new ObbyHud(hud));
        hud.elements.add(new PickHud(hud));
        hud.elements.add(new SpotifyHud(hud));
        hud.elements.add(new XpHud(hud));
	}

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(HIG);
    }

}
