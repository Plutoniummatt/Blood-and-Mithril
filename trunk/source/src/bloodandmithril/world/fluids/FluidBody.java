package bloodandmithril.world.fluids;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * A body of fluid
 *
 * @author Matt, Sam
 */
@Copyright("Matthew Peck 2014")
public class FluidBody implements Serializable {
	private static final long serialVersionUID = 8315959113525547208L;

	private volatile float volume;
	private final ConcurrentSkipListMap<Integer, LinkedList<Integer>> occupiedCoordinates = new ConcurrentSkipListMap<>();
	private Boundaries bindingBox = new Boundaries(0, 0, 0, 0);


	/**
	 * Constructor
	 */
	public FluidBody(Map<Integer, LinkedList<Integer>> occupiedCoordinates, float volume) {
		this.occupiedCoordinates.putAll(occupiedCoordinates);
		this.volume = volume;
		update();
	}


	/**
	 * Renders this {@link FluidBody}, called from main thread
	 */
	public void render() {
		// Split the occupied coordinates into y-layers
		float workingVolume = volume;
		for (Entry<Integer, LinkedList<Integer>> layer : occupiedCoordinates.entrySet()) {
			float renderVolume;
			if (workingVolume < layer.getValue().size()) {
				renderVolume = workingVolume / layer.getValue().size();
			} else {
				renderVolume = 1f;
				workingVolume -= layer.getValue().size();
			}

			Domain.shapeRenderer.begin(ShapeType.FilledRectangle);
			Domain.shapeRenderer.setColor(0f, 0.5f, 0.8f, 0.6f);
			Domain.shapeRenderer.setProjectionMatrix(BloodAndMithrilClient.cam.combined);
			for (int x : layer.getValue()) {
				renderFluidElement(x, layer.getKey(), renderVolume);
			}
			Domain.shapeRenderer.end();
		}
	}


	/**
	 * Renders this {@link FluidBody}s binding box
	 */
	public void renderBindingBox() {
		Domain.shapeRenderer.begin(ShapeType.Rectangle);
		Domain.shapeRenderer.setColor(Color.RED);
		Domain.shapeRenderer.setProjectionMatrix(BloodAndMithrilClient.cam.combined);
		Domain.shapeRenderer.rect(
			convertToWorldCoord(bindingBox.left, true),
			convertToWorldCoord(bindingBox.bottom, true),
			convertToWorldCoord(bindingBox.right + 1, true) - convertToWorldCoord(bindingBox.left, true),
			convertToWorldCoord(bindingBox.top + 1, true) - convertToWorldCoord(bindingBox.bottom, true)
		);
		Domain.shapeRenderer.end();
	}


	/**
	 * Updates this {@link FluidBody}
	 */
	public void update() {
		Integer minX = null, maxX = null;

		for (Entry<Integer, LinkedList<Integer>> layer : occupiedCoordinates.entrySet()) {
			for (int x : layer.getValue()) {
				if (minX == null) {
					minX = x;
				}

				if (maxX == null) {
					maxX = x;
				}

				if (x < minX) {
					minX = x;
				}

				if (x > maxX) {
					maxX = x;
				}
			}
		}
		bindingBox.bottom = occupiedCoordinates.firstKey();
		bindingBox.top = occupiedCoordinates.lastKey();
		bindingBox.left = minX;
		bindingBox.right = maxX;
	}


	/**
	 * Renders an individual fluid element
	 */
	private void renderFluidElement(int x, int y, float volume) {
		Domain.shapeRenderer.filledRect(
			convertToWorldCoord(x, true),
			convertToWorldCoord(y, true),
			TILE_SIZE,
			TILE_SIZE * volume
		);
	}
}