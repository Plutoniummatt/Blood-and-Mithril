package bloodandmithril.graphics.background;

import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.badlogic.gdx.Gdx.files;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Shaders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Class to manage background images
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class BackgroundImages {

	private static Texture backgrounds;
	
	public static TextureRegion repeatingOcean;
	
	static {
		if (ClientServerInterface.isClient()) {
			backgrounds = new Texture(files.internal("data/image/repeatingOcean.png"));
			repeatingOcean = new TextureRegion(backgrounds);
		}
	}
	
	private static Background ocean = new Ocean();
	
	/**
	 * Renders the background images
	 */
	public static void renderBackground() {
		spriteBatch.begin();
		spriteBatch.setShader(Shaders.pass);
		
		float start = getX() * (1f - ocean.getDistanceX());
		for (float x = start; x + ocean.getTextureRegion().getRegionWidth() <= WIDTH; x += ocean.getTextureRegion().getRegionWidth()) {
			ocean.preRender(spriteBatch);
			spriteBatch.draw(
				repeatingOcean, 
				x, 
				getY() * (1f - ocean.getDistanceY()) + ocean.getOffsetY()
			);	
		}
		
		spriteBatch.end();
	}
	
	
	private static int getX() {
		return  - (int) BloodAndMithrilClient.cam.position.x;
	}
	
	private static int getY() {
		return  - (int) BloodAndMithrilClient.cam.position.y;
	}
	
	
	public static abstract class Background {
		public abstract TextureRegion getTextureRegion();
		
		/**
		 * @return the perceived distance, 0 being the same as foreground, 1 being infinity
		 */
		public abstract float getDistanceX();
		
		/**
		 * @return the perceived distance, 0 being the same as foreground, 1 being infinity
		 */
		public abstract float getDistanceY();
		
		/**
		 * @return the height offset
		 */
		public abstract float getOffsetY();
		
		/**
		 * Applies shaders and other pre-render processes
		 */
		public abstract void preRender(SpriteBatch spriteBatch);
	}
}