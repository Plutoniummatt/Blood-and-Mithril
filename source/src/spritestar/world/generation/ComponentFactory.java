package spritestar.world.generation;

import java.util.List;

import spritestar.util.datastructure.Boundaries;
import spritestar.util.datastructure.Line;
import spritestar.world.generation.substructures.components.Connection;
import spritestar.world.generation.substructures.components.Pyramid;
import spritestar.world.generation.substructures.components.RectangularRoom;
import spritestar.world.generation.substructures.components.connections.Stairs;
import spritestar.world.topography.tile.tiles.brick.YellowBrickPlatform;
import spritestar.world.topography.tile.tiles.brick.YellowBrickTile;


/**
 * A factory that creates components
 *
 * @author Matt
 */
public class ComponentFactory {

	/**
	 * Creates a {@link RectangularRoom} with the {@link Boundaries} given.
	 */
	public static RectangularRoom createRoom(List<Connection> connectionsToGenerateFrom, Boundaries boundaries) {
		RectangularRoom rectangularRoom = new RectangularRoom(
			boundaries,
			3,
			1,
			0,
			0,
			YellowBrickTile.class
		);
		rectangularRoom.generate(connectionsToGenerateFrom);
		return rectangularRoom;
	}


	/**
	 * Creates a {@link Pyramid} with the {@link Boundaries} given.
	 */
	public static Pyramid createPyramid(List<Connection> connectionsToGenerateFrom, Boundaries boundaries) {
		Pyramid pyramid = new Pyramid(
			boundaries,
			YellowBrickTile.class
		);
		pyramid.generate(connectionsToGenerateFrom);
		return pyramid;
	}


	public static Stairs createStairs(List<Connection> connectionsToGenerateFrom, Line connectionLine, Line floorLine, boolean upRight, boolean generateConnection) {
		Stairs stairs = new Stairs(
			connectionsToGenerateFrom,
			new Boundaries(
				(int)connectionLine.y1,
				(int)connectionLine.y1,
				(int)connectionLine.x1,
				(int)connectionLine.x2
				),
				connectionLine,
				floorLine,
				upRight,
				generateConnection,
				YellowBrickTile.class,
				YellowBrickPlatform.class,
				10,
				3
			);
		stairs.generate(connectionsToGenerateFrom);
		return stairs;
	}
}