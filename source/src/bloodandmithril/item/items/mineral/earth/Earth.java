package bloodandmithril.item.items.mineral.earth;

import bloodandmithril.item.items.mineral.Mineral;


/**
 * Earth/Dirt/Clay etc
 *
 * @author Matt
 */
public abstract class Earth extends Mineral {

	private static final long serialVersionUID = -6616334367982345623L;

	/**
	 * Protected constructor
	 */
	protected Earth(float mass, int volume, boolean equippable, long value) {
		super(mass, volume, equippable, value);
	}


	@Override
	public String getType() {
		return "Earth";
	}
}