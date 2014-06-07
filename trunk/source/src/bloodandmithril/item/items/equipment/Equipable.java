package bloodandmithril.item.items.equipment;

import bloodandmithril.item.affix.Affixed;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.items.equipment.weapon.Broadsword;
import bloodandmithril.item.items.equipment.weapon.ButterflySword;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * This interface allows {@link Item}s that implement it to be rendered
 *
 * @author Matt
 */
public abstract class Equipable extends Item implements Affixed {
	private static final long serialVersionUID = 6029877977431123172L;

	public final EquipmentSlot slot;

	public static void setup() {
		ButterflySword.texture = new TextureRegion(Domain.individualTexture, 528, 0, 47, 12);
		Broadsword.texture = new TextureRegion(Domain.individualTexture, 528, 12, 52, 11);
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
}