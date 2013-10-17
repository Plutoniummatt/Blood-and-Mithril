package spritestar.world.generation.substructures;

import java.util.ArrayList;
import java.util.List;

import spritestar.util.datastructure.Boundaries;
import spritestar.util.datastructure.Line;
import spritestar.world.generation.ComponentFactory;
import spritestar.world.generation.StructureMap;
import spritestar.world.generation.SubStructure;
import spritestar.world.generation.substructures.components.Connection;
import spritestar.world.generation.substructures.components.Pyramid;
import spritestar.world.generation.substructures.components.RectangularRoom;
import spritestar.world.generation.substructures.components.connections.Stairs;
import spritestar.world.generation.superstructures.Desert;
import spritestar.world.topography.Topography;

/**
 * Desert temple {@link SubStructure} - Found under {@link Desert}s
 *
 * @author Matt
 */
public class DesertTemple extends SubStructure {
	private static final long serialVersionUID = -357956334002458979L;
	
	/** Dimensions of the {@link Pyramid} entrance, see {@link #constructPyramidEntrance(List, int, int)} */
	private final int pyramidWidth = 200;
	private final int pyramidHeight = 100;

	/**
	 * Constructor
	 */
	public DesertTemple() {
	}


	@Override
	protected void generateStructure(boolean generatingToRight) {
		List<Connection> connectionsToGenerateFrom = new ArrayList<Connection>();

		// Use the superStructures surface to find a place for the entrance
		int centerWorldTileX = superStructureBoundaries.left * Topography.chunkSize + (superStructureBoundaries.right - superStructureBoundaries.left) * Topography.chunkSize / 2;
		int centerWorldTileY = StructureMap.surfaceHeight.get(centerWorldTileX);

		// Construct the pyramid entrance
		constructPyramidEntrance(connectionsToGenerateFrom, centerWorldTileX, centerWorldTileY);

		// Stem other components from that entrance TODO - This looks like it should be in framework
	}


	/**
	 * Constructs the entrance {@link Pyramid} of this {@link DesertTemple}
	 */
	private void constructPyramidEntrance(List<Connection> connectionsToGenerateFrom, int centerWorldTileX, int centerWorldTileY) {
		Pyramid pyramid = ComponentFactory.createPyramid(
			connectionsToGenerateFrom,
			new Boundaries(
				centerWorldTileY + pyramidHeight / 2,
				centerWorldTileY - pyramidHeight / 2,
				centerWorldTileX - pyramidWidth/2,
				centerWorldTileX + pyramidWidth/2
			)
		);
		addComponent(pyramid);
		RectangularRoom room = ComponentFactory.createRoom(connectionsToGenerateFrom, pyramid.calculateCorridorBoundaries(10));
		pyramid.addComponent(room);
		Line connectionLine = new Line(pyramid.boundaries.left + 60, pyramid.boundaries.bottom, pyramid.boundaries.left + 70, pyramid.boundaries.bottom);
		Stairs stairs = ComponentFactory.createStairs(connectionsToGenerateFrom, connectionLine, pyramid.getFloorLine(connectionLine), true, true);
		room.boundaries.left = stairs.boundaries.right;
		pyramid.addConnection(stairs);
	}
}