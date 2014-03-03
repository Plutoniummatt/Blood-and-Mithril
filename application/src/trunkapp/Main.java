package trunkapp;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.persistence.ConfigPersistenceService;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * The entry point of the game
 *
 * @author Matt
 */
public class Main {
	public static BloodAndMithrilClient client;
	
	public static void main(String[] args) {

	  //Configurations
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Blood and Mithril";
		cfg.useGL20 = true;
		cfg.samples = 4;
		cfg.width = ConfigPersistenceService.getConfig().getResX();
		cfg.height = ConfigPersistenceService.getConfig().getResY();
		cfg.resizable = false;
		cfg.addIcon("data/image/icon.png", FileType.Internal);
		cfg.addIcon("data/image/smallIcon.png", FileType.Internal);

		client = new BloodAndMithrilClient();
		new LwjglApplication(client, cfg);
	}
}
