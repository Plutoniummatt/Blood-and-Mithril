package bloodandmithril.generation.component.components.stemming.room;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.components.stemming.interfaces.HorizontalInterface;
import bloodandmithril.generation.component.components.stemming.interfaces.StemmingDirection;
import bloodandmithril.generation.component.components.stemming.interfaces.VerticalInterface;
import bloodandmithril.util.Operator;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A simple, four-sided room with adjustable width, height and wall thickness
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class Room extends Component {
	private static final long serialVersionUID = 1488252657521341625L;
	private final int wallThickness;
	private final Class<? extends Tile> wallTile;
	
	private Operator<Tile> fTileManipulator = tile -> {};
	private Operator<Tile> bTileManipulator = tile -> {};

	/**
	 * Constructor
	 */
	Room(
		final Boundaries boundaries, 
		final int structureKey, 
		final int wallThickness, 
		final Class<? extends Tile> wallTile, 
		Operator<Tile> fTileManipulator, 
		Operator<Tile> bTileManipulator
	) {
		super(boundaries, structureKey);
		this.wallThickness = wallThickness;
		this.wallTile = wallTile;
		this.fTileManipulator = fTileManipulator;
		this.bTileManipulator = bTileManipulator;
	}


	@Override
	public Tile getForegroundTile(final int worldTileX, final int worldTileY) {
		if (boundaries.isWithin(worldTileX, worldTileY)) {
			if (new Boundaries(
				boundaries.top - wallThickness, 
				boundaries.bottom + wallThickness, 
				boundaries.left + wallThickness, 
				boundaries.right - wallThickness
			).isWithin(worldTileX, worldTileY)) {
				return new Tile.EmptyTile();
			}
			
			try {
				Tile fTile = wallTile.newInstance();
				fTileManipulator.operate(fTile);
				return fTile;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return null;
	}


	@Override
	public Tile getBackgroundTile(final int worldTileX, final int worldTileY) {
		if (boundaries.isWithin(worldTileX, worldTileY)) {
			try {
				Tile bTile = wallTile.newInstance();
				bTileManipulator.operate(bTile);
				return bTile;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return null;
	}


	@Override
	public void generateInterfaces() {
		// Top
		addInterface(new HorizontalInterface(
			boundaries.left + wallThickness, 
			boundaries.top - wallThickness, 
			boundaries.right - boundaries.left - 2 * wallThickness + 1,
			StemmingDirection.UP
		));
		
		// Bottom
		addInterface(new HorizontalInterface(
			boundaries.left + wallThickness, 
			boundaries.bottom + wallThickness, 
			boundaries.right - boundaries.left - 2 * wallThickness + 1,
			StemmingDirection.DOWN
		));
		
		// Right
		addInterface(new VerticalInterface(
			boundaries.right - wallThickness, 
			boundaries.bottom + wallThickness, 
			boundaries.top - boundaries.bottom - 2 * wallThickness + 1,
			StemmingDirection.RIGHT
		));
		
		// Left
		addInterface(new VerticalInterface(
			boundaries.left + wallThickness, 
			boundaries.bottom + wallThickness, 
			boundaries.top - boundaries.bottom - 2 * wallThickness + 1,
			StemmingDirection.LEFT
		));
	}
}