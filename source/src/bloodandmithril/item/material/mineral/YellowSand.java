package bloodandmithril.item.material.mineral;

import bloodandmithril.Fortress;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.world.topography.tile.tiles.sedimentary.YellowSandTile;

import com.badlogic.gdx.graphics.Color;

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
		super(10f, false, 1);
	}


	/**
	 * @see bloodandmithril.item.Item#getSingular(boolean)
	 */
	@Override
	public String getSingular(boolean firstCap) {
		if (firstCap) {
			return "Sand";
		}
		return "sand";
	}


	/**
	 * @see bloodandmithril.item.Item#getPlural(boolean)
	 */
	@Override
	public String getPlural(boolean firstCap) {
		if (firstCap) {
			return "Sand";
		}
		return "sand";
	}


	/**
	 * @see bloodandmithril.item.Item#getInfoWindow()
	 */
	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			"Sand is a naturally occurring granular material composed of finely divided rock and mineral particles.",
			Color.ORANGE,
			Fortress.getMouseScreenX(),
			Fortress.getMouseScreenY(),
			350,
			200,
			"Sand",
			true,
			100,
			100
		);
	}


	/**
	 * @see bloodandmithril.item.Item#sameAs(bloodandmithril.item.Item)
	 */
	@Override
	public boolean sameAs(Item other) {
		return other instanceof YellowSand;
	}
}