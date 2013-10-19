package trunkapp;


import bloodandmithril.Fortress;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * The entry point of the game
 *
 * @author Matt
 */
public class Main {
	public static void main(String[] args) {

	  //Configurations
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Spritestar";
		cfg.useGL20 = true;
		cfg.samples = 4;
		try {
			cfg.width = Integer.parseInt(System.getProperty("resX"));
			cfg.height = Integer.parseInt(System.getProperty("resY"));
		} catch (NumberFormatException e) {
			throw new IllegalStateException("set -DresX and -DresY JVM Arguments as resolution");
			//For exmaple:
			//		-DresX=1900
			//		-DresY=1000
		}
		cfg.resizable = false;
		cfg.addIcon("data/image/icon.png", FileType.Internal);
		cfg.addIcon("data/image/smallIcon.png", FileType.Internal);

		new LwjglApplication(new Fortress(), cfg);
	}
}
