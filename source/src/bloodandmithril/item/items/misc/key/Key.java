package bloodandmithril.item.items.misc.key;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;

/**
 * A generic key
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Key extends Item {
	private static final long serialVersionUID = 607926655790662998L;
	public static final String description = "A key is a device that is used to operate a lock.";

	/**
	 * Constructor
	 */
	protected Key(float mass, long value) {
		super(mass, 0, false, value);
	}


	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			getDescription(),
			Color.ORANGE,
			350,
			200,
			"Key",
			true,
			100,
			100
		);
	}


	/**
	 * @return the description of the item type
	 */
	@Override
	public ItemCategory getType() {
		return ItemCategory.KEY;
	}

	@Override
	public float getUprightAngle() {
		return 90f;
	}
}