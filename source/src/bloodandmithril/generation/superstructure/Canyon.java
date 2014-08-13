package bloodandmithril.generation.superstructure;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.generation.TerrainGenerator;
import bloodandmithril.generation.patterns.Layers;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.generation.tools.SawToothGenerator;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A single, very large canyon.
 *
 * @author Sam
 */
public class Canyon extends SuperStructure {
	private static final long serialVersionUID = 4571957719346940393L;

	private int cWidth;
	private int cHeight;
	private int surfaceLineVariation;
	private int maxSurfaceLineHeightDifference;
	private int maxCanyonDepth;
	private int minCanyonDepth;
	private int baseLineVariation;
	private int numberOfCliffSteps;
	private int cliffStepWidth;

	private int middleHeight;
	private HashMap<Integer, Integer> leftCliffLine = new HashMap<>();
	private HashMap<Integer, Integer> rightCliffLine = new HashMap<>();
	private int leftCliffStart;
	private int rightCliffStart;


	/**
	 * @param worldId - the ID of the world.
	 * @param cWidth - Width of the structure in chunks.
	 * @param cHeight - Height of the structure in chunks.
	 * @param surfaceLineVariation - The variation of the lines at the top of the cliffs.
	 * @param baseLineVariation - The variation of the line at the base of the cliffs.
	 * @param maxSurfaceLineHeightDifference - The maximum difference in height off opposite cliffs.
	 * @param minCanyonDepth - The minimum depth of the canyon.
	 * @param maxCanyonDepth - The maximum depth of the canyon.
	 * @param numberOfCliffSteps - The number of times the cliff face steps out.
	 * @param cliffStepWidth - The width of a step.
	 */
	public Canyon(int worldId, int cWidth, int cHeight, int surfaceLineVariation, int baseLineVariation, int maxSurfaceLineHeightDifference, int minCanyonDepth, int maxCanyonDepth, int numberOfCliffSteps, int cliffStepWidth) {
		super(worldId);
		this.cWidth = cWidth;
		this.cHeight = cHeight;
		this.surfaceLineVariation = surfaceLineVariation;
		this.baseLineVariation = baseLineVariation;
		this.maxSurfaceLineHeightDifference = maxSurfaceLineHeightDifference;
		this.minCanyonDepth = minCanyonDepth;
		this.maxCanyonDepth = maxCanyonDepth;
		this.numberOfCliffSteps = numberOfCliffSteps;
		this.cliffStepWidth = cliffStepWidth;
	}


	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		return RectangularSpaceCalculator.calculateBoundariesConfineWithinTwoHeights(true, startingChunkX, startingChunkY, cWidth, cHeight, TerrainGenerator.maxSurfaceHeightInChunks, TerrainGenerator.maxSurfaceHeightInChunks - cHeight, Domain.getWorld(worldId).getTopography());
	}


	@Override
	protected void internalGenerate(boolean generatingToRight) {
		final int dafaultSurfaceHeight = 500;
		ConcurrentHashMap<Integer, Integer> surfaceHeightMap = Domain.getWorld(worldId).getTopography().getStructures().getSurfaceHeight();

		int rightMostTile = (getBoundaries().right + 1) * Topography.CHUNK_SIZE - 1;
		int leftMostTile = getBoundaries().left * Topography.CHUNK_SIZE;
		int width = rightMostTile - leftMostTile;
		int startingHeight;
		leftCliffStart = leftMostTile + width/8;
		rightCliffStart = rightMostTile - width/8;
		int otherHeight;
		SawToothGenerator leftGround;
		SawToothGenerator leftCliff;
		SawToothGenerator rightGround;
		SawToothGenerator rightCliff;
		SawToothGenerator middleGround;

		// create the saw tooth generators
		if (generatingToRight) {
			if (surfaceHeightMap.get(leftMostTile - 1) != null) {
				startingHeight = surfaceHeightMap.get(leftMostTile - 1);
			} else {
				startingHeight = dafaultSurfaceHeight;
			}
			otherHeight = startingHeight - maxSurfaceLineHeightDifference/2 + Util.getRandom().nextInt(maxSurfaceLineHeightDifference);
			leftGround = new SawToothGenerator(startingHeight - surfaceLineVariation/2, startingHeight + surfaceLineVariation/2, 2, 1, 50);
			rightGround = new SawToothGenerator(otherHeight - surfaceLineVariation/2, otherHeight + surfaceLineVariation/2, 2, 1, 50);
		} else {
			if (surfaceHeightMap.get(rightMostTile + 1) != null) {
				startingHeight = surfaceHeightMap.get(rightMostTile + 1);
			} else {
				startingHeight = dafaultSurfaceHeight;
			}
			otherHeight = startingHeight - maxSurfaceLineHeightDifference/2 + Util.getRandom().nextInt(maxSurfaceLineHeightDifference);
			rightGround = new SawToothGenerator(startingHeight - surfaceLineVariation/2, startingHeight + surfaceLineVariation/2, 2, 1, 50);
			leftGround = new SawToothGenerator(otherHeight - surfaceLineVariation/2, otherHeight + surfaceLineVariation/2, 2, 1, 50);
		}
		middleHeight = Math.min(startingHeight, otherHeight) - (minCanyonDepth + Util.getRandom().nextInt(maxCanyonDepth - minCanyonDepth));
		middleGround = new SawToothGenerator(middleHeight - baseLineVariation, middleHeight + baseLineVariation, 2, 1, 50);

		leftCliff = new SawToothGenerator(leftCliffStart, leftCliffStart+ cliffStepWidth, 1, 0, 100);
		rightCliff = new SawToothGenerator(rightCliffStart - cliffStepWidth, rightCliffStart, 1, 0, 100);
		generateSurfaceHeight(generatingToRight, rightMostTile, leftMostTile, otherHeight, leftGround, rightGround, middleGround);
		generateCliffFaces(generatingToRight, startingHeight, otherHeight, leftCliff, rightCliff);
	}


	private void generateSurfaceHeight(boolean generatingToRight, int rightMostTile, int leftMostTile, int otherHeight, SawToothGenerator leftGround, SawToothGenerator rightGround, SawToothGenerator middleGround) {
		ConcurrentHashMap<Integer, Integer> surfaceHeightMap = Domain.getWorld(worldId).getTopography().getStructures().getSurfaceHeight();
		if (generatingToRight) {
			for (int x = leftMostTile; x <= rightMostTile;  x++) {
				if (x < leftCliffStart) {
					leftGround.generateSurfaceHeight(x, generatingToRight, surfaceHeightMap);
				} else if (x > rightCliffStart + 1) {
					rightGround.generateSurfaceHeight(x, generatingToRight, surfaceHeightMap);
				} else if (x == leftCliffStart) {
					surfaceHeightMap.put(x, middleHeight);
				} else if (x == rightCliffStart + 1) {
					surfaceHeightMap.put(x, otherHeight);
				} else {
					middleGround.generateSurfaceHeight(x, generatingToRight, surfaceHeightMap);
				}
			}
		} else {
			for (int x = rightMostTile; x >= leftMostTile; x--) {
				if (x < leftCliffStart - 1) {
					leftGround.generateSurfaceHeight(x, generatingToRight, surfaceHeightMap);
				} else if (x > rightCliffStart) {
					rightGround.generateSurfaceHeight(x, generatingToRight, surfaceHeightMap);
				} else if (x == leftCliffStart - 1) {
					surfaceHeightMap.put(x, otherHeight);
				} else if (x == rightCliffStart) {
					surfaceHeightMap.put(x, middleHeight);
				} else {
					middleGround.generateSurfaceHeight(x, generatingToRight, surfaceHeightMap);
				}
			}
		}
	}


	private void generateCliffFaces(boolean generatingToRight, int startingHeight, int otherHeight, SawToothGenerator leftCliff, SawToothGenerator rightCliff) {
		ConcurrentHashMap<Integer, Integer> surfaceHeightMap = Domain.getWorld(worldId).getTopography().getStructures().getSurfaceHeight();
		int leftStepDifference = ((generatingToRight ? startingHeight : otherHeight) - middleHeight)/numberOfCliffSteps;
		int leftCliffStepPoint = leftStepDifference * (numberOfCliffSteps - 1) + middleHeight;
		int leftCliffBoundary = leftCliffStart;
		int rightCliffBoundary = rightCliffStart;
		for (int y = surfaceHeightMap.get(leftCliffStart - 1); y > middleHeight - baseLineVariation / 2; y--) {
			if (y == surfaceHeightMap.get(leftCliffStart - 1)) {
				leftCliffLine.put(y, leftCliffStart - 1);
			} else {
				if (y == leftCliffStepPoint) {
					leftCliffBoundary += cliffStepWidth;
					leftCliff.setMinSurface(leftCliffBoundary);
					leftCliff.setMaxSurface(leftCliffBoundary + cliffStepWidth);
					leftCliffStepPoint -= leftStepDifference;
				}
				leftCliff.generateSurfaceHeight(y, false, leftCliffLine);
			}
		}
		int rightStepDifference = ((generatingToRight ? otherHeight : startingHeight) - middleHeight) / numberOfCliffSteps;
		int rightCliffStepPoint = rightStepDifference * (numberOfCliffSteps - 1) + middleHeight;
		for (int y = surfaceHeightMap.get(rightCliffStart + 1); y > middleHeight - baseLineVariation / 2; y--) {
			if (y == surfaceHeightMap.get(rightCliffStart + 1)) {
				rightCliffLine.put(y, rightCliffStart + 1);
			} else {
				if (y == rightCliffStepPoint) {
					rightCliffBoundary -= cliffStepWidth;
					rightCliff.setMinSurface(rightCliffBoundary - cliffStepWidth);
					rightCliff.setMaxSurface(rightCliffBoundary);
					rightCliffStepPoint -= rightStepDifference;
				}
				rightCliff.generateSurfaceHeight(y, false, rightCliffLine);
			}
		}
	}


	@Override
	protected Tile internalGetForegroundTile(int worldTileX, int worldTileY) {
		ConcurrentHashMap<Integer, Integer> surfaceHeightMap = Domain.getWorld(worldId).getTopography().getStructures().getSurfaceHeight();
		if (worldTileX < leftCliffStart) {
			if (worldTileY < surfaceHeightMap.get(worldTileX)) {
				return Layers.getTile(worldTileX, worldTileY);
			} else {
				return new Tile.EmptyTile();
			}
		} else if (worldTileX > rightCliffStart) {
			if (worldTileY < surfaceHeightMap.get(worldTileX)) {
				return Layers.getTile(worldTileX, worldTileY);
			} else {
				return new Tile.EmptyTile();
			}
		} else {
			if (leftCliffLine.get(worldTileY) != null && worldTileX < leftCliffLine.get(worldTileY) || rightCliffLine.get(worldTileY) != null && worldTileX > rightCliffLine.get(worldTileY) || worldTileY < surfaceHeightMap.get(worldTileX)) {
				return Layers.getTile(worldTileX, worldTileY);
			} else {
				return new Tile.EmptyTile();
			}
		}
	}


	@Override
	protected Tile internalGetBackgroundTile(int worldTileX, int worldTileY) {
		ConcurrentHashMap<Integer, Integer> surfaceHeightMap = Domain.getWorld(worldId).getTopography().getStructures().getSurfaceHeight();

		if (worldTileX < leftCliffStart) {
			if (worldTileY < surfaceHeightMap.get(worldTileX) - 1 && leftCliffLine.get(worldTileY) != null && worldTileX < leftCliffLine.get(worldTileY) - 1) {
				return Layers.getTile(worldTileX, worldTileY);
			} else {
				return new Tile.EmptyTile();
			}
		} else if (worldTileX > rightCliffStart) {
			if (worldTileY < surfaceHeightMap.get(worldTileX) - 1 && rightCliffLine.get(worldTileY) != null && worldTileX > rightCliffLine.get(worldTileY) + 1) {
				return Layers.getTile(worldTileX, worldTileY);
			} else {
				return new Tile.EmptyTile();
			}
		} else {
			if (leftCliffLine.get(worldTileY) != null && worldTileX < leftCliffLine.get(worldTileY) - 1 || rightCliffLine.get(worldTileY) != null && worldTileX > rightCliffLine.get(worldTileY) + 1 || worldTileY < surfaceHeightMap.get(worldTileX) - 1) {
				return Layers.getTile(worldTileX, worldTileY);
			} else {
				return new Tile.EmptyTile();
			}
		}
	}
}
