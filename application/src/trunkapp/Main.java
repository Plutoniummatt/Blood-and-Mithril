package trunkapp;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.persistence.ConfigPersistenceService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.DevWindow;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
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
		cfg.addIcon("data/image/smallIcon.png", FileType.Internal);
		cfg.addIcon("data/image/icon.png", FileType.Internal);

		client = new BloodAndMithrilClient() {
			@Override
			public boolean keyDown(int keycode) {
				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && keycode == Input.Keys.D) {
					UserInterface.addLayeredComponentUnique(
						new DevWindow(
							WIDTH/2 - 250,
							HEIGHT/2 + 150,
							500,
							300,
							true
						)
					);
				}

				return super.keyDown(keycode);
			}
		};

		new LwjglApplication(client, cfg);
	}
}