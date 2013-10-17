package spritestar.world.generation.superstructures;

import static spritestar.world.generation.settings.GlobalGenerationSettings.defaultSurfaceHeight;
import static spritestar.world.generation.settings.GlobalGenerationSettings.desertMaxSandstoneDepth;
import static spritestar.world.generation.settings.GlobalGenerationSettings.desertMaxSurfaceHeightVariation;
import static spritestar.world.generation.settings.GlobalGenerationSettings.desertMaxTransitionDepth;
import static spritestar.world.generation.settings.GlobalGenerationSettings.desertMaxWidth;
import static spritestar.world.generation.settings.GlobalGenerationSettings.desertMinWidth;
import static spritestar.world.generation.settings.GlobalGenerationSettings.desertTransitionWidth;
import static spritestar.world.generation.settings.GlobalGenerationSettings.maxSurfaceHeight;

import java.util.HashMap;

import spritestar.persistence.ParameterPersistenceService;
import spritestar.util.datastructure.Boundaries;
import spritestar.util.datastructure.IntIntHashMap;
import spritestar.world.generation.StructureMap;
import spritestar.world.generation.SuperStructure;
import spritestar.world.generation.patterns.Layers;
import spritestar.world.generation.substructures.DesertTemple;
import spritestar.world.generation.tools.PerlinNoiseGenerator1D;
import spritestar.world.generation.tools.RectangularSpaceCalculator;
import spritestar.world.generation.tools.SawToothGenerator;
import spritestar.world.topography.Topography;
import spritestar.world.topography.tile.Tile;
import spritestar.world.topography.tile.tiles.sedimentary.YellowSandTile;
import spritestar.world.topography.tile.tiles.soil.DryDirtTile;
import spritestar.world.topography.tile.tiles.stone.SandStoneTile;

/**
 * The structure of a desert surface to be stored and used to generate when needed
 *
 * @author Sam, Matt
 */
public class Desert extends SuperStructure {
	private static final long serialVersionUID = 4034191268168150728L;

	private final PerlinNoiseGenerator1D perlinSurfaceGenerator = new PerlinNoiseGenerator1D(30, ParameterPersistenceService.getParameters().getSeed());

	private final IntIntHashMap sandBase = new IntIntHashMap();

	private final IntIntHashMap transitionBase = new IntIntHashMap();

	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		//calculates where the structure can go
		return RectangularSpaceCalculator.calculateBoundaries(
			true,
			startingChunkX,
			startingChunkY,
			(desertMaxWidth - desertMinWidth) / 2 + 1,
			maxSurfaceHeight - desertMaxSandstoneDepth / Topography.chunkSize + 1,
			maxSurfaceHeight,
			desertMaxSandstoneDepth / Topography.chunkSize - 1
		);
	}


	@Override
	protected boolean isValid() {
		return !(boundaries.bottom < desertMaxSandstoneDepth/Topography.chunkSize - 1 || boundaries.top > maxSurfaceHeight);
	}


	@Override
	protected void generateStructure(boolean generatingToRight) {
		int rightMostTile = (boundaries.right + 1) * Topography.chunkSize - 1;
		int leftMostTile = boundaries.left * Topography.chunkSize;

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
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
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
				return Layers.getTile(worldTileX, worldTileY);
			}
			if (worldTileY > sandBase.get(worldTileX)) {
				return new SandStoneTile();
			} else {
				return Layers.getTile(worldTileX, worldTileY);
			}
		}
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
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
					return new Tile.EmptyTile();
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


	@Override
	protected void generateSubStructures(boolean generatingToRight) {
		generateAndAddSubStructure(new DesertTemple(), boundaries.left, boundaries.top, generatingToRight);
	}
}