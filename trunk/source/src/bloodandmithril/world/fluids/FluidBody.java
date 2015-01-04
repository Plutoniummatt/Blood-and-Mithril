package bloodandmithril.world.fluids;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
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

	/** Composition of this {@link FluidBody} */
	private Map<Class<? extends Liquid>, Integer> composition = Maps.newHashMap();

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

				if (System.currentTimeMillis() - prevFrame > 65) {
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
	public FluidBody(Map<Integer, Set<Integer>> occupiedCoordinates, float volume, int worldId, Map<Class<? extends Liquid>, Integer> composition) {
		this.worldId = worldId;
		this.occupiedCoordinates.putAll(occupiedCoordinates);
		this.volume = volume;
		this.composition.putAll(composition);
		updateBindingBox();
	}


	public static FluidBody createForWorldCoordinates(Collection<Vector2> occupiedCoordinates, float volume, Class<? extends Liquid> liquid, int worldId) {
		Collection<TwoInts> tileCoords = Lists.newLinkedList();

		for (Vector2 v : occupiedCoordinates) {
			tileCoords.add(
				new TwoInts(
					Topography.convertToWorldTileCoord(v.x),
					Topography.convertToWorldTileCoord(v.y)
				)
			);
		}

		return createForTileCoordinates(tileCoords, volume, liquid, worldId);
	}


	public static FluidBody createForTileCoordinates(Collection<TwoInts> occupiedCoordinates, float volume, Class<? extends Liquid> liquid, int worldId) {
		Map<Integer, Set<Integer>> coords = Maps.newLinkedHashMap();
		for (TwoInts coordinate : occupiedCoordinates) {
			if (!coords.containsKey(coordinate.b)) {
				coords.put(coordinate.b, Sets.newLinkedHashSet());
			}
			coords.get(coordinate.b).add(coordinate.a);
		}

		HashMap<Class<? extends Liquid>, Integer> fluidType = Maps.newHashMap();
		fluidType.put(liquid, 100);

		return new FluidBody(coords, volume, worldId, fluidType);
	}


	private Color determineColor() {
		Color finalColor = new Color(0, 0, 0, 0);
		for (Entry<Class<? extends Liquid>, Integer> entry : composition.entrySet()) {
			try {
				Liquid liquid = entry.getKey().newInstance();
				finalColor = finalColor.add(liquid.getColor().cpy().mul(0.01f * (float) entry.getValue()));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return finalColor;
	}


	/**
	 * Renders this {@link FluidBody}, called from main thread
	 */
	public void render() {
		Domain.shapeRenderer.begin(ShapeType.FilledRectangle);
		Domain.shapeRenderer.setColor(determineColor());
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
				try {
					renderFluidElement(x, layer.getKey(), renderVolume);
				} catch (NoTileFoundException e) {}
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
	public synchronized void update(boolean suppressSplitting) throws NoTileFoundException {
		float workingVolume = volume;
		float topLayerVolume = 0f;
		boolean layerRemoved = false;
		boolean suppressTopSpread = false;
		boolean possibleTileSplit = false;
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
				if (!Domain.getWorld(worldId).getTopography().getTile(x, y, true).isPassable()) {
					occupiedCoordinates.get(y).remove(x);
					possibleTileSplit = true;
					if (occupiedCoordinates.get(y).isEmpty()) {
						occupiedCoordinates.remove(y);
						layerRemoved = true;
					}
					continue;
				}

				// Flow and Spread
				boolean tileBelowPassable = Domain.getWorld(worldId).getTopography().getTile(x, y - 1, true).isPassable();
				boolean tileRightBottomPassable = Domain.getWorld(worldId).getTopography().getTile(x + 1, y - 1, true).isPassable();
				boolean tileRightPassable = Domain.getWorld(worldId).getTopography().getTile(x + 1, y, true).isPassable();
				boolean tileLeftPassable = Domain.getWorld(worldId).getTopography().getTile(x - 1, y, true).isPassable();
				boolean tileLeftBottomPassable = Domain.getWorld(worldId).getTopography().getTile(x - 1, y - 1, true).isPassable();
				boolean tileBelowOccupied = hasFluid(x, y - 1);
				if (tileBelowPassable && !tileBelowOccupied) {
					if (occupiedCoordinates.containsKey(y - 1)) {
						suppressTopSpread = occupiedCoordinates.get(y - 1).add(x) || suppressTopSpread;
						if (checkMerge(x, y - 1)) {
							merged = true;
							break;
						}
					} else {
						Set<Integer> newRow = Sets.newConcurrentHashSet();
						newRow.add(x);
						occupiedCoordinates.put(y - 1, newRow);
						suppressTopSpread = true;
						if (checkMerge(x, y - 1)) {
							merged = true;
							break;
						}
					}
				} else if ((workingVolume != 0f || topLayerVolume > spreadHeight) && (pillarTouchingFloor(x, y) || !tileRightBottomPassable || !tileLeftBottomPassable)) {
					if (tileRightPassable && (!tileBelowPassable || hasFluid(x, y - 1) && !tileRightBottomPassable)) {
						suppressTopSpread = layer.getValue().add(x + 1) || suppressTopSpread;
						if (checkMerge(x + 1, y)) {
							merged = true;
							break;
						}
					}

					if (tileLeftPassable && (!tileBelowPassable || hasFluid(x, y - 1) && !tileLeftBottomPassable)) {
						suppressTopSpread = layer.getValue().add(x - 1) || suppressTopSpread;
						if (checkMerge(x - 1, y)) {
							merged = true;
							break;
						}
					}
				}
			}

			if (merged) {
				break;
			}
		}

		TreeMap<Integer, Set<Integer>> mapCopy = Maps.newTreeMap(occupiedCoordinates);
		mapCopy.entrySet().stream().forEach(entry -> {
			entry.setValue(Sets.newTreeSet(entry.getValue()));
		});
		workingVolume = volume;
		for (Entry<Integer, Set<Integer>> layer : mapCopy.entrySet()) {

			if (workingVolume < layer.getValue().size()) {
				topLayerVolume = workingVolume / layer.getValue().size();
				workingVolume = 0f;
			} else {
				workingVolume -= layer.getValue().size();
			}

			int y = layer.getKey();
			boolean previousExposed = false;
			Integer previousX = null;
			for (int x : Lists.newLinkedList(layer.getValue())) {
				boolean rightPassable = Domain.getWorld(worldId).getTopography().getTile(x + 1, y, true).isPassable();
				boolean leftPassable = Domain.getWorld(worldId).getTopography().getTile(x - 1, y, true).isPassable();
				boolean exposed = !(hasFluidInMap(x - 1, y, mapCopy) || !leftPassable) && (hasFluidInMap(x + 1, y, mapCopy) || !rightPassable);
				if (!suppressTopSpread && Domain.getWorld(worldId).getTopography().getTile(x, y + 1, true).isPassable() && workingVolume > 0f) {
					if (previousExposed && previousX == x - 1) {
						exposed = true;
					}
					if (!exposed && occupiedCoordinates.containsKey(y + 1)) {
						occupiedCoordinates.get(y + 1).add(x);
					}
					previousExposed = exposed;
					previousX = x;
				}
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

		if ((layerRemoved || possibleTileSplit) && !suppressSplitting) {
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
		float preMergeVolume = this.volume;
		float preMergeOtherVolume = other.volume;
		this.volume = this.volume + other.volume;

		for (Entry<Integer, Set<Integer>> otherEntry : other.occupiedCoordinates.entrySet()) {
			Set<Integer> thisRow = this.occupiedCoordinates.get(otherEntry.getKey());
			if (thisRow == null) {
				this.occupiedCoordinates.put(otherEntry.getKey(), otherEntry.getValue());
			} else {
				thisRow.addAll(otherEntry.getValue());
			}
		}

		Map<Class<? extends Liquid>, Float> fractions = Maps.newHashMap();
		Map<Class<? extends Liquid>, Integer> percentages = Maps.newHashMap();
		for (Entry<Class<? extends Liquid>, Integer> entry : composition.entrySet()) {
			fractions.put(entry.getKey(), preMergeVolume * (float) entry.getValue() * 0.01f);
		}

		for (Entry<Class<? extends Liquid>, Integer> entry : other.composition.entrySet()) {
			Float existing = fractions.get(entry.getKey());
			if (existing == null) {
				fractions.put(entry.getKey(), preMergeOtherVolume * (float) entry.getValue() * 0.01f);
			} else {
				fractions.put(entry.getKey(), existing + preMergeOtherVolume * (float) entry.getValue() * 0.01f);
			}
		}

		for (Entry<Class<? extends Liquid>, Float> fraction : fractions.entrySet()) {
			percentages.put(fraction.getKey(), Math.round(100f * fraction.getValue() / this.volume));
		}

		int total = percentages.values().stream().mapToInt(val -> {return val;}).sum();
		while (total != 100) {
			if (total > 100) {
				Entry<Class<? extends Liquid>, Integer> entry = percentages.entrySet().stream().findAny().get();
				percentages.put(entry.getKey(), entry.getValue() - 1);
				total--;
			} else {
				Entry<Class<? extends Liquid>, Integer> entry = percentages.entrySet().stream().findAny().get();
				percentages.put(entry.getKey(), entry.getValue() + 1);
				total++;
			}
		}

		composition.clear();
		composition.putAll(percentages);
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
			world.addFluid(new FluidBody(convertToFluidBodyMap(fragment.getKey()), fragment.getValue(), world.getWorldId(), composition));
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
	private boolean pillarTouchingFloor(int x, int y) throws NoTileFoundException {
		while (hasFluid(x, y)) {
			y = y - 1;
		}

		return !Domain.getWorld(worldId).getTopography().getTile(x, y, true).isPassable();
	}


	/**
	 * @return whether the specified coordates are occupied
	 */
	private boolean hasFluid(int x, int y) {
		return hasFluidInMap(x, y, occupiedCoordinates);
	}


	/**
	 * @return whether the specified coordates are occupied
	 */
	private boolean hasFluidInMap(int x, int y, Map<Integer, Set<Integer>> map) {
		Set<Integer> row = map.get(y);

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
	private void renderFluidElement(int x, int y, float volume) throws NoTileFoundException {
		boolean left = false;
		boolean right = false;
		if (Domain.getWorld(worldId).getTopography().getTile(x - 1, y, true).isSmoothCeiling() &&
			hasFluid(x - 1, y - 1)) {
			left = true;
		}
		if (Domain.getWorld(worldId).getTopography().getTile(x + 1, y, true).isSmoothCeiling() &&
			hasFluid(x + 1, y - 1)) {
			right = true;
		}

		Domain.shapeRenderer.filledRect(
			convertToWorldCoord(x - (left ? 1 : 0), true),
			convertToWorldCoord(y, true),
			TILE_SIZE + (right ? TILE_SIZE : 0),
			TILE_SIZE * volume
		);
	}
}