package bloodandmithril.application;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.CommonModule;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.ConfigPersistenceService;

/**
 * The entry point of the game
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Main {
	public static BloodAndMithrilClient client;

	public static void main(final String args[]) throws Exception {
		BloodAndMithrilClient.devMode = true;
		setupInjector();
		startGameClient(new BloodAndMithrilClient());
	}
	
	
	public static void startGameClient(ApplicationListener applicationListener) {
		// Configurations
		final LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Blood and Mithril";
		cfg.useGL30 = false;
		cfg.samples = 4;
		cfg.width = Wiring.injector().getInstance(ConfigPersistenceService.class).getConfig().getResX();
		cfg.height = Wiring.injector().getInstance(ConfigPersistenceService.class).getConfig().getResY();
		cfg.fullscreen = Wiring.injector().getInstance(ConfigPersistenceService.class).getConfig().isFullScreen();
		cfg.resizable = true;
		cfg.addIcon("data/image/smallIcon.png", FileType.Internal);
		cfg.addIcon("data/image/icon.png", FileType.Internal);
		cfg.allowSoftwareMode = false;

		
		new LwjglApplication(applicationListener, cfg);
	}
	
	
	private static void setupInjector() {
		ClientServerInterface.setClient(true);
		Wiring.setupInjector(new CommonModule());
	}
}