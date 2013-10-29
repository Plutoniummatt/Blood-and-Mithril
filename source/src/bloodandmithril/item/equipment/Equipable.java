package bloodandmithril.item.equipment;

import bloodandmithril.item.Equipper.EquipmentSlot;
import bloodandmithril.item.Item;

import com.badlogic.gdx.math.Vector2;

/**
 * This interface allows {@link Item}s that implement it to be rendered
 *
 * @author Matt
 */
public abstract class Equipable extends Item {
	private static final long serialVersionUID = 6029877977431123172L;

	public final EquipmentSlot slot;

	/**
	 * Protected constructor
	 */
	protected Equipable(float mass, boolean equippable, long value, EquipmentSlot slot) {
		super(mass, equippable, value);
		this.slot = slot;
	}


	/** Renders this {@link Equipable} */
	public abstract void render(Vector2 position, float angle, boolean flipX);
}
