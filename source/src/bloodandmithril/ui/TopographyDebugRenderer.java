package bloodandmithril.ui;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Topography;

/**
 * Displays graphical and text information about the {@link Topography}
 *
 * @author Matt
 */
public class TopographyDebugRenderer {

	/** The tile coordinates of the internal 'topography camera', this represents the bottom left corner of the window */
	private static int topoX, topoY;
	
	/**
	 * Renders the topography map
	 */
	public static void render() {
		UserInterface.shapeRenderer.begin(ShapeType.Rectangle);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		for (Structure struct : Structures.getStructures().values()) {
			UserInterface.shapeRenderer.setColor(Color.GREEN);
			if (struct instanceof SuperStructure) {
				Boundaries boundaries = ((SuperStructure) struct).getBoundaries();
				UserInterface.shapeRenderer.rect(
					boundaries.left * Topography.CHUNK_SIZE - topoX, 
					boundaries.bottom * Topography.CHUNK_SIZE - topoY, 
					boundaries.getWidth() * Topography.CHUNK_SIZE, 
					boundaries.getHeight() * Topography.CHUNK_SIZE
				);
			}
			
			UserInterface.shapeRenderer.setColor(Color.RED);
			for (bloodandmithril.generation.component.Component component : struct.getComponents()) {
				Boundaries boundaries = component.getBoundaries();
				UserInterface.shapeRenderer.rect(
					boundaries.left - topoX, 
					boundaries.bottom - topoY, 
					boundaries.getWidth(), 
					boundaries.getHeight()
				);
			}
		}
		
		UserInterface.shapeRenderer.setColor(Colors.modulateAlpha(Color.CYAN, 0.2f));
		for (Entry<Integer, ConcurrentHashMap<Integer, Chunk>> outerEntry : Domain.getActiveWorld().getTopography().getChunkMap().getChunkMap().entrySet()) {
			for (Entry<Integer, Chunk> innerEntry : outerEntry.getValue().entrySet()) {
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
			(BloodAndMithrilClient.cam.position.x - BloodAndMithrilClient.WIDTH / 2) / Topography.TILE_SIZE - topoX, 
			(BloodAndMithrilClient.cam.position.y - BloodAndMithrilClient.HEIGHT / 2) / Topography.TILE_SIZE - topoY, 
			BloodAndMithrilClient.WIDTH / Topography.TILE_SIZE, 
			BloodAndMithrilClient.HEIGHT / Topography.TILE_SIZE
		);
		
		UserInterface.shapeRenderer.end();
		
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			topoX = topoX - 10;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			topoX = topoX + 10;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			topoY = topoY - 10;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
			topoY = topoY + 10;
		}
	}
}