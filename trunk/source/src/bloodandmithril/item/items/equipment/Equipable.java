package bloodandmithril.item.items.equipment;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.affix.Affixed;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;

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
	protected Equipable(float mass, int volume, boolean equippable, long value, EquipmentSlot slot) {
		super(mass, volume, equippable, value);
		this.slot = slot;
	}


	/** Renders this {@link Equipable} */
	public abstract void render(Vector2 position, float angle, boolean flipX);

	/** Processes particle effects, these effects are rendered together with an individual */
	public abstract void particleEffects(Vector2 position, float angle, boolean flipX);

	/** @return The animation index where this {@link Equipable} will be rendered immediately before */
	public abstract int getRenderingIndex(Individual individual);

	/** Whether this {@link Equipable} requies both the {@link EquipmentSlot#MAINHAND} and {@link EquipmentSlot#OFFHAND}*/
	public abstract boolean twoHand();

	/** Updates this equipable when equipped */
	public abstract void update(Equipper equipper, float delta);

	/** Called when unequipped */
	public abstract void onUnequip(Equipper equipper);

	/** Called when equipped */
	public abstract void onEquip(Equipper equipper);
}