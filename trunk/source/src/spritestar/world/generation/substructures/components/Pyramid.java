package spritestar.world.generation.substructures.components;

import java.util.List;

import spritestar.util.datastructure.Boundaries;
import spritestar.util.datastructure.Line;
import spritestar.world.generation.Component;
import spritestar.world.generation.StructureMap;
import spritestar.world.topography.tile.Tile;

/**
 * Makes a pyramid to be used as the entrance for a temple.
 *
 * @author Sam
 */
public class Pyramid extends Component {
	private static final long serialVersionUID = 7824474786206038710L;

	/** Main {@link Tile} of the {@link Pyramid} */
	private Class<? extends Tile> mainTile;

	/** Physical dimensions of this {@link Pyramid} */
	private int leftSlantConstant, rightSlantConstant, bottomOfCorridor, topOfCorridor, leftOfCorridor, rightOfCorridor;

	/**
	 * Constructor
	 */
	public Pyramid(
			Boundaries boundaries,
			Class<? extends Tile> mainTile) {
		super(boundaries);
		this.mainTile = mainTile;
	}


	@Override
	public void generateComponent(List<Connection> connectionsToGenerateFrom) {
		//pyramid shape
		int center = boundaries.left + (boundaries.right - boundaries.left)/2;
		leftSlantConstant = Math.min(boundaries.top - center, boundaries.bottom - boundaries.left);
		rightSlantConstant = Math.min(boundaries.top + center, boundaries.bottom + boundaries.right);
	}


	/** Calculates where the side of the pyramid meets the surface so it can place a corridor there. */
	public Boundaries calculateCorridorBoundaries(int corridorHeight) {

		int y = boundaries.bottom;
		while (StructureMap.surfaceHeight.get(y - leftSlantConstant) > y - 1) {
			if (StructureMap.surfaceHeight.get(y - leftSlantConstant) <= y) {
				bottomOfCorridor = y;
				topOfCorridor = y + corridorHeight;
			}
			y++;
		}

		y = boundaries.bottom;
		while (StructureMap.surfaceHeight.get(rightSlantConstant - y) > y - 1) {
			if (StructureMap.surfaceHeight.get(rightSlantConstant - y) <= y) {
				if(bottomOfCorridor < y) {
					bottomOfCorridor = y;
					topOfCorridor = y + corridorHeight;
				}
			}
			y++;
		}

		rightOfCorridor = rightSlantConstant - bottomOfCorridor - 1;
		leftOfCorridor = bottomOfCorridor - leftSlantConstant + 1;

		return new Boundaries(topOfCorridor, bottomOfCorridor, leftOfCorridor, rightOfCorridor);
	}


	@Override
	public Tile getForegroundTileFromComponent(int worldTileX, int worldTileY) {
		if (worldTileY - worldTileX < leftSlantConstant && worldTileY + worldTileX < rightSlantConstant) {
			try {
				return mainTile.newInstance();
			} catch (Exception e) {
				throw new RuntimeException();
			}
		} else {
			return null;
		}
	}


	@Override
	public Tile getBackgroundTileFromComponent(int worldTileX, int worldTileY) {
		if (worldTileY - worldTileX < leftSlantConstant && worldTileY + worldTileX < rightSlantConstant) {
			try {
				return mainTile.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}


	@Override
	public void addConnection(Connection connection) {
		addComponent(connection); //TODO layer this properly
	}


	@Override
	public Line getFloorLine(Line connectionLine) {
		for(Component component : components) {
			return component.getFloorLine(connectionLine);
		}
		return null;
	}
}