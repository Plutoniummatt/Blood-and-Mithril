package bloodandmithril.item.items.equipment.offhand;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.equipment.Equipper;

import com.badlogic.gdx.math.Vector2;

/**
 * Shields
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Shield extends OffhandEquipment implements Craftable {
	private static final long serialVersionUID = 5112607606681273075L;

	/**
	 * Constructor
	 */
	protected Shield(float mass, int volume, long value) {
		super(mass, volume, value);
	}


	@Override
	public ItemCategory getType() {
		return ItemCategory.OFFHAND;
	}


	/**
	 * @return the probability to block an attack with this shield
	 */
	public abstract float getBlockChance();


	@Override
	public void particleEffects(Vector2 position, float angle, boolean flipX) {
	}


	@Override
	public void update(Equipper equipper, float delta) {
	}


	@Override
	public void onUnequip(Equipper equipper) {
	}


	@Override
	public void onEquip(Equipper equipper) {
	}
}