package bloodandmithril.item.material.brick;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;

import com.badlogic.gdx.graphics.Color;

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
		super(10f, false, 10);
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
	public Window getInfoWindow() {
		return new MessageWindow(
			"Yellow colored bricks",
			Color.ORANGE,
			BloodAndMithrilClient.getMouseScreenX(),
			BloodAndMithrilClient.getMouseScreenY(),
			350,
			200,
			"Yellow Bricks",
			true,
			100,
			100
		);
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof YellowBrick;
	}
}