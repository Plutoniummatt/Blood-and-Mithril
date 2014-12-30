package bloodandmithril.world.fluids;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A body of fluid
 *
 * @author Matt, Sam
 */
@Copyright("Matthew Peck 2014")
public class FluidBody implements Serializable {
	private static final long serialVersionUID = 8315959113525547208L;

	private volatile float volume;
	private final ConcurrentSkipListMap<Integer, Set<Integer>> occupiedCoordinates = new ConcurrentSkipListMap<>();
	private final int worldId;
	private Boundaries bindingBox = new Boundaries(0, 0, 0, 0);

	/** The height at which surface tension is no longer able to prevent the fluid from spreading */
	private static final float spreadHeight = 0.1f;

	/** How fast the fluid in this body of water will evaporate per second per tile exposed to air that has a non-passable tile underneath */
	private static final float evaporationRate = 0.0003f;


	/**
	 * Constructor
	 */
	public FluidBody(Map<Integer, Set<Integer>> occupiedCoordinates, float volume, int worldId) {
		this.worldId = worldId;
		this.occupiedCoordinates.putAll(occupiedCoordinates);
		this.volume = volume;
		update();
	}


	/**
	 * Renders this {@link FluidBody}, called from main thread
	 */
	public void render() {
		Domain.shapeRenderer.begin(ShapeType.FilledRectangle);
		Domain.shapeRenderer.setColor(0f, 0.2f, 1f, 0.92f);
		Domain.shapeRenderer.setProjectionMatrix(BloodAndMithrilClient.cam.combined);
		// Split the occupied coordinates into y-layers
		float workingVolume = volume;
		for (Entry<Integer, Set<Integer>> layer : occupiedCoordinates.entrySet()) {
			float renderVolume;
			if (workingVolume < layer.getValue().size()) {
				renderVolume = workingVolume / layer.getValue().size();
				workingVolume = 0f;
			} else {
				renderVolume = 1f;
				workingVolume -= layer.getValue().size();
			}

			for (int x : Lists.newLinkedList(layer.getValue())) {
				renderFluidElement(x, layer.getKey(), renderVolume);
			}
		}
		Domain.shapeRenderer.end();
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
	public synchronized void update() {
		// Update binding box.
		// Remove rows when fluid is no longer occupying a row.
		Integer minX = null, maxX = null;
		float workingVolume = volume;
		float topLayerVolume = 0f;
		for (Entry<Integer, Set<Integer>> layer : occupiedCoordinates.entrySet()) {

			if (workingVolume == 0f) {
				occupiedCoordinates.remove(layer.getKey());
				continue;
			}

			if (workingVolume < layer.getValue().size()) {
				topLayerVolume = workingVolume / layer.getValue().size();
				workingVolume = 0f;
			} else {
				workingVolume -= layer.getValue().size();
			}
			
			final int y = layer.getKey();

			for (int x : Lists.newLinkedList(layer.getValue())) {
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
				
				// Flow and Spread
				boolean tileBelowPassable = Domain.getWorld(worldId).getTopography().getTile(x, y - 1, true).isPassable();
				boolean tileBelowOccupied = hasFluid(x, y - 1);
				if (tileBelowPassable && !tileBelowOccupied) {
					if (occupiedCoordinates.containsKey(y - 1)) {
						occupiedCoordinates.get(y - 1).add(x);
					} else {
						Set<Integer> newRow = Sets.newConcurrentHashSet();
						newRow.add(x);
						occupiedCoordinates.put(y - 1, newRow);
					}
				} else if ((workingVolume != 0f || topLayerVolume > spreadHeight) && pillarTouchingFloor(x, y)) {
					if (Domain.getWorld(worldId).getTopography().getTile(x + 1, y, true).isPassable()) {
						layer.getValue().add(x + 1);
					}
					
					if (Domain.getWorld(worldId).getTopography().getTile(x - 1, y, true).isPassable()) {
						layer.getValue().add(x - 1);
					}
				}
				
				if (Domain.getWorld(worldId).getTopography().getTile(x, y + 1, true).isPassable() && workingVolume > 0f) {
					if (occupiedCoordinates.containsKey(y + 1)) {
						occupiedCoordinates.get(y + 1).add(x);
					}
				}
			}
		}
		bindingBox.bottom = occupiedCoordinates.firstKey();
		bindingBox.top = occupiedCoordinates.lastKey();
		bindingBox.left = minX;
		bindingBox.right = maxX;

		if (workingVolume > 0f) {
			// Make sure the fluid has "elasticity", i.e. it will expand when compressed.
			Entry<Integer, Set<Integer>> lastEntry = occupiedCoordinates.lastEntry();
			final int y = lastEntry.getKey() + 1;
			Set<Integer> newRow = Sets.newConcurrentHashSet();
			for (int x : lastEntry.getValue()) {
				if (Domain.getWorld(worldId).getTopography().getTile(x, y, true).isPassable()) {
					newRow.add(x);
				}
			}
			occupiedCoordinates.put(y, newRow);
		}

		// Evaporation
		Entry<Integer, Set<Integer>> lastEntry = occupiedCoordinates.lastEntry();
		final int y = lastEntry.getKey();
		for (int x : lastEntry.getValue()) {
			if (Domain.getWorld(worldId).getTopography().getTile(x, y + 1, true).isPassable()) {
				volume -= evaporationRate / 60f;
			}
		}
	}
	
	
	/**
	 * @return whether the specified coordinates is part of a pillar of fluid whose base sits on a non passable tile
	 */
	private boolean pillarTouchingFloor(int x, int y) {
		while (hasFluid(x, y)) {
			y = y - 1;
		}
		
		return !Domain.getWorld(worldId).getTopography().getTile(x, y, true).isPassable();
	}


	/**
	 * @return whether the specified coordates are occupied
	 */
	private boolean hasFluid(int x, int y) {
		Set<Integer> row = occupiedCoordinates.get(y);
		
		if (row == null) {
			return false;
		} else {
			return row.contains(x);
		}
	}


	/**
	 * Add to this fluid body
	 */
	public void add(float volume) {
		this.volume += volume;
	}


	/**
	 * Subtract from this fluid body
	 */
	public float subtract(float volume) {
		float subtracted = 0f;
		if (volume > this.volume) {
			subtracted = this.volume;
			this.volume = 0f;
		} else {
			subtracted = volume;
			this.volume -= volume;
		}
		return subtracted;
	}


	/**
	 * @return whether a world tile coordinate is adjacent to an occupied fluid coordinate or inside a body of fluid
	 */
	private boolean isTileCoordinateAdjacentOrInside(int x, int y) {
		if (x > bindingBox.right + 1 ||
			y > bindingBox.top + 1 ||
			x < bindingBox.left - 1 ||
			y < bindingBox.bottom - 1) {
			return false;
		}

		if (occupiedCoordinates.containsKey(y)) {
			if (occupiedCoordinates.get(y).contains(x) || occupiedCoordinates.get(y).contains(x + 1) || occupiedCoordinates.get(y).contains(x - 1)) {
				return true;
			}
		}

		if (occupiedCoordinates.containsKey(y + 1)) {
			if (occupiedCoordinates.get(y + 1).contains(x)) {
				return true;
			}
		}

		if (occupiedCoordinates.containsKey(y - 1)) {
			if (occupiedCoordinates.get(y - 1).contains(x)) {
				return true;
			}
		}

		return false;
	}


	/**
	 * Checks whether this {@link FluidBody} can flow into a newly created space.
	 */
	public void newSpace(int x, int y) {
		if (isTileCoordinateAdjacentOrInside(x, y)) {
			if (occupiedCoordinates.get(y) == null) {
				Set<Integer> row = Sets.newLinkedHashSet();
				row.add(x);
				occupiedCoordinates.put(y, row);
			} else {
				occupiedCoordinates.get(y).add(x);
			}
		}
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