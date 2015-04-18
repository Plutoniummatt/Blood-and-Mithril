package bloodandmithril.graphics.background;

import static bloodandmithril.core.BloodAndMithrilClient.WIDTH;
import static bloodandmithril.core.BloodAndMithrilClient.cam;
import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.graphics.background.BackgroundImages.textures;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.TreeMap;

import bloodandmithril.core.Copyright;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A background layer
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Layer implements Serializable {
	private static final long serialVersionUID = 5749079115895910672L;

	/**
	 * Maps the position of the x-coordinate of the image, to the image ID
	 */
	private final TreeMap<Integer, Integer> images;

	/**
	 * Constructor
	 */
	protected Layer(TreeMap<Integer, Integer> images) {
		this.images = images;
	}


	public static int getScreenHorizonY() {
		return 400 - Math.round((int) cam.position.y * (1f - 0.95f));
	}


	public static int getCameraYForHorizonCoord(int horizon) {
		return Math.round((400f - horizon)/(1f - 0.95f));
	}


	/**
	 * Renders this layer
	 */
	public void render(int camX, int camY) {
		int startPositionX = Math.round((camX - WIDTH / 2) * (1f - getDistanceX()));
		int startPositionY = Math.round(camY * (1f - getDistanceY()));
		int currentPosition = 0;

		Entry<Integer, Integer> floorEntry = images.floorEntry(startPositionX);
		if (floorEntry == null) {
			return;
		}
		float startingRenderingPosition = (camX - WIDTH/2) * (1f - getDistanceX()) - floorEntry.getKey();

		boolean rendering = true;
		while(rendering) {
			if (currentPosition - startingRenderingPosition > WIDTH) {
				rendering = false;
			}

			boolean empty = images.floorEntry(startPositionX + currentPosition).getValue() == 0;
			TextureRegion toDraw = empty ? null : textures.get(images.floorEntry(startPositionX + currentPosition).getValue());

			if (toDraw != null) {
				spriteBatch.draw(
					toDraw,
					currentPosition - startingRenderingPosition,
					getOffsetY() - startPositionY
				);
			}

			currentPosition += empty ? 200 : toDraw.getRegionWidth();
		}
	}


	/**
	 * Applies pre-render logic like shaders
	 */
	public abstract void preRender();


	/**
	 * @return the perceived distance of this layer from foreground, 1f is infinity
	 */
	public abstract float getDistanceX();


	/**
	 * @return the perceived distance of this layer from foreground, 1f is infinity
	 */
	public abstract float getDistanceY();


	/**
	 * @return the vertical render offset
	 */
	public abstract float getOffsetY();
}