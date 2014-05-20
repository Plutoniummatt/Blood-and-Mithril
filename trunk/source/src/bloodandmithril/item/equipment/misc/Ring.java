package bloodandmithril.item.equipment.misc;

import bloodandmithril.item.Craftable;
import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;

import com.badlogic.gdx.math.Vector2;

/**
 * Rings, to be put on fingers
 *
 * @author Matt
 */
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
}