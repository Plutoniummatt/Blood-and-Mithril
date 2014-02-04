package bloodandmithril.item.material.plant;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.Window;


/**
 * Felberries
 *
 * @author Matt
 */
public class Felberry extends Item implements Consumable {

	/**
	 * @param mass
	 * @param equippable
	 * @param value
	 */
	protected Felberry(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see bloodandmithril.item.Consumable#consume(bloodandmithril.character.Individual)
	 */
	@Override
	public boolean consume(Individual consumer) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see bloodandmithril.item.Item#getSingular(boolean)
	 */
	@Override
	public String getSingular(boolean firstCap) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see bloodandmithril.item.Item#getPlural(boolean)
	 */
	@Override
	public String getPlural(boolean firstCap) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see bloodandmithril.item.Item#getInfoWindow()
	 */
	@Override
	public Window getInfoWindow() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see bloodandmithril.item.Item#sameAs(bloodandmithril.item.Item)
	 */
	@Override
	public boolean sameAs(Item other) {
		// TODO Auto-generated method stub
		return false;
	}

}
