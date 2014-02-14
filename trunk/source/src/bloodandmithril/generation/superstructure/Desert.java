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

import bloodandmithril.generation.StructureMap;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.Corridor;
import bloodandmithril.generation.component.Corridor.CorridorCreationCustomization;
import bloodandmithril.generation.component.Room;
import bloodandmithril.generation.component.Room.RoomCreationCustomization;
import bloodandmithril.generation.component.Stairs;
import bloodandmithril.generation.component.Stairs.StairsCreationCustomization;
import bloodandmithril.generation.patterns.Layers;
import bloodandmithril.generation.patterns.UndergroundWithCaves;
import bloodandmithril.generation.tools.PerlinNoiseGenerator1D;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.generation.tools.SawToothGenerator;
import bloodandmithril.persistence.ParameterPersistenceService;
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

	private static boolean oododo = true; //TODO WTF IS THIS SHIT MANNNNG
	@Override
	protected void generateStructure(boolean generatingToRight) {
		int rightMostTile = (boundaries.right + 1) * Topography.CHUNK_SIZE - 1;
		int leftMostTile = boundaries.left * Topography.CHUNK_SIZE;

		if (oododo) {
			components.add(new Room(new Boundaries(4, -24, -4, 24), new Boundaries(-1, -19, 1, 19), structureKey));

			components.add(
				components.get(0).stem(
					Stairs.class,
					new StairsCreationCustomization(
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextInt(30) + 20,
						1,
						7,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					)
				)
			);

			Component stem = null;
			while (stem == null) {
				stem = components.get(1).stem(
					Stairs.class,
					new StairsCreationCustomization(
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextInt(30) + 20,
						1,
						7,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					)
				);
			}
			components.add(stem);

			Component stem2 = null;
			while (stem2 == null) {
				stem2 = components.get(2).stem(
					Stairs.class,
					new StairsCreationCustomization(
						Util.getRandom().nextBoolean(),
						Util.getRandom().nextBoolean(),
						true,
						Util.getRandom().nextInt(30) + 20,
						1,
						7,
						3,
						YellowBrickTile.class,
						YellowBrickTile.class
					)
				);
			}
			components.add(stem2);

			Component stem3 = null;
			while (stem3 == null) {
				stem3 = components.get(3).stem(
					Corridor.class,
					new CorridorCreationCustomization(
						Util.getRandom().nextBoolean(),
						3,
						3,
						20 + Util.getRandom().nextInt(10),
						7,
						YellowBrickTile.class
					)
				);
			}
			components.add(stem3);

			Component stem4 = null;
			while (stem4 == null) {
				stem4 = components.get(4).stem(
					Room.class,
					new RoomCreationCustomization(
						Util.getRandom().nextBoolean(),
						20 + Util.getRandom().nextInt(10),
						20 + Util.getRandom().nextInt(10),
						3,
						YellowBrickTile.class
					)
				);
			}
			components.add(stem4);

			for (int i = 0; i < 10; i++) {
				Component stem5 = null;
				int attempts = 0;
				while (stem5 == null && attempts < 10) {
					stem5 = components.get(5 + i).stem(
						Stairs.class,
						new StairsCreationCustomization(
							Util.getRandom().nextBoolean(),
							Util.getRandom().nextBoolean(),
							Util.getRandom().nextBoolean(),
							Util.getRandom().nextInt(10) + 10,
							1,
							7,
							3,
							YellowBrickTile.class,
							YellowBrickTile.class
						)
					);
					attempts++;
				}
				if (stem5 != null) {
					components.add(stem5);
				} else {
					break;
				}
			}
			oododo = false;
		}

		generateSurface(generatingToRight, rightMostTile, leftMostTile);

		generateTransitionBase(generatingToRight, rightMostTile, leftMostTile);

		generateSandBase(generatingToRight, rightMostTile, leftMostTile);
	}


	/** Generates the surface layer */
	private void generateSurface(boolean generatingToRight, int rightMostTile, int leftMostTile) {
		int startingHeight;

		// set starting height
		if (generatingToRight) {
			if (StructureMap.surfaceHeight.get(leftMostTile - 1) != null) {
				startingHeight = StructureMap.surfaceHeight.get(leftMostTile - 1);
			} else {
				startingHeight = defaultSurfaceHeight;
			}
		} else {
			if (StructureMap.surfaceHeight.get(rightMostTile + 1) != null) {
				startingHeight = StructureMap.surfaceHeight.get(rightMostTile + 1);
			} else {
				startingHeight = defaultSurfaceHeight;
			}
		}

		//fill surfaceHeight
		if (generatingToRight) {
			for (int x = leftMostTile; x <= rightMostTile; x++) {
				StructureMap.surfaceHeight.put(x, (int)(startingHeight + desertMaxSurfaceHeightVariation * perlinSurfaceGenerator.generate(x, 1) - desertMaxSurfaceHeightVariation * perlinSurfaceGenerator.generate(leftMostTile, 1)));
			}
		} else {
			for (int x = rightMostTile; x >= leftMostTile; x--) {
				StructureMap.surfaceHeight.put(x, (int)(startingHeight + desertMaxSurfaceHeightVariation * perlinSurfaceGenerator.generate(x, 1) - desertMaxSurfaceHeightVariation * perlinSurfaceGenerator.generate(rightMostTile, 1)));
			}
		}
	}


	/** Generate transition layer */
	private void generateTransitionBase(boolean generatingToRight, int rightMostTile, int leftMostTile) {
		HashMap<Integer, Integer> left = new HashMap<>();
		HashMap<Integer, Integer> right = new HashMap<>();
		SawToothGenerator transitionBaseGenerator = new SawToothGenerator(desertMaxTransitionDepth, desertMaxTransitionDepth + 20, 3, 2, 150);
		if (generatingToRight) {
			right.put(leftMostTile, StructureMap.surfaceHeight.get(leftMostTile));
			for (int x = leftMostTile + 1; x <= rightMostTile; x++) {
				transitionBaseGenerator.generateSurfaceHeight(x, generatingToRight, right);
			}
			left.put(rightMostTile, StructureMap.surfaceHeight.get(rightMostTile));
			for (int x = rightMostTile - 1; x >= leftMostTile; x--) {
				transitionBaseGenerator.generateSurfaceHeight(x, !generatingToRight, left);
			}
		} else {
			left.put(rightMostTile, StructureMap.surfaceHeight.get(rightMostTile));
			for (int x = rightMostTile - 1; x >= leftMostTile; x--) {
				transitionBaseGenerator.generateSurfaceHeight(x, generatingToRight, left);
			}
			right.put(leftMostTile, StructureMap.surfaceHeight.get(leftMostTile));
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
			right.put(leftMostTile + desertTransitionWidth, StructureMap.surfaceHeight.get(leftMostTile + desertTransitionWidth));
			for (int x = leftMostTile + desertTransitionWidth+ 1; x <= rightMostTile - desertTransitionWidth; x++) {
				sandBaseGenerator.generateSurfaceHeight(x, generatingToRight, right);
			}
			left.put(rightMostTile - desertTransitionWidth, StructureMap.surfaceHeight.get(rightMostTile - desertTransitionWidth));
			for (int x = rightMostTile - desertTransitionWidth - 1; x >= leftMostTile + desertTransitionWidth; x--) {
				sandBaseGenerator.generateSurfaceHeight(x, !generatingToRight, left);
			}
		} else {
			left.put(rightMostTile - desertTransitionWidth, StructureMap.surfaceHeight.get(rightMostTile - desertTransitionWidth));
			for (int x = rightMostTile - desertTransitionWidth - 1; x >= leftMostTile + desertTransitionWidth; x--) {
				sandBaseGenerator.generateSurfaceHeight(x, generatingToRight, left);
			}
			right.put(leftMostTile + desertTransitionWidth, StructureMap.surfaceHeight.get(leftMostTile + desertTransitionWidth));
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

		if (worldTileY > StructureMap.surfaceHeight.get(worldTileX)) {
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

		for (Component thing : components) {
			if (thing.getBackgroundTile(worldTileX, worldTileY) != null) {
				return thing.getBackgroundTile(worldTileX, worldTileY);
			}
		}

		if (worldTileY > StructureMap.surfaceHeight.get(worldTileX) - 2) {
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