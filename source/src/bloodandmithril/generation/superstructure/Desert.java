package bloodandmithril.generation.superstructure;

import static bloodandmithril.generation.settings.GlobalGenerationSettings.defaultSurfaceHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.desertMaxSandstoneDepth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.desertMaxSurfaceHeightVariation;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.desertMaxTransitionDepth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.desertMaxWidth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.desertMinWidth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.desertTransitionWidth;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxSurfaceHeight;

import java.util.HashMap;

import bloodandmithril.generation.Structures;
import bloodandmithril.generation.component.Corridor;
import bloodandmithril.generation.component.Corridor.CorridorCreationCustomization;
import bloodandmithril.generation.component.Room;
import bloodandmithril.generation.component.Room.RoomCreationCustomization;
import bloodandmithril.generation.component.Stairs;
import bloodandmithril.generation.component.Stairs.StairsCreationCustomization;
import bloodandmithril.generation.component.prefab.UndergroundDesertTempleEntrance;
import bloodandmithril.generation.patterns.Layers;
import bloodandmithril.generation.patterns.UndergroundWithCaves;
import bloodandmithril.generation.tools.PerlinNoiseGenerator1D;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.generation.tools.SawToothGenerator;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.util.Function;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;
import bloodandmithril.world.topography.tile.tiles.sedimentary.YellowSandTile;
import bloodandmithril.world.topography.tile.tiles.soil.DryDirtTile;
import bloodandmithril.world.topography.tile.tiles.stone.SandStoneTile;

/**
 * The structure of a desert surface to be stored and used to generate when needed
 *
 * @author Sam, Matt
 */
public class Desert extends SuperStructure {
	private static final long serialVersionUID = 4034191268168150728L;

	/** Generates the wavey surface of the desert */
	private final PerlinNoiseGenerator1D perlinSurfaceGenerator = new PerlinNoiseGenerator1D(30, ParameterPersistenceService.getParameters().getSeed());

	/** The boundary from which sand is generated */
	private final HashMap<Integer, Integer> sandBase = new HashMap<>();

	/** The boundary from which dry dirt is generated */
	private final HashMap<Integer, Integer> transitionBase = new HashMap<>();

	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		//calculates where the structure can go
		return RectangularSpaceCalculator.calculateBoundaries(
			true,
			startingChunkX,
			startingChunkY,
			(desertMaxWidth - desertMinWidth) / 2 + 1,
			maxSurfaceHeight - desertMaxSandstoneDepth / Topography.CHUNK_SIZE + 1,
			maxSurfaceHeight,
			desertMaxSandstoneDepth / Topography.CHUNK_SIZE - 1
		);
	}

	private static boolean desertGenerated = false; //TODO WTF IS THIS SHIT MANNNNG
	@Override
	protected void internalGenerate(boolean generatingToRight) {
		int rightMostTile = (getBoundaries().right + 1) * Topography.CHUNK_SIZE - 1;
		int leftMostTile = getBoundaries().left * Topography.CHUNK_SIZE;
		
		generateSurface(generatingToRight, rightMostTile, leftMostTile);
		generateTransitionBase(generatingToRight, rightMostTile, leftMostTile);
		generateSandBase(generatingToRight, rightMostTile, leftMostTile);
		
		if (!desertGenerated) {
			getComponents().add(new UndergroundDesertTempleEntrance(0, Structures.getSurfaceHeight().get(100 - 17) + 27, getStructureKey(), false, YellowBrickTile.class, YellowBrickTile.class));

			getComponents().get(0).stem(
				this,
				Corridor.class,
				new Function<CorridorCreationCustomization>() {
					@Override
					public CorridorCreationCustomization call() {
						return new CorridorCreationCustomization(
							false, 
							1, 
							1, 
							150, 
							6, 
							YellowBrickTile.class
						);
					}
				}
			);
			
			getComponents().get(1).stem(
				this, 
				Stairs.class, 
				new Function<StairsCreationCustomization>() {
					@Override
					public StairsCreationCustomization call() {
						return new StairsCreationCustomization(
							Util.getRandom().nextBoolean(),
							Util.getRandom().nextBoolean(),
							Util.getRandom().nextBoolean(),
							Util.getRandom().nextInt(10) + 10,
							1,
							6,
							2,
							YellowBrickTile.class,
							YellowBrickTile.class
						);
					}
				},
				10
			);
			
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
							7,
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
							7,
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
							7,
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
							7,
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
							7,
							3,
							YellowBrickTile.class,
							YellowBrickTile.class
						);
					}
				},
				10
			);
			
			desertGenerated = true;
		}
	}


	/** Generates the surface layer */
	private void generateSurface(boolean generatingToRight, int rightMostTile, int leftMostTile) {
		int startingHeight;

		// set starting height
		if (generatingToRight) {
			if (Structures.getSurfaceHeight().get(leftMostTile - 1) != null) {
				startingHeight = Structures.getSurfaceHeight().get(leftMostTile - 1);
			} else {
				startingHeight = defaultSurfaceHeight;
			}
		} else {
			if (Structures.getSurfaceHeight().get(rightMostTile + 1) != null) {
				startingHeight = Structures.getSurfaceHeight().get(rightMostTile + 1);
			} else {
				startingHeight = defaultSurfaceHeight;
			}
		}

		//fill surfaceHeight
		if (generatingToRight) {
			for (int x = leftMostTile; x <= rightMostTile; x++) {
				Structures.getSurfaceHeight().put(
					x,
					(int)(startingHeight + desertMaxSurfaceHeightVariation * perlinSurfaceGenerator.generate(x, 1) - desertMaxSurfaceHeightVariation * perlinSurfaceGenerator.generate(leftMostTile, 1))
				);
			}
		} else {
			for (int x = rightMostTile; x >= leftMostTile; x--) {
				Structures.getSurfaceHeight().put(
					x,
					(int)(startingHeight + desertMaxSurfaceHeightVariation * perlinSurfaceGenerator.generate(x, 1) - desertMaxSurfaceHeightVariation * perlinSurfaceGenerator.generate(rightMostTile, 1))
				);
			}
		}
	}


	/** Generate transition layer */
	private void generateTransitionBase(boolean generatingToRight, int rightMostTile, int leftMostTile) {
		HashMap<Integer, Integer> left = new HashMap<>();
		HashMap<Integer, Integer> right = new HashMap<>();
		SawToothGenerator transitionBaseGenerator = new SawToothGenerator(desertMaxTransitionDepth, desertMaxTransitionDepth + 20, 3, 2, 150);
		if (generatingToRight) {
			right.put(leftMostTile, Structures.getSurfaceHeight().get(leftMostTile));
			for (int x = leftMostTile + 1; x <= rightMostTile; x++) {
				transitionBaseGenerator.generateSurfaceHeight(x, generatingToRight, right);
			}
			left.put(rightMostTile, Structures.getSurfaceHeight().get(rightMostTile));
			for (int x = rightMostTile - 1; x >= leftMostTile; x--) {
				transitionBaseGenerator.generateSurfaceHeight(x, !generatingToRight, left);
			}
		} else {
			left.put(rightMostTile, Structures.getSurfaceHeight().get(rightMostTile));
			for (int x = rightMostTile - 1; x >= leftMostTile; x--) {
				transitionBaseGenerator.generateSurfaceHeight(x, generatingToRight, left);
			}
			right.put(leftMostTile, Structures.getSurfaceHeight().get(leftMostTile));
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
		HashMap<Integer, Integer> left = new HashMap<>();
		HashMap<Integer, Integer> right = new HashMap<>();
		SawToothGenerator sandBaseGenerator = new SawToothGenerator(desertMaxSandstoneDepth, desertMaxSandstoneDepth + 20, 2, 1, 100);
		if (generatingToRight) {
			right.put(leftMostTile + desertTransitionWidth, Structures.getSurfaceHeight().get(leftMostTile + desertTransitionWidth));
			for (int x = leftMostTile + desertTransitionWidth+ 1; x <= rightMostTile - desertTransitionWidth; x++) {
				sandBaseGenerator.generateSurfaceHeight(x, generatingToRight, right);
			}
			left.put(rightMostTile - desertTransitionWidth, Structures.getSurfaceHeight().get(rightMostTile - desertTransitionWidth));
			for (int x = rightMostTile - desertTransitionWidth - 1; x >= leftMostTile + desertTransitionWidth; x--) {
				sandBaseGenerator.generateSurfaceHeight(x, !generatingToRight, left);
			}
		} else {
			left.put(rightMostTile - desertTransitionWidth, Structures.getSurfaceHeight().get(rightMostTile - desertTransitionWidth));
			for (int x = rightMostTile - desertTransitionWidth - 1; x >= leftMostTile + desertTransitionWidth; x--) {
				sandBaseGenerator.generateSurfaceHeight(x, generatingToRight, left);
			}
			right.put(leftMostTile + desertTransitionWidth, Structures.getSurfaceHeight().get(leftMostTile + desertTransitionWidth));
			for (int x = leftMostTile + desertTransitionWidth+ 1; x <= rightMostTile - desertTransitionWidth; x++) {
				sandBaseGenerator.generateSurfaceHeight(x, !generatingToRight, right);
			}
		}
		for (int x = leftMostTile + desertTransitionWidth; x <= rightMostTile - desertTransitionWidth; x++) {
			sandBase.put(x, Math.max(right.get(x), left.get(x)));
		}
	}


	@Override
	protected Tile internalGetForegroundTile(int worldTileX, int worldTileY) {

		if (worldTileY > Structures.getSurfaceHeight().get(worldTileX)) {
			return new Tile.EmptyTile();
		} else if (worldTileY > transitionBase.get(worldTileX)) {
			if (sandBase.get(worldTileX) == null) {
				return new DryDirtTile();
			}
			if (worldTileY > sandBase.get(worldTileX)) {
				return new YellowSandTile();
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
		if (worldTileY > Structures.getSurfaceHeight().get(worldTileX)-1) {
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