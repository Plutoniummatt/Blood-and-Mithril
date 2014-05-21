package bloodandmithril.item.misc;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.items.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;

/**
 * A generic key
 *
 * @author Matt
 */
public abstract class Key extends Item {
	private static final long serialVersionUID = 607926655790662998L;
	public static final String description = "A key is a device that is used to operate a lock.";

	/**
	 * Constructor
	 */
	protected Key(float mass, long value) {
		super(mass, false, value);
	}


	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			getDescription(),
			Color.ORANGE,
			BloodAndMithrilClient.WIDTH/2 - 175,
			BloodAndMithrilClient.HEIGHT/2 + 100,
			350,
			200,
			"Key",
			true,
			100,
			100
		);
	}
}