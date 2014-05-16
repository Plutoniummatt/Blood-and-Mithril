package bloodandmithril.item.equipment;

import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper.EquipmentSlot;

import com.badlogic.gdx.math.Vector2;

/**
 * Rings, to be put on fingers
 *
 * @author Matt
 */
public abstract class Ring extends Equipable implements Craftable {
	private static final long serialVersionUID = -4877588926698088468L;

	/**
	 * Constructor
	 */
	protected Ring(long value) {
		super(0f, true, value, EquipmentSlot.RING);
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		// Do nothing
	}
}