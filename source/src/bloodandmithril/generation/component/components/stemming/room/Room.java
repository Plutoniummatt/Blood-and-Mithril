package bloodandmithril.generation.component.components.stemming.room;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;
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

	/**
	 * Constructor
	 */
	Room(final Boundaries boundaries, final int structureKey, final int wallThickness, final Class<? extends Tile> wallTile) {
		super(boundaries, structureKey);
		this.wallThickness = wallThickness;
		this.wallTile = wallTile;
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
				return wallTile.newInstance();
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
				return wallTile.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return null;
	}


	@Override
	public void generateInterfaces() {
	}
}