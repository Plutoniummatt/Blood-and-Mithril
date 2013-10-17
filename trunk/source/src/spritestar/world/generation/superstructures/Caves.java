package spritestar.world.generation.superstructures;

import static spritestar.world.generation.settings.GlobalGenerationSettings.cavesBoundaryOffset;
import static spritestar.world.generation.settings.GlobalGenerationSettings.cavesMaxHeight;
import static spritestar.world.generation.settings.GlobalGenerationSettings.cavesMaxWidth;

import java.util.HashMap;

import spritestar.util.datastructure.Boundaries;
import spritestar.world.generation.SuperStructure;
import spritestar.world.generation.patterns.Layers;
import spritestar.world.generation.tools.RectangularSpaceCalculator;
import spritestar.world.generation.tools.SawToothGenerator;
import spritestar.world.topography.Topography;
import spritestar.world.topography.tile.Tile;
import spritestar.world.topography.tile.Tile.EmptyTile;

/**
 * The structure of a cave. TODO - Caves are substructures
 *
 * @author Sam, Matt
 */
public class Caves extends SuperStructure {
	private static final long serialVersionUID = -9034605400597129907L;

	/** The ceiling of the cave, maps x to y for the ceiling surface contour. */
	private final HashMap<Integer, Integer> top = new HashMap<>();

	/** The floor of the cave, maps x to y for the floor surface contour. */
	private final HashMap<Integer, Integer> bottom = new HashMap<>();

	/** The left wall of the cave, maps y to x for the left wall surface contour. */
	private final HashMap<Integer, Integer> left = new HashMap<>();

	/** The right wall of the cave, maps y to x for the right wall surface contour. */
	private final HashMap<Integer, Integer> right = new HashMap<>();


	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		// Find space for the cave
		return RectangularSpaceCalculator.calculateBoundaries(
			true,
			startingChunkX,
			startingChunkY,
			cavesMaxWidth,
			cavesMaxHeight
		);
	}


	@Override
	protected boolean isValid() {
		return true;
	}


	@Override
	protected void generateStructure(boolean generatingToRight) {
		// Make the generators for the boundaries of the cave
		SawToothGenerator topBoundaryGenerator = new SawToothGenerator(boundaries.top * Topography.chunkSize - cavesBoundaryOffset, (boundaries.top + 1) * Topography.chunkSize - 1, 0, 0, 20);
		SawToothGenerator bottomBoundaryGenerator = new SawToothGenerator(boundaries.bottom * Topography.chunkSize, (boundaries.bottom + 1) * Topography.chunkSize - 1 + cavesBoundaryOffset, 0, 0, 20);
		SawToothGenerator leftBoundaryGenerator = new SawToothGenerator(boundaries.left * Topography.chunkSize, (boundaries.left + 1) * Topography.chunkSize - 1 + cavesBoundaryOffset, 0, 0, 20);
		SawToothGenerator rightBoundaryGenerator = new SawToothGenerator(boundaries.right * Topography.chunkSize - cavesBoundaryOffset, (boundaries.right + 1) * Topography.chunkSize - 1, 0, 0, 20);


		// Generate the boundaries
		for (int a = boundaries.bottom * Topography.chunkSize; a < (boundaries.top + 1) * Topography.chunkSize - 1; a++) {
			leftBoundaryGenerator.generateSurfaceHeight(a, true, left);
		}
		for (int a = boundaries.bottom * Topography.chunkSize; a < (boundaries.top + 1) * Topography.chunkSize - 1; a++) {
			rightBoundaryGenerator.generateSurfaceHeight(a, true, right);
		}
		for (int a = boundaries.left * Topography.chunkSize; a < (boundaries.right + 1) * Topography.chunkSize - 1; a++) {
			bottomBoundaryGenerator.generateSurfaceHeight(a, true, bottom);
		}
		for (int a = boundaries.left * Topography.chunkSize; a < (boundaries.right + 1) * Topography.chunkSize - 1; a++) {
			topBoundaryGenerator.generateSurfaceHeight(a, true, top);
		}
	}


	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
		if (top.get(worldTileX) != null && bottom.get(worldTileX) != null && left.get(worldTileY) != null && right.get(worldTileY) != null && worldTileY < top.get(worldTileX) && worldTileY > bottom.get(worldTileX) && worldTileX > left.get(worldTileY) && worldTileX < right.get(worldTileY)) {
			return new EmptyTile();
		} else {
			return Layers.getTile(worldTileX, worldTileY);
		}
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		return Layers.getTile(worldTileX, worldTileY);
	}


	@Override
	protected void generateSubStructures(boolean generatingToRight) {
	}
}
