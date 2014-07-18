package bloodandmithril.item.items.equipment;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.affix.Affixed;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;

import com.badlogic.gdx.math.Vector2;

/**
 * This interface allows {@link Item}s that implement it to be rendered
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Equipable extends Item implements Affixed {
	private static final long serialVersionUID = 6029877977431123172L;

	public final EquipmentSlot slot;

	public static void setup() {
	}


	/**
	 * Protected constructor
	 */
	protected Equipable(float mass, boolean equippable, long value, EquipmentSlot slot) {
		super(mass, equippable, value);
		this.slot = slot;
	}


	/** Renders this {@link Equipable} */
	public abstract void render(Vector2 position, float angle, boolean flipX);


	/**
	 * @return The animation index where this {@link Equipable} will be rendered immediately before
	 */
	public abstract int getRenderingIndex(Individual individual);
}