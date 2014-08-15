package bloodandmithril.generation.superstructure;

import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;
import static java.lang.Math.max;

import java.util.HashMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.TerrainGenerator;
import bloodandmithril.generation.component.components.Corridor;
import bloodandmithril.generation.component.components.Room;
import bloodandmithril.generation.component.components.Stairs;
import bloodandmithril.generation.component.components.Corridor.CorridorCreationCustomization;
import bloodandmithril.generation.component.components.Room.RoomCreationCustomization;
import bloodandmithril.generation.component.components.Stairs.StairsCreationCustomization;
import bloodandmithril.generation.component.components.prefab.UndergroundDesertTempleEntrance;
import bloodandmithril.generation.patterns.Layers;
import bloodandmithril.generation.patterns.UndergroundWithCaves;
import bloodandmithril.generation.tools.PerlinNoiseGenerator1D;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.generation.tools.SawToothGenerator;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.util.Function;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;
import bloodandmithril.world.topography.tile.tiles.sedimentary.SandTile;
import bloodandmithril.world.topography.tile.tiles.soil.DryDirtTile;
import bloodandmithril.world.topography.tile.tiles.stone.SandStoneTile;

/**
 * The structure of a desert surface to be stored and used to generate when needed
 *
 * @author Sam, Matt
 */
@Copyright("Matthew Peck 2014")
public class Desert extends SuperStructure {
	private static final long serialVersionUID = 4034191268168150728L;

	/** Generates the wavey surface of the desert */
	private final PerlinNoiseGenerator1D perlinSurfaceGenerator = new PerlinNoiseGenerator1D(30, ParameterPersistenceService.getParameters().getSeed(), 1, 0f);

	/** The boundary from which sand is generated */
	private final HashMap<Integer, Integer> sandBase = new HashMap<>();

	/** The boundary from which dry dirt is generated */
	private final HashMap<Integer, Integer> transitionBase = new HashMap<>();

	/** Dimensions of this {@link Desert} */
	private int cWidth, cHeight, tDuneVariationHeight, tSandStoneDepth, tTransitionWidth;


	/**
	 * @param worldId - The ID of the world.
	 * @param cWidth - The Width of the desert in chunks.
	 * @param cHeight - The Height of the desert in chunks.
	 * @param tDuneVariationHeight - How much the surface height can vary by.
	 * @param tSandStoneDepth - The world coordinate of the tile height where normal underground continues.
	 * @param tTransitionWidth - The width in tiles of the dry soil areas either side of the sand.
	 */
	public Desert(int worldId, int cWidth, int cHeight, int tDuneVariationHeight, int tSandStoneDepth, int tTransitionWidth) {
		super(worldId);
		this.cWidth = cWidth;
		this.cHeight = cHeight;
		this.tDuneVariationHeight = tDuneVariationHeight;
		this.tSandStoneDepth = tSandStoneDepth;
		this.tTransitionWidth = tTransitionWidth;
	}


	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		//calculates where the structure can go
		return RectangularSpaceCalculator.calculateBoundariesConfineWithinTwoHeights(
			true,
			startingChunkX,
			startingChunkY,
			cWidth,
			cHeight,
			TerrainGenerator.maxSurfaceHeightInChunks,
			TerrainGenerator.maxSurfaceHeightInChunks - cHeight,
			Domain.getWorld(worldId).getTopography()
		);
	}


	@Override
	protected void internalGenerate(boolean generatingToRight) {
		int rightMostTile = (getBoundaries().right + 1) * Topography.CHUNK_SIZE - 1;
		int leftMostTile = getBoundaries().left * Topography.CHUNK_SIZE;

		generateSurface(generatingToRight, rightMostTile, leftMostTile);
		generateTransitionBase(generatingToRight, rightMostTile, leftMostTile);
		generateSandBase(generatingToRight, rightMostTile, leftMostTile);
		generateDungeon();
	}


	/** Generates the desert dungeon */
	private void generateDungeon() {
		// Add the entrance in the middle of this desert
		int entranceX = (getBoundaries().left + getBoundaries().right) / 2 * Topography.CHUNK_SIZE;
		getComponents().add(new UndergroundDesertTempleEntrance(
			entranceX,
			max(
				Domain.getWorld(worldId).getTopography().getStructures().getSurfaceHeight().get(366 + entranceX) + 80,
				Domain.getWorld(worldId).getTopography().getStructures().getSurfaceHeight().get(24 + entranceX) + 80
			),
			getStructureKey(),
			false,
			YellowBrickTile.class,
			YellowBrickTile.class
		));

		getComponents().get(0).stem(
			this,
			Stairs.class,
			new Function<StairsCreationCustomization>() {
				@Override
				public StairsCreationCustomization call() {
					return new StairsCreationCustomization(
						false,
						false,
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextInt(30) + 20,
						1,
						17,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					);
				}
			}
		).stem(
			this,
			Stairs.class,
			new Function<StairsCreationCustomization>() {
				@Override
				public StairsCreationCustomization call() {
					return new StairsCreationCustomization(
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextInt(30) + 20,
						1,
						17,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					);
				}
			}
		).stem(
			this,
			Stairs.class,
			new Function<StairsCreationCustomization>() {
				@Override
				public StairsCreationCustomization call() {
					return new StairsCreationCustomization(
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						true,
						Util.getRandom().nextInt(30) + 20,
						1,
						17,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					);
				}
			}
		).stem(
			this,
			Corridor.class,
			new Function<CorridorCreationCustomization>() {
				@Override
				public CorridorCreationCustomization call() {
					return new CorridorCreationCustomization(
						Util.getRandom().nextBoolean(),
						3,
						3,
						20 + Util.getRandom().nextInt(10),
						17,
						YellowBrickTile.class
					);
				}
			}
		).stem(
			this,
			Room.class,
			new Function<RoomCreationCustomization>() {
				@Override
				public RoomCreationCustomization call() {
					return new RoomCreationCustomization(
						Util.getRandom().nextBoolean(),
						20 + Util.getRandom().nextInt(10),
						20 + Util.getRandom().nextInt(10),
						3,
						YellowBrickTile.class
					);
				}
			}
		).stem(
			this,
			Stairs.class,
			new Function<StairsCreationCustomization>() {
				@Override
				public StairsCreationCustomization call() {
					return new StairsCreationCustomization(
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextInt(20) + 15,
						1,
						17,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					);
				}
			},
			10
		);
	}


	/** Generates the surface layer */
	private void generateSurface(boolean generatingToRight, int rightMostTile, int leftMostTile) {
		Structures structures = Domain.getWorld(worldId).getTopography().getStructures();
		int startingHeight;
		int dafaultSurfaceHeight = 100;

		// set starting height
		if (generatingToRight) {
			if (structures.getSurfaceHeight().get(leftMostTile - 1) != null) { 
				startingHeight = structures.getSurfaceHeight().get(leftMostTile - 1);
			} else {
				startingHeight = dafaultSurfaceHeight;
			}
		} else {
			if (structures.getSurfaceHeight().get(rightMostTile + 1) != null) {
				startingHeight = structures.getSurfaceHeight().get(rightMostTile + 1);
			} else {
				startingHeight = dafaultSurfaceHeight;
			}
		}

		//fill surfaceHeight
		if (generatingToRight) {
			for (int x = leftMostTile; x <= rightMostTile; x++) {
				structures.getSurfaceHeight().put(
					x,
					(int)(startingHeight + tDuneVariationHeight * perlinSurfaceGenerator.generate(x, 1) - tDuneVariationHeight * perlinSurfaceGenerator.generate(leftMostTile, 1))
				);
			}
		} else {
			for (int x = rightMostTile; x >= leftMostTile; x--) {
				structures.getSurfaceHeight().put(
					x,
					(int)(startingHeight + tDuneVariationHeight * perlinSurfaceGenerator.generate(x, 1) - tDuneVariationHeight * perlinSurfaceGenerator.generate(rightMostTile, 1))
				);
			}
		}
	}


	/** Generate transition layer */
	private void generateTransitionBase(boolean generatingToRight, int rightMostTile, int leftMostTile) {
		Structures structures = Domain.getWorld(worldId).getTopography().getStructures();
		HashMap<Integer, Integer> left = new HashMap<>();
		HashMap<Integer, Integer> right = new HashMap<>();

		int depth = getBoundaries().bottom + tSandStoneDepth;
		SawToothGenerator transitionBaseGenerator = new SawToothGenerator(depth, depth + 20, 3, 2, 150);
		if (generatingToRight) {
			right.put(leftMostTile, structures.getSurfaceHeight().get(leftMostTile));
			for (int x = leftMostTile + 1; x <= rightMostTile; x++) {
				transitionBaseGenerator.generateSurfaceHeight(x, generatingToRight, right);
			}
			left.put(rightMostTile, structures.getSurfaceHeight().get(rightMostTile));
			for (int x = rightMostTile - 1; x >= leftMostTile; x--) {
				transitionBaseGenerator.generateSurfaceHeight(x, !generatingToRight, left);
			}
		} else {
			left.put(rightMostTile, structures.getSurfaceHeight().get(rightMostTile));
			for (int x = rightMostTile - 1; x >= leftMostTile; x--) {
				transitionBaseGenerator.generateSurfaceHeight(x, generatingToRight, left);
			}
			right.put(leftMostTile, structures.getSurfaceHeight().get(leftMostTile));
			for (int x = leftMostTile + 1; x <= rightMostTile; x++) {
				transitionBaseGenerator.generateSurfaceHeight(x, !generatingToRight, right);
			}
		}
		for (int x = leftMostTile; x <= rightMostTile; x++) {
			transitionBase.put(x, Math.max(right.get(x), left.get(x)));
		}
	}


	/** Generate sand layer */
	private void generateSandBase(boolean generatingToRight, int rightMostTile, int leftMostTile) {
		Structures structures = Domain.getWorld(worldId).getTopography().getStructures();
		HashMap<Integer, Integer> left = new HashMap<>();
		HashMap<Integer, Integer> right = new HashMap<>();
		SawToothGenerator sandBaseGenerator = new SawToothGenerator(convertToWorldTileCoord(getBoundaries().bottom, 0), convertToWorldTileCoord(getBoundaries().bottom, 0) + 20, 2, 1, 100);
		if (generatingToRight) {
			right.put(leftMostTile + tTransitionWidth, structures.getSurfaceHeight().get(leftMostTile + tTransitionWidth));
			for (int x = leftMostTile + tTransitionWidth+ 1; x <= rightMostTile - tTransitionWidth; x++) {
				sandBaseGenerator.generateSurfaceHeight(x, generatingToRight, right);
			}
			left.put(rightMostTile - tTransitionWidth, structures.getSurfaceHeight().get(rightMostTile - tTransitionWidth));
			for (int x = rightMostTile - tTransitionWidth - 1; x >= leftMostTile + tTransitionWidth; x--) {
				sandBaseGenerator.generateSurfaceHeight(x, !generatingToRight, left);
			}
		} else {
			left.put(rightMostTile - tTransitionWidth, structures.getSurfaceHeight().get(rightMostTile - tTransitionWidth));
			for (int x = rightMostTile - tTransitionWidth - 1; x >= leftMostTile + tTransitionWidth; x--) {
				sandBaseGenerator.generateSurfaceHeight(x, generatingToRight, left);
			}
			right.put(leftMostTile + tTransitionWidth, structures.getSurfaceHeight().get(leftMostTile + tTransitionWidth));
			for (int x = leftMostTile + tTransitionWidth+ 1; x <= rightMostTile - tTransitionWidth; x++) {
				sandBaseGenerator.generateSurfaceHeight(x, !generatingToRight, right);
			}
		}
		for (int x = leftMostTile + tTransitionWidth; x <= rightMostTile - tTransitionWidth; x++) {
			sandBase.put(x, Math.max(right.get(x), left.get(x)));
		}
	}


	@Override
	protected Tile internalGetForegroundTile(int worldTileX, int worldTileY) {
		Structures structures = Domain.getWorld(worldId).getTopography().getStructures();

		if (worldTileY > structures.getSurfaceHeight().get(worldTileX)) {
			return new Tile.EmptyTile();
		} else if (worldTileY > transitionBase.get(worldTileX)) {
			if (sandBase.get(worldTileX) == null) {
				return new DryDirtTile();
			}
			if (worldTileY > sandBase.get(worldTileX)) {
				return new SandTile();
			} else {
				return new DryDirtTile();
			}
		} else {
			if (sandBase.get(worldTileX) == null) {
				return UndergroundWithCaves.getTile(worldTileX, worldTileY);
			}
			if (worldTileY > sandBase.get(worldTileX)) {
				return new SandStoneTile();
			} else {
				return UndergroundWithCaves.getTile(worldTileX, worldTileY);
			}
		}
	}


	@Override
	protected Tile internalGetBackgroundTile(int worldTileX, int worldTileY) {
		Structures structures = Domain.getWorld(worldId).getTopography().getStructures();

		if (worldTileY > structures.getSurfaceHeight().get(worldTileX)-1) {
			return new Tile.EmptyTile();

		} else if (worldTileY > transitionBase.get(worldTileX)) {
			if (sandBase.get(worldTileX) == null) {
				return new DryDirtTile();
			}
			if (worldTileY <= transitionBase.get(worldTileX) + 2) {
				if (worldTileY >= sandBase.get(worldTileX) + 2) {
					return new SandStoneTile();
				} else {
					return new DryDirtTile();
				}
			} else {
				if (worldTileY >= sandBase.get(worldTileX) + 2) {
					return new SandStoneTile();
				} else {
					return new DryDirtTile();
				}
			}
		} else {
			if (sandBase.get(worldTileX) == null) {
				return Layers.getTile(worldTileX, worldTileY);
			}
			if (worldTileY > sandBase.get(worldTileX)) {
				return new SandStoneTile();
			} else {
				return Layers.getTile(worldTileX, worldTileY);
			}
		}
	}
}