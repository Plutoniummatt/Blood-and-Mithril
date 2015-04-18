package bloodandmithril.graphics.background;

import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.badlogic.gdx.Gdx.files;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Shaders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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

	static {
		if (ClientServerInterface.isClient()) {
			backgrounds = new Texture(files.internal("data/image/bg.png"));
			textures.put(1, new TextureRegion(backgrounds, 473, 0, 10, 75));
			textures.put(2, new TextureRegion(backgrounds, 0, 0, 473, 75));
			textures.put(3, new TextureRegion(backgrounds, 483, 0, 29, 38));
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
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.pass);
		spriteBatch.draw(textures.get(1), 0, 0, BloodAndMithrilClient.WIDTH, Layer.getScreenHorizonY() - 1);
		spriteBatch.end();
		
		for (Layer layer : layers) {
			spriteBatch.begin();
			layer.preRender();
			layer.render(
				(int) cam.position.x,
				(int) cam.position.y
			);
			spriteBatch.end();
		}
	}
}