package bloodandmithril.ui.components.window;

import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Topography;

/**
 * A {@link Window} that displays graphical and text information about the {@link Topography}
 *
 * @author Matt
 */
public class TopographyWindow extends Window {

	/** The tile coordinates of the internal 'topography camera', this represents the bottom left corner of the window */
	private int topoX, topoY;
	
	/**
	 * Constructor
	 */
	public TopographyWindow() {
		super(BloodAndMithrilClient.WIDTH/2 - 400, BloodAndMithrilClient.HEIGHT/2 + 300, 800, 600, "Topography", true, 800, 600, false, false);
	}

	
	@Override
	protected void internalWindowRender() {
		UserInterface.shapeRenderer.begin(ShapeType.Rectangle);
		for (Structure struct : Structures.getStructures().values()) {
			UserInterface.shapeRenderer.setColor(Color.GREEN);
			if (struct instanceof SuperStructure) {
				Boundaries boundaries = ((SuperStructure) struct).getBoundaries();
				UserInterface.shapeRenderer.rect(
					boundaries.left * Topography.TILE_SIZE - topoX, 
					boundaries.bottom * Topography.TILE_SIZE - topoY, 
					boundaries.getWidth() * Topography.TILE_SIZE, 
					boundaries.getHeight() * Topography.TILE_SIZE
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
			
			UserInterface.shapeRenderer.setColor(Color.ORANGE);
			for (Entry<Integer, ConcurrentHashMap<Integer, Chunk>> outerEntry : Topography.chunkMap.getChunkMap().entrySet()) {
				for (Entry<Integer, Chunk> innerEntry : outerEntry.getValue().entrySet()) {
					UserInterface.shapeRenderer.rect(
						outerEntry.getKey() * Topography.TILE_SIZE - topoX, 
						innerEntry.getKey() * Topography.TILE_SIZE - topoY, 
						Topography.TILE_SIZE, 
						Topography.TILE_SIZE
					);
				}
			}
		}
		UserInterface.shapeRenderer.end();
	}

	
	@Override
	protected void internalLeftClick(List<ContextMenu> copy, Deque<Component> windowsCopy) {
	}

	
	@Override
	protected void uponClose() {
	}

	
	@Override
	public boolean keyPressed(int keyCode) {
		if (keyCode == Input.Keys.LEFT) {
			topoX = topoX - 20;
			return true;
		}
		if (keyCode == Input.Keys.RIGHT) {
			topoX = topoX + 20;
			return true;
		}
		return false;
	}

	
	@Override
	public void leftClickReleased() {
	}
}