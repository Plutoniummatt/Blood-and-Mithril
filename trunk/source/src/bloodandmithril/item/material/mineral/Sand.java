package bloodandmithril.item.material.mineral;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.world.topography.tile.tiles.sedimentary.SandTile;

/**
 * Sand, obtained as a result of mining {@link SandTile}
 *
 * @author Matt
 */
public class Sand extends Item {
	private static final long serialVersionUID = -7756119539773387265L;


	/**
	 * Constructor
	 */
	public Sand() {
		super(10f, false, ItemValues.YELLOWSAND);
	}


	@Override
	public String getSingular(boolean firstCap) {
		if (firstCap) {
			return "Sand";
		}
		return "sand";
	}


	@Override
	public String getPlural(boolean firstCap) {
		if (firstCap) {
			return "Sand";
		}
		return "sand";
	}


	@Override
	public String getDescription() {
		return "Sand is a naturally occurring granular material composed of finely divided rock and mineral particles.";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Sand;
	}


	@Override
	public void render() {
	}
}