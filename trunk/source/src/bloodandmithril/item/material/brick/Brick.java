package bloodandmithril.item.material.brick;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;

/**
 * {@link Item} representing {@link YellowBrickTile}
 *
 * @author Matt
 */
public class Brick extends Item {
	private static final long serialVersionUID = -7756119539482746265L;

	/**
	 * Constructor
	 */
	public Brick() {
		super(10f, false, ItemValues.YELLOWBRICK);
	}


	@Override
	public String getSingular(boolean firstCap) {
		if (firstCap) {
			return "Bricks";
		}
		return "bricks";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return getSingular(firstCap);
	}


	@Override
	public String getDescription() {
		return "Bricks, clay hardened under intense heat";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Brick;
	}


	@Override
	public void render() {
	}
}