package spritestar.world.generation.substructures.components;

import java.util.List;

import spritestar.util.datastructure.Boundaries;
import spritestar.util.datastructure.Line;
import spritestar.world.generation.Component;
import spritestar.world.topography.tile.Tile;

/**
 * A rectangular room
 *
 * @author Matt
 */
public class RectangularRoom extends Component {
	private static final long serialVersionUID = -2637020937363360512L;

	/** Wall thickness */
	private final int topWallThickness, bottomWallThickness, leftWallThickness, rightWallThickness;

	/** Tile type of the room */
	private Class<? extends Tile> mainTile;

	/**
	 * Constructor
	 */
	public RectangularRoom(
			Boundaries boundaries,
			int topWallThickness,
			int bottomWallThickness,
			int leftWallThickness,
			int rightWallThickness,
			Class<? extends Tile> mainTile) {
		super(boundaries);
		this.topWallThickness = topWallThickness;
		this.bottomWallThickness = bottomWallThickness;
		this.leftWallThickness = leftWallThickness;
		this.rightWallThickness = rightWallThickness;
		this.mainTile = mainTile;
	}


	@Override
	public Tile getForegroundTileFromComponent(int worldTileX, int worldTileY) {
		if (worldTileY > boundaries.top - topWallThickness || worldTileY < boundaries.bottom + bottomWallThickness || worldTileX < boundaries.left + leftWallThickness || worldTileX > boundaries.right - rightWallThickness) {
			try {
				return mainTile.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Can't make this instance for some reason.");
			}
		} else {
			return new Tile.EmptyTile();
		}
	}


	@Override
	public Tile getBackgroundTileFromComponent(int worldTileX, int worldTileY) {
		try {
			return mainTile.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Can't make this instance for some reason.");
		}
	}


	@Override
	public void addConnection(Connection connection) {
		addComponent(connection); //TODO layer this properly
	}


	@Override
	protected void generateComponent(List<Connection> connectionsToGenerateFrom) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Line getFloorLine(Line connectionLine) {
		return new Line(
			boundaries.left + leftWallThickness,
			boundaries.bottom + bottomWallThickness,
			boundaries.right - rightWallThickness,
			boundaries.bottom + bottomWallThickness
		);
	}
}