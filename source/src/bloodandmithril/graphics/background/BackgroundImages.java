package bloodandmithril.graphics.background;

import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.badlogic.gdx.Gdx.files;

import java.util.List;
import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;

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
public class BackgroundImages {
	private static Texture backgrounds;

	public static Map<Integer, TextureRegion> textures = Maps.newHashMap();
	private static List<Layer> layers = Lists.newArrayList();

	static {
		if (ClientServerInterface.isClient()) {
			backgrounds = new Texture(files.internal("data/image/oceanWithIsland.png"));
			textures.put(1, new TextureRegion(backgrounds, 0, 0, 200, 800));
			textures.put(2, new TextureRegion(backgrounds, 200, 0, 500, 800));

			layers.add(new DayLightColorLayerWithFluidReflections());
		}
	}

	/**
	 * Renders the background images
	 */
	public static void renderBackground() {
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