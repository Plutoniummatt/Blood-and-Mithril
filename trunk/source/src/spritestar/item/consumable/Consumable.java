package spritestar.item.consumable;

import spritestar.character.Individual;
import spritestar.item.Item;

/**
 * An {@link Item} that can be consumed.
 *
 * @author Matt
 */
public abstract class Consumable extends Item {
	private static final long serialVersionUID = 6866616217945762547L;

	/**
	 * Constructor
	 */
	protected Consumable(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
	}
	
	/** Affect the {@link Individual} that has consumed this {@link Consumable} */
	public abstract boolean consume(Individual consumer);
}
