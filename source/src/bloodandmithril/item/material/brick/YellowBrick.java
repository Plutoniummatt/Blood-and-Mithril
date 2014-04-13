package bloodandmithril.item.material.brick;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;

/**
 * {@link Item} representing {@link YellowBrickTile}
 *
 * @author Matt
 */
public class YellowBrick extends Item {
	private static final long serialVersionUID = -7756119539482746265L;

	/**
	 * Constructor
	 */
	public YellowBrick() {
		super(10f, false, ItemValues.YELLOWBRICK);
	}


	@Override
	public String getSingular(boolean firstCap) {
		if (firstCap) {
			return "Yellow bricks";
		}
		return "yellow bricks";
	}

	@Override
	public String getPlural(boolean firstCap) {
		return getSingular(firstCap);
	}


	@Override
	public String getDescription() {
		return "Yellow colored bricks";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof YellowBrick;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return this;
	}


	@Override
	public void render() {

	}
}