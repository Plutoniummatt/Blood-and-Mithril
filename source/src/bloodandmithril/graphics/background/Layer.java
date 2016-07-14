package bloodandmithril.graphics.background;

import static bloodandmithril.graphics.background.BackgroundImages.textures;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
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
	protected Layer(final TreeMap<Integer, WrapperForTwo<Integer, Integer>> images) {
		this.images = images;
	}


	public static int getScreenHorizonY(final Graphics graphics) {
		final int calculated = 550 - Math.round((int) graphics.getCam().position.y * (1f - 0.95f));

		return pegToHorizon(calculated, graphics, 0);
	}


	private static int pegToHorizon(final int y, final Graphics graphics, final int offset) {
		return Math.max(
			Math.min(y, graphics.getHeight() * 3 / 4 + offset),
			graphics.getHeight() / 4
		);
	}


	/**
	 * Renders this layer
	 */
	public void render(final int camX, final int camY, final Graphics graphics) {
		int startPositionX = Math.round((camX - graphics.getWidth() / 2) * (1f - getDistanceX()));
		final int startPositionY = Math.round(camY * (1f - getDistanceY()));
		int currentPosition = 0;

		final Entry<Integer, WrapperForTwo<Integer, Integer>> floorEntry = images.floorEntry(startPositionX);
		if (floorEntry == null) {
			return;
		}
		final float startingRenderingPosition = (camX - graphics.getWidth()/2) * (1f - getDistanceX()) - floorEntry.getKey();

		boolean rendering = true;
		while(rendering) {
			if (currentPosition - startingRenderingPosition > graphics.getWidth()) {
				rendering = false;
			}

			final boolean empty = images.floorEntry(startPositionX + currentPosition).getValue().a == BackgroundImages.EMPTY;
			final TextureRegion toDraw = empty ? null : textures.get(images.floorEntry(startPositionX + currentPosition).getValue().a);

			if (toDraw != null) {
				graphics.getSpriteBatch().draw(
					toDraw,
					currentPosition - startingRenderingPosition,
					pegToHorizon((int) (getOffsetY() - startPositionY + images.floorEntry(startPositionX + currentPosition).getValue().b), graphics, -10)
				);
			}

			currentPosition += empty ? 200 : toDraw.getRegionWidth();
			startPositionX = images.floorEntry(startPositionX).getKey();
		}
	}


	/**
	 * Applies pre-render logic like shaders
	 */
	public abstract void preRender(Graphics graphics);


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