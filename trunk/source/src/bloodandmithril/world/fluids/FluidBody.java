package bloodandmithril.world.fluids;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldCoord;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
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
	public static volatile boolean paused = false;

	static {
		fluidThread = new Thread(() -> {
			long prevFrame = System.currentTimeMillis();

			while (true) {
				if (paused) {
					try {
						Thread.sleep(10);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}

					continue;
				}
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
		Domain.shapeRenderer.setColor(0.2f, 0.5f, 1f, 0.8f);
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
	public void update(boolean suppressSplitting) {
		if (volume <= 0f || occupiedCoordinates.isEmpty()) {
			Domain.getWorld(worldId).removeFluid(this);
			return;
		}

		float workingVolume = volume;
		float topLayerVolume = 0f;
		boolean layerRemoved = false;
		boolean possibleTileSplit = false;
		boolean suppressTopSpread = false;
		boolean merged = false;
		Set<TwoInts> downflowSuppression = Sets.newLinkedHashSet();
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
			for (int x : Lists.newLinkedList(layer.getValue())) {
				FluidBody overlapsWith = overlapsWith(x, y);
				FluidBody overlapsWithLeft = overlapsWith(x - 1, y);
				FluidBody overlapsWithRight = overlapsWith(x + 1, y);

				boolean sideRemoval = false;
				if (overlapsWithLeft != null) {
					sideRemoval = overlapsWithLeft.occupiedCoordinates.lastKey() == y || sideRemoval;
					downflowSuppression.add(new TwoInts(x, y + 1));
				}
				if (overlapsWithRight != null) {
					sideRemoval = overlapsWithRight.occupiedCoordinates.lastKey() == y || sideRemoval;
					downflowSuppression.add(new TwoInts(x, y + 1));
				}

				if (!Domain.getWorld(worldId).getTopography().getTile(x, y, true).isPassable() || overlapsWith != null && overlapsWith.occupiedCoordinates.lastKey() >= y || sideRemoval) {
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
				boolean tileBelowOccupied = hasFluidInThisFluid(x, y - 1);
				if (tileBelowPassable && !tileBelowOccupied && !downflowSuppression.contains(new TwoInts(x, y))) {
					if (occupiedCoordinates.containsKey(y - 1)) {
						FluidBody transferWith = overlapsWith(x, y - 1);
						if (transferWith == null) {
							suppressTopSpread = occupiedCoordinates.get(y - 1).add(x) || suppressTopSpread;
						} else {
							TransferState transferState = transferWith(transferWith);
							if (transferState == TransferState.MERGED) {
								merged = true;
								break;
							} else {
								suppressTopSpread = transferState == TransferState.TRANSFERRED_FROM || suppressTopSpread;
								continue;
							}
						}
					} else {
						FluidBody transferWith = overlapsWith(x, y - 1);;
						if (transferWith == null) {
							Set<Integer> newRow = Sets.newConcurrentHashSet();
							newRow.add(x);
							occupiedCoordinates.put(y - 1, newRow);
							suppressTopSpread = true;
						} else {
							TransferState transferState = transferWith(transferWith);
							if (transferState == TransferState.MERGED) {
								merged = true;
								break;
							} else {
								suppressTopSpread = transferState == TransferState.TRANSFERRED_FROM || suppressTopSpread;
								continue;
							}
						}
					}
				} else if ((workingVolume != 0f || topLayerVolume > spreadHeight) && (pillarTouchingFloor(x, y) || !tileRightBottomPassable || !tileLeftBottomPassable)) {
					if (tileRightPassable && (!tileBelowPassable || hasFluidInThisFluid(x, y - 1) && !tileRightBottomPassable)) {
						FluidBody transferWith = overlapsWith(x + 1, y);
						if (transferWith == null) {
							suppressTopSpread = layer.getValue().add(x + 1) || suppressTopSpread;
						} else {
							TransferState transferState = transferWith(transferWith);
							if (transferState == TransferState.MERGED) {
								merged = true;
								break;
							} else {
								suppressTopSpread = transferState == TransferState.TRANSFERRED_FROM || suppressTopSpread;
								continue;
							}
						}
					}

					if (tileLeftPassable && (!tileBelowPassable || hasFluidInThisFluid(x, y - 1) && !tileLeftBottomPassable)) {
						FluidBody transferWith = overlapsWith(x - 1, y);
						if (transferWith == null) {
							suppressTopSpread = layer.getValue().add(x - 1) || suppressTopSpread;
						} else {
							TransferState transferState = transferWith(transferWith);
							if (transferState == TransferState.MERGED) {
								merged = true;
								break;
							} else {
								suppressTopSpread = transferState == TransferState.TRANSFERRED_FROM || suppressTopSpread;
								continue;
							}
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
			entry.setValue(Sets.newLinkedHashSet(entry.getValue()));
		});
		workingVolume = volume;

		if (!suppressTopSpread && downflowSuppression.isEmpty()) {
			for (Entry<Integer, Set<Integer>> layer : mapCopy.entrySet()) {

				if (workingVolume < layer.getValue().size()) {
					topLayerVolume = workingVolume / layer.getValue().size();
					workingVolume = 0f;
				} else {
					workingVolume -= layer.getValue().size();
				}

				int y = layer.getKey();
				boolean rowAdded = false;
				for (int x : Lists.newLinkedList(layer.getValue())) {
					boolean rightPassable = Domain.getWorld(worldId).getTopography().getTile(x + 1, y, true).isPassable();
					boolean leftPassable = Domain.getWorld(worldId).getTopography().getTile(x - 1, y, true).isPassable();
					if (Domain.getWorld(worldId).getTopography().getTile(x, y + 1, true).isPassable() && workingVolume > 0f &&
						(hasFluidInThisFluid(x - 1, y) || !leftPassable) && (hasFluidInThisFluid(x + 1, y) || !rightPassable)) {
						if (occupiedCoordinates.containsKey(y + 1)) {
							rowAdded = occupiedCoordinates.get(y + 1).add(x) || rowAdded;
						} else {
							LinkedHashSet<Integer> newRow = Sets.newLinkedHashSet();
							newRow.add(x);
							occupiedCoordinates.put(y + 1, newRow);
							rowAdded = true;
						}
					}
				}

				if (rowAdded) {
					for (int x : Lists.newLinkedList(occupiedCoordinates.get(y + 1))) {
						boolean rightPassable = Domain.getWorld(worldId).getTopography().getTile(x + 1, y + 1, true).isPassable();
						boolean leftPassable = Domain.getWorld(worldId).getTopography().getTile(x - 1, y + 1, true).isPassable();
						boolean rightBottomPassable = Domain.getWorld(worldId).getTopography().getTile(x + 1, y, true).isPassable();
						boolean leftBottomPassable = Domain.getWorld(worldId).getTopography().getTile(x - 1, y, true).isPassable();
						if ((hasFluidInThisFluid(x - 1, y) || !leftBottomPassable) && (hasFluidInThisFluid(x + 1, y) || !rightBottomPassable)) {
							if (!hasFluidInThisFluid(x - 1, y + 1) && leftPassable) {
								occupiedCoordinates.get(y + 1).add(x - 1);
							} else if (!hasFluidInThisFluid(x + 1, y + 1) && rightPassable) {
								occupiedCoordinates.get(y + 1).add(x + 1);
							}
						}
					}
				}
			}
		}

		if (volume <= 0f || occupiedCoordinates.isEmpty()) {
			Domain.getWorld(worldId).removeFluid(this);
			return;
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
	 * @param handles fluid transfer logic with another {@link FluidBody}
	 */
	private TransferState transferWith(FluidBody other) {
		if (this.occupiedCoordinates.lastKey() > other.occupiedCoordinates.lastKey()) {
			other.add(subtract(1f));
			return TransferState.TRANSFERRED_FROM;
		} else if (this.occupiedCoordinates.lastKey() < other.occupiedCoordinates.lastKey()) {
			add(other.subtract(1f));
			return TransferState.TRANSFERRED_TO;
		} else {
			merge(other);
			return TransferState.MERGED;
		}
	}


	/**
	 * Checks whether this {@link FluidBody} is flowing into another {@link FluidBody}, returning the {@link FluidBody} to transfer with.
	 */
	private FluidBody overlapsWith(int x, int y) {
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
						return fluid;
					} else {
						continue;
					}
				}
			}
		}

		return null;
	}


	/**
	 * @param merges this body with another
	 */
	private void merge(FluidBody other) {
		System.out.println(hashCode() + "is merging with " + other.hashCode());
		this.volume = this.volume + other.volume;

		for (Entry<Integer, Set<Integer>> otherEntry : other.occupiedCoordinates.entrySet()) {
			Set<Integer> thisRow = this.occupiedCoordinates.get(otherEntry.getKey());
			if (thisRow == null) {
				this.occupiedCoordinates.put(otherEntry.getKey(), otherEntry.getValue());
			} else {
				thisRow.addAll(otherEntry.getValue());
			}
		}

		Domain.getWorld(worldId).removeFluid(other);
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

		if (fragments.size() == 1) {
			return;
		}

		System.out.println(hashCode() + "Has split");

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
		while (hasFluidInThisFluid(x, y)) {
			y = y - 1;
		}

		return !Domain.getWorld(worldId).getTopography().getTile(x, y, true).isPassable();
	}


	/**
	 * @return whether the specified coordinates are occupied
	 */
	private boolean hasFluidInThisFluid(int x, int y) {
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
		boolean left = false;
		boolean right = false;
		if (Domain.getWorld(worldId).getTopography().getTile(x - 1, y, true).isSmoothCeiling()) {
			left = true;
		}
		if (Domain.getWorld(worldId).getTopography().getTile(x + 1, y, true).isSmoothCeiling()) {
			right = true;
		}

		Domain.shapeRenderer.filledRect(
			convertToWorldCoord(x - (left ? 1 : 0), true),
			convertToWorldCoord(y, true),
			TILE_SIZE + (right ? TILE_SIZE : 0),
			TILE_SIZE * volume
		);
	}


	private enum TransferState {
		TRANSFERRED_FROM, TRANSFERRED_TO, MERGED;
	}
}