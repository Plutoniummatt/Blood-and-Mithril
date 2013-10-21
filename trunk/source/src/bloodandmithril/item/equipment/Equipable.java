package bloodandmithril.item.equipment;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.item.Item;

/**
 * This interface allows {@link Item}s that implement it to be rendered
 *
 * @author Matt
 */
public abstract class Equipable extends Item {
	private static final long serialVersionUID = 6029877977431123172L;
	
	
	/**
	 * Protected constructor
	 */
	protected Equipable(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
	}
	
	
	/** Renders this {@link Equipable} */
	public abstract void render(Vector2 position, float angle, boolean flipX);
}
