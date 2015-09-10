package trunkapp;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
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
@Copyright("Matthew Peck 2014")
public class Main {
	public static BloodAndMithrilClient client;

	public static void main(String[] args) {

	  //Configurations
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Blood and Mithril";
		cfg.useGL30 = false;
		cfg.samples = 4;
		cfg.width = ConfigPersistenceService.getConfig().getResX();
		cfg.height = ConfigPersistenceService.getConfig().getResY();
		cfg.fullscreen = ConfigPersistenceService.getConfig().isFullScreen();
		cfg.resizable = true;
		cfg.addIcon("data/image/smallIcon.png", FileType.Internal);
		cfg.addIcon("data/image/icon.png", FileType.Internal);

		BloodAndMithrilClient.devMode = true;
		client = new BloodAndMithrilClient() {
			@Override
			public boolean keyDown(int keycode) {
				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && keycode == Input.Keys.D) {
					UserInterface.addLayeredComponentUnique(
						new DevWindow(
							BloodAndMithrilClient.getGraphics().getWidth(),
							BloodAndMithrilClient.getGraphics().getHeight()/2 + 150,
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