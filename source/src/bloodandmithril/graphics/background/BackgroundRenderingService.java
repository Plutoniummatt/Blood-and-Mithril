package bloodandmithril.graphics.background;

import static bloodandmithril.graphics.Textures.BACKGROUND_IMAGES;
import static bloodandmithril.graphics.Textures.backgroundImages;
import static com.badlogic.gdx.Gdx.files;

import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * Contains logic related to background image rendering
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class BackgroundRenderingService {
	private static float HORIZON_DISTANCE_FACTOR = 0.95f;

	public static final int EMPTY = 0;
	public static final int OCEAN = 1;
	public static final int ISLAND = 2;
	public static final int SHIP = 3;
	public static final int FLOATING_ISLAND_CASTLE = 4;

	static {
		if (ClientServerInterface.isClient()) {
			BACKGROUND_IMAGES = new Texture(files.internal("data/image/bg.png"));
			BACKGROUND_IMAGES.setFilter(TextureFilter.Linear, TextureFilter.Nearest);
			backgroundImages.put(OCEAN, new TextureRegion(BACKGROUND_IMAGES, 475, 0, 8, 75));
			backgroundImages.put(ISLAND, new TextureRegion(BACKGROUND_IMAGES, 0, 0, 473, 75));
			backgroundImages.put(SHIP, new TextureRegion(BACKGROUND_IMAGES, 485, 0, 58, 44));
			backgroundImages.put(FLOATING_ISLAND_CASTLE, new TextureRegion(BACKGROUND_IMAGES, 0, 75, 172, 219));
		}
	}
	
	@Inject private Graphics graphics;
	
	/**
	 * @return the screen y-coordinate of the horizon (horizon sea level)
	 */
	public float getHorizonScreenY() {
		final int calculated = 550 - Math.round((int) graphics.getCam().position.y * (1f - HORIZON_DISTANCE_FACTOR));
		return pegToHorizon(calculated, 0);
	}
	
	
	/**
	 * @return pegs the coordinate between 1/4 and 3/4 of screen height.
	 */
	public float pegToHorizon(final float y, float offset) {
		return Math.max(
			Math.min(y, graphics.getHeight() * 3 / 4),
			graphics.getHeight() / 4
		);
	}
	
	
	/**
	 * Renders a {@link Layer} given the camera coordinates
	 * 
	 * @param layer
	 * @param camX
	 * @param camY
	 */
	public void renderLayer(Layer layer, final int camX, final int camY, boolean peggedToHorizon) {
		int startPositionX = Math.round((camX - graphics.getWidth() / 2) * (1f - layer.getDistanceX()));
		final int startPositionY = peggedToHorizon ? (int) -getHorizonScreenY() : Math.round(camY * (1f - layer.getDistanceY()));
		int currentPosition = 0;

		final Entry<Integer, WrapperForTwo<Integer, Integer>> floorEntry = layer.images.floorEntry(startPositionX);
		if (floorEntry == null) {
			return;
		}
		final float startingRenderingPosition = (camX - graphics.getWidth()/2) * (1f - layer.getDistanceX()) - floorEntry.getKey();

		boolean rendering = true;
		while(rendering) {
			if (currentPosition - startingRenderingPosition > graphics.getWidth()) {
				rendering = false;
			}

			final boolean empty = layer.images.floorEntry(startPositionX + currentPosition).getValue().a == EMPTY;
			final TextureRegion toDraw = empty ? null : backgroundImages.get(layer.images.floorEntry(startPositionX + currentPosition).getValue().a);

			if (toDraw != null) {
				Integer backgroundImageOffset = layer.images.floorEntry(startPositionX + currentPosition).getValue().b;
				float layerOffset = layer.getOffsetY();
				
				graphics.getSpriteBatch().draw(
					toDraw,
					currentPosition - startingRenderingPosition,
					layerOffset - startPositionY + backgroundImageOffset
				);
			}

			currentPosition += empty ? 200 : toDraw.getRegionWidth();
			startPositionX = layer.images.floorEntry(startPositionX).getKey();
		}
	}
	
	
	/**
	 * Renders the background images
	 */
	public void renderBackground(BackgroundImages background) {
		// Render the sea
		graphics.getSpriteBatch().begin();
		graphics.getSpriteBatch().setShader(Shaders.pass);
		graphics.getSpriteBatch().draw(backgroundImages.get(OCEAN), 0, 0, graphics.getWidth(), getHorizonScreenY() - 1f);
		graphics.getSpriteBatch().end();

		for (Layer layer : background.layers) {
			graphics.getSpriteBatch().begin();
			layer.preRender(graphics);
			renderLayer(
				layer,
				(int) graphics.getCam().position.x,
				(int) graphics.getCam().position.y,
				layer.peggedToHorizon
			);
			graphics.getSpriteBatch().end();
		}
	}
}