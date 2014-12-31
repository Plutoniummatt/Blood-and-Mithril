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
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

	private static Thread fluidThread;

	static {
		fluidThread = new Thread(() -> {
			long prevFrame = System.currentTimeMillis();

			while (true) {
				try {
					Thread.sleep(1);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				if (System.currentTimeMillis() - prevFrame > 70) {
					prevFrame = System.currentTimeMillis();
					Domain.updateFluids();
				}
			}
		});

		fluidThread.setName("Fluid thread");
		fluidThread.start();
	}


	/**
	 * Constructor
	 */
	public FluidBody(Map<Integer, Set<Integer>> occupiedCoordinates, float volume, int worldId) {
		this.worldId = worldId;
		this.occupiedCoordinates.putAll(occupiedCoordinates);
		this.volume = volume;
		updateBindingBox();
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

			for (int x : layer.getValue()) {
				renderFluidElement(x, layer.getKey(), renderVolume);
			}
		}
		Domain.shapeRenderer.end();
	}


	/**
	 * Renders this {@link FluidBody}, called from main thread
	 */
	public void renderElementBoxes() {
		Domain.shapeRenderer.begin(ShapeType.Rectangle);
		Domain.shapeRenderer.setColor(0f, 1f, 0f, 1f);
		Domain.shapeRenderer.setProjectionMatrix(BloodAndMithrilClient.cam.combined);
		// Split the occupied coordinates into y-layers
		for (Entry<Integer, Set<Integer>> layer : occupiedCoordinates.entrySet()) {
			for (int x : Lists.newLinkedList(layer.getValue())) {
				Domain.shapeRenderer.rect(
					convertToWorldCoord(x, true),
					convertToWorldCoord(layer.getKey(), true),
					TILE_SIZE,
					TILE_SIZE
				);
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
	public synchronized void update(boolean suppressSplitting) {
		float workingVolume = volume;
		float topLayerVolume = 0f;
		boolean layerRemoved = false;
		for (Entry<Integer, Set<Integer>> layer : Lists.newLinkedList(occupiedCoordinates.entrySet())) {

			// Remove rows when fluid is no longer occupying a row.
			if (workingVolume == 0f) {
				occupiedCoordinates.remove(layer.getKey());
				// After this step, this fluid body may have to be split into multiple bodies
				layerRemoved = true;
				continue;
			}

			if (workingVolume < layer.getValue().size()) {
				topLayerVolume = workingVolume / layer.getValue().size();
				workingVolume = 0f;
			} else {
				workingVolume -= layer.getValue().size();
			}

			final int y = layer.getKey();
			boolean merged = false;
			for (int x : Lists.newLinkedList(layer.getValue())) {
				// Flow and Spread
				boolean tileBelowPassable = Domain.getWorld(worldId).getTopography().getTile(x, y - 1, true).isPassable();
				boolean tileBelowOccupied = hasFluid(x, y - 1);
				if (tileBelowPassable && !tileBelowOccupied) {
					if (occupiedCoordinates.containsKey(y - 1)) {
						occupiedCoordinates.get(y - 1).add(x);
						if (checkMerge(x, y - 1)) {
							merged = true;
							break;
						}
					} else {
						Set<Integer> newRow = Sets.newConcurrentHashSet();
						newRow.add(x);
						occupiedCoordinates.put(y - 1, newRow);
						if (checkMerge(x, y - 1)) {
							merged = true;
							break;
						}
					}
				} else if ((workingVolume != 0f || topLayerVolume > spreadHeight) && pillarTouchingFloor(x, y)) {
					if (Domain.getWorld(worldId).getTopography().getTile(x + 1, y, true).isPassable()) {
						layer.getValue().add(x + 1);
						if (checkMerge(x + 1, y)) {
							merged = true;
							break;
						}
					}

					if (Domain.getWorld(worldId).getTopography().getTile(x - 1, y, true).isPassable()) {
						layer.getValue().add(x - 1);
						if (checkMerge(x - 1, y)) {
							merged = true;
							break;
						}
					}
				}

				if (Domain.getWorld(worldId).getTopography().getTile(x, y + 1, true).isPassable() && workingVolume > 0f) {
					if (occupiedCoordinates.containsKey(y + 1)) {
						occupiedCoordinates.get(y + 1).add(x);
					}
				}
			}
			if (merged) {
				break;
			}
		}

		updateBindingBox();

		// Evaporation
		Entry<Integer, Set<Integer>> lastEntry = occupiedCoordinates.lastEntry();
		final int y = lastEntry.getKey();
		for (int x : lastEntry.getValue()) {
			if (Domain.getWorld(worldId).getTopography().getTile(x, y + 1, true).isPassable()) {
				volume -= evaporationRate / 60f;
			}
		}

		if (layerRemoved && !suppressSplitting) {
			calculatePossibleSplit();
		}
	}


	/**
	 * Checks whether this {@link FluidBody} is flowing into another {@link FluidBody} and handles merging.
	 */
	private boolean checkMerge(int x, int y) {
		for (FluidBody fluid : Domain.getWorld(worldId).getFluids()) {
			if (fluid == this) {
				continue;
			}

			if (fluid.bindingBox.isWithin(x, y)) {
				Set<Integer> row = fluid.occupiedCoordinates.get(y);
				if (row == null) {
					continue;
				} else {
					if (row.contains(x)) {
						fluid.merge(this);
						Domain.getWorld(worldId).removeFluid(this);
						return true;
					} else {
						continue;
					}
				}
			}
		}

		return false;
	}


	private void merge(FluidBody other) {
		this.volume = this.volume + other.volume;

		for (Entry<Integer, Set<Integer>> otherEntry : other.occupiedCoordinates.entrySet()) {
			Set<Integer> thisRow = this.occupiedCoordinates.get(otherEntry.getKey());
			if (thisRow == null) {
				this.occupiedCoordinates.put(otherEntry.getKey(), otherEntry.getValue());
			} else {
				thisRow.addAll(otherEntry.getValue());
			}
		}
	}


	/**
	 * Updates the binding box of this {@link FluidBody}.
	 */
	private void updateBindingBox() {
		Integer minX = null, maxX = null;
		for (Entry<Integer, Set<Integer>> layer : Lists.newLinkedList(occupiedCoordinates.entrySet())) {
			// Update binding box.
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
			}
		}

		bindingBox.bottom = occupiedCoordinates.firstKey();
		bindingBox.top = occupiedCoordinates.lastKey();
		bindingBox.left = minX;
		bindingBox.right = maxX;
	}


	/**
	 * Calculate and performs splitting into multiple {@link FluidBody}s
	 */
	private void calculatePossibleSplit() {
		Map<Integer, Set<Integer>> occupiedCoordinatesCopy = Maps.newHashMap();

		for (Entry<Integer, Set<Integer>> entry : occupiedCoordinates.entrySet()) {
			occupiedCoordinatesCopy.put(new Integer(entry.getKey()), Sets.newHashSet(entry.getValue()));
		}

		Map<Set<TwoInts>, Float> fragments = Maps.newHashMap();
		while (!occupiedCoordinatesCopy.isEmpty()) {
  			fragments.put(extractFragment(occupiedCoordinatesCopy), 0f);
		}

		int totalElements = fragments.keySet().stream().mapToInt(set -> {
			return set.size();
		}).sum();

		fragments.entrySet().forEach(entry -> {
			entry.setValue(volume * entry.getKey().size() / totalElements);
		});

		World world = Domain.getWorld(worldId);
		world.removeFluid(this);
		for (Entry<Set<TwoInts>, Float> fragment : fragments.entrySet()) {
			world.addFluid(new FluidBody(convertToFluidBodyMap(fragment.getKey()), fragment.getValue(), world.getWorldId()));
		}
	}


	/**
	 * Converts and returns a set of coordinates into a {@link FluidBody}
	 */
	private Map<Integer, Set<Integer>> convertToFluidBodyMap(Set<TwoInts> fragmentElements) {
		Map<Integer, Set<Integer>> map = Maps.newHashMap();

		for (TwoInts element : fragmentElements) {
			if (map.containsKey(element.b)) {
				map.get(element.b).add(new Integer(element.a));
			} else {
				Set<Integer> newRow = Sets.newConcurrentHashSet();
				newRow.add(element.a);
				map.put(new Integer(element.b), newRow);
			}
		}

		return map;
	}


	/**
	 * @return an extracted fluid body fragment
	 */
	private Set<TwoInts> extractFragment(Map<Integer, Set<Integer>> occupiedCoordinatesCopy) {
		Set<TwoInts> fragment = null;
		for (Entry<Integer, Set<Integer>> entry : occupiedCoordinatesCopy.entrySet()) {
			int y = entry.getKey();
			for (int x : entry.getValue()) {
				fragment = findFragment(x, y);
				break;
			}
			break;
		}

		if (fragment != null && !fragment.isEmpty()) {
			fragment.stream().forEach(element -> {
				Set<Integer> row = occupiedCoordinatesCopy.get(element.b);
				row.remove(element.a);
				if (row.isEmpty()) {
					occupiedCoordinatesCopy.remove(element.b);
				}
			});
		}

		return fragment;
	}


	/**
	 * @return a set of coordinates
	 */
	private Set<TwoInts> findFragment(int x, int y) {
		Set<TwoInts> fragment = Sets.newLinkedHashSet();
		Set<Integer> row = occupiedCoordinates.get(y);

		if (row == null) {
			throw new RuntimeException("Fragment seed is not occupied");
		} else {
			processFragmentElement(fragment, x, y);
		}

		return fragment;
	}


	private void processFragmentElement(Set<TwoInts> fragment, int x, int y) {
		if (!occupiedCoordinates.containsKey(y)) {
			return;
		}

		if (fragment.contains(new TwoInts(x, y))) {
			return;
		}

		if (occupiedCoordinates.get(y).contains(x)) {
			fragment.add(new TwoInts(x, y));
		} else {
			return;
		}

		processFragmentElement(fragment, x - 1, y);
		processFragmentElement(fragment, x + 1, y);
		processFragmentElement(fragment, x, y + 1);
		processFragmentElement(fragment, x, y - 1);
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