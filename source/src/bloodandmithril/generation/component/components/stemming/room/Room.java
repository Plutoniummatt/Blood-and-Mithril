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


	/**
	 * Constructor
	 */
	Room(final Boundaries boundaries, final int structureKey) {
		super(boundaries, structureKey);
	}


	@Override
	public Tile getForegroundTile(final int worldTileX, final int worldTileY) {
		return null;
	}


	@Override
	public Tile getBackgroundTile(final int worldTileX, final int worldTileY) {
		return null;
	}


	@Override
	public void generateInterfaces() {
	}
}