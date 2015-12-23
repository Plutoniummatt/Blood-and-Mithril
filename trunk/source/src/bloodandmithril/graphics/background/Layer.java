package bloodandmithril.graphics.background;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static bloodandmithril.graphics.background.BackgroundImages.textures;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.WrapperForTwo;

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
	private final TreeMap<Integer, WrapperForTwo<Integer, Integer>> images;

	/**
	 * Constructor
	 */
	protected Layer(TreeMap<Integer, WrapperForTwo<Integer, Integer>> images) {
		this.images = images;
	}


	public static int getScreenHorizonY() {
		return 400 - Math.round((int) getGraphics().getCam().position.y * (1f - 0.95f));
	}


	public static int getCameraYForHorizonCoord(int horizon) {
		return Math.round((400f - horizon)/(1f - 0.95f));
	}


	/**
	 * Renders this layer
	 */
	public void render(int camX, int camY) {
		int startPositionX = Math.round((camX - getGraphics().getWidth() / 2) * (1f - getDistanceX()));
		int startPositionY = Math.round(camY * (1f - getDistanceY()));
		int currentPosition = 0;

		Entry<Integer, WrapperForTwo<Integer, Integer>> floorEntry = images.floorEntry(startPositionX);
		if (floorEntry == null) {
			return;
		}
		float startingRenderingPosition = (camX - getGraphics().getWidth()/2) * (1f - getDistanceX()) - floorEntry.getKey();

		boolean rendering = true;
		while(rendering) {
			if (currentPosition - startingRenderingPosition > getGraphics().getWidth()) {
				rendering = false;
			}

			boolean empty = images.floorEntry(startPositionX + currentPosition).getValue().a == BackgroundImages.EMPTY;
			TextureRegion toDraw = empty ? null : textures.get(images.floorEntry(startPositionX + currentPosition).getValue().a);

			if (toDraw != null) {
				getGraphics().getSpriteBatch().draw(
					toDraw,
					currentPosition - startingRenderingPosition,
					getOffsetY() - startPositionY + images.floorEntry(startPositionX + currentPosition).getValue().b
				);
			}

			currentPosition += empty ? 200 : toDraw.getRegionWidth();
			startPositionX = images.floorEntry(startPositionX).getKey();
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