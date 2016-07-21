package bloodandmithril.application;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
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
		// Configurations
		final LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Blood and Mithril";
		cfg.useGL30 = false;
		cfg.samples = 4;
		cfg.width = ConfigPersistenceService.getConfig().getResX();
		cfg.height = ConfigPersistenceService.getConfig().getResY();
		cfg.fullscreen = ConfigPersistenceService.getConfig().isFullScreen();
		cfg.resizable = true;
		cfg.addIcon("data/image/smallIcon.png", FileType.Internal);
		cfg.addIcon("data/image/icon.png", FileType.Internal);
		cfg.allowSoftwareMode = false;

		BloodAndMithrilClient.devMode = true;
		client = new BloodAndMithrilClient();

		new LwjglApplication(client, cfg);
	}
}