package bloodandmithril.item.equipment.armor;

import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;

/**
 * Superclass for all armor pieces
 *
 * @author Matt
 */
public abstract class Armor extends Equipable {
	private static final long serialVersionUID = -1573984410574358125L;

	private Class<? extends Material> material;

	/**
	 * Constructor
	 */
	protected Armor(float mass, long value, EquipmentSlot slot, Class<? extends Material> material) {
		super(mass, true, value, slot);
		this.material = material;
	}

	/**
	 * @return the material this piece of {@link Armor} is made from
	 */
	public Class<? extends Material> getMaterial() {
		return material;
	}
}