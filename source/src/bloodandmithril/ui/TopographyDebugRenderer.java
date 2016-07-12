package bloodandmithril.ui;

import static bloodandmithril.control.InputUtilities.isKeyPressed;
import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Topography;

/**
 * Displays graphical and text information about the {@link Topography}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class TopographyDebugRenderer {

	/** The tile coordinates of the internal 'topography camera', this represents the bottom left corner of the window */
	private int topoX, topoY;

	@Inject private GameClientStateTracker gameClientStateTracker;

	/**
	 * Renders the topography map
	 */
	public void render(final Graphics graphics) {
		UserInterface.shapeRenderer.begin(ShapeType.Line);
		graphics.getSpriteBatch().begin();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		for (final Structure struct : Structures.getStructures().values()) {
			if (struct.worldId != gameClientStateTracker.getActiveWorldId()) {
				continue;
			}

			UserInterface.shapeRenderer.setColor(Color.GREEN);
			if (struct instanceof SuperStructure) {
				final Boundaries boundaries = ((SuperStructure) struct).getBoundaries();
				Fonts.defaultFont.draw(graphics.getSpriteBatch(), Integer.toString(struct.getStructureKey()), boundaries.left * Topography.CHUNK_SIZE - topoX + 5, boundaries.top * Topography.CHUNK_SIZE - topoY + 10);
				Fonts.defaultFont.draw(graphics.getSpriteBatch(), struct.getClass().getSimpleName(), boundaries.left * Topography.CHUNK_SIZE - topoX + 5, boundaries.top * Topography.CHUNK_SIZE - topoY - 15);
				UserInterface.shapeRenderer.rect(
					boundaries.left * Topography.CHUNK_SIZE - topoX,
					boundaries.bottom * Topography.CHUNK_SIZE - topoY,
					boundaries.getWidth() * Topography.CHUNK_SIZE,
					boundaries.getHeight() * Topography.CHUNK_SIZE
				);

				for (int x = convertToWorldTileCoord(((SuperStructure) struct).getBoundaries().left, 0); x <= convertToWorldTileCoord(((SuperStructure) struct).getBoundaries().right, Topography.CHUNK_SIZE - 1); x++) {
					UserInterface.shapeRenderer.circle(x - topoX + 0.5f, ((SuperStructure) struct).getSurfaceHeight().apply(x) - topoY, 1);
				}
			}

			UserInterface.shapeRenderer.setColor(Color.RED);
			for (final bloodandmithril.generation.component.Component component : struct.getComponents()) {
				final Boundaries boundaries = component.getBoundaries();
				UserInterface.shapeRenderer.rect(
					boundaries.left - topoX,
					boundaries.bottom - topoY,
					boundaries.getWidth(),
					boundaries.getHeight()
				);
			}
		}
		graphics.getSpriteBatch().end();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		UserInterface.shapeRenderer.setColor(Colors.modulateAlpha(Color.CYAN, 0.2f));
		for (final Entry<Integer, HashMap<Integer, Chunk>> outerEntry : gameClientStateTracker.getActiveWorld().getTopography().getChunkMap().getChunkMap().entrySet()) {
			for (final Entry<Integer, Chunk> innerEntry : outerEntry.getValue().entrySet()) {
				UserInterface.shapeRenderer.rect(
					outerEntry.getKey() * Topography.CHUNK_SIZE - topoX,
					innerEntry.getKey() * Topography.CHUNK_SIZE - topoY,
					Topography.CHUNK_SIZE,
					Topography.CHUNK_SIZE
				);
			}
		}

		UserInterface.shapeRenderer.setColor(Color.ORANGE);
		UserInterface.shapeRenderer.rect(
			(graphics.getCam().position.x - graphics.getWidth() / 2) / Topography.TILE_SIZE - topoX,
			(graphics.getCam().position.y - graphics.getHeight() / 2) / Topography.TILE_SIZE - topoY,
			graphics.getWidth() / Topography.TILE_SIZE,
			graphics.getHeight() / Topography.TILE_SIZE
		);

		UserInterface.shapeRenderer.end();

		if (isKeyPressed(Keys.CONTROL_LEFT) && BloodAndMithrilClient.devMode) {
			if (isKeyPressed(Input.Keys.LEFT)) {
				topoX = topoX - 10;
			}
			if (isKeyPressed(Input.Keys.RIGHT)) {
				topoX = topoX + 10;
			}
			if (isKeyPressed(Input.Keys.DOWN)) {
				topoY = topoY - 10;
			}
			if (isKeyPressed(Input.Keys.UP)) {
				topoY = topoY + 10;
			}
		}
	}
}