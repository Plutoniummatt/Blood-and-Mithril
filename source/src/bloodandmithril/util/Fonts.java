package bloodandmithril.util;

import bloodandmithril.core.Copyright;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * Font utility class
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Fonts {

	/** The default font to use */
	public static BitmapFont defaultFont;

	/**
	 * Loads all fonts
	 */
	public static void setup() {
		// Default font
		FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/default.ttf"));

		defaultFont = fontGenerator.generateFont(15);
		defaultFont.setColor(Color.WHITE);

		fontGenerator.dispose();
	}
}
