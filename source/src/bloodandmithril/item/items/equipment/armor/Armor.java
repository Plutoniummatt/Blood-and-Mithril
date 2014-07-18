package bloodandmithril.item.items.equipment.armor;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;

/**
 * Superclass for all armor pieces
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Armor<T extends Material> extends Equipable {
	private static final long serialVersionUID = -1573984410574358125L;

	private Class<T> material;

	/**
	 * Constructor
	 */
	protected Armor(float mass, long value, EquipmentSlot slot, Class<T> material) {
		super(mass, true, value, slot);
		this.material = material;
	}

	/**
	 * @return the material this piece of {@link Armor} is made from
	 */
	public Class<T> getMaterial() {
		return material;
	}
}