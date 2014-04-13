package bloodandmithril.item.material.mineral;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.world.topography.tile.tiles.sedimentary.YellowSandTile;

/**
 * Yellow Sand, obtained as a result of mining {@link YellowSandTile}
 *
 * @author Matt
 */
public class YellowSand extends Item {
	private static final long serialVersionUID = -7756119539773387265L;


	/**
	 * Constructor
	 */
	public YellowSand() {
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
		return other instanceof YellowSand;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return this;
	}


	@Override
	public void render() {

	}
}