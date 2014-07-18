package bloodandmithril.item.items.equipment.misc;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;

import com.badlogic.gdx.math.Vector2;

/**
 * Rings, to be put on fingers
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Ring extends Equipable implements Craftable {
	private static final long serialVersionUID = -4877588926698088468L;

	private Class<? extends Material> material;

	/**
	 * Constructor
	 */
	protected Ring(long value, Class<? extends Material> material) {
		super(0f, true, value, EquipmentSlot.RING);
		this.material = material;
	}


	@Override
	public void render(Vector2 position, float angle, boolean flipX) {
		// Do nothing
	}


	/**
	 * @return the material this {@link Ring} is made of
	 */
	public Class<? extends Material> getMaterial() {
		return material;
	}


	/**
	 * @return the description of the item type
	 */
	@Override
	public String getType() {
		return "Ring";
	}
}