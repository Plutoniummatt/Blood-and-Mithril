package bloodandmithril.graphics.background;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static com.badlogic.gdx.Gdx.files;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Shaders;

/**
 * Class to manage background images
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class BackgroundImages implements Serializable {
	private static final long serialVersionUID = -649236314540206654L;
	private static Texture backgrounds;

	public static Map<Integer, TextureRegion> textures = Maps.newHashMap();
	private List<Layer> layers = Lists.newArrayList();

	public static final int EMPTY = 0;
	public static final int OCEAN = 1;
	public static final int ISLAND = 2;
	public static final int SHIP = 3;

	static {
		if (ClientServerInterface.isClient()) {
			backgrounds = new Texture(files.internal("data/image/bg.png"));
			backgrounds.setFilter(TextureFilter.Linear, TextureFilter.Nearest);
			textures.put(OCEAN, new TextureRegion(backgrounds, 474, 0, 10, 75));
			textures.put(ISLAND, new TextureRegion(backgrounds, 0, 0, 473, 75));
			textures.put(SHIP, new TextureRegion(backgrounds, 485, 0, 58, 44));
		}
	}

	public BackgroundImages() {
		layers.add(new DayLightColorLayerWithFluidReflections());
	}

	/**
	 * Renders the background images
	 */
	public void renderBackground() {
		// Render the sea
		getGraphics().getSpriteBatch().begin();
		getGraphics().getSpriteBatch().setShader(Shaders.pass);
		getGraphics().getSpriteBatch().draw(textures.get(OCEAN), 0, 0, getGraphics().getWidth(), Layer.getScreenHorizonY() - 1);
		getGraphics().getSpriteBatch().end();

		for (Layer layer : layers) {
			getGraphics().getSpriteBatch().begin();
			layer.preRender();
			layer.render(
				(int) getGraphics().getCam().position.x,
				(int) getGraphics().getCam().position.y
			);
			getGraphics().getSpriteBatch().end();
		}
	}
}