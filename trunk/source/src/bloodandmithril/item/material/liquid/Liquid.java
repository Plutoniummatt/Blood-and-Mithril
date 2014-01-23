package bloodandmithril.item.material.liquid;

import bloodandmithril.character.Individual;

/**
 * A class representing a liquid
 *
 * @author Matt
 */
public abstract class Liquid {

	public abstract void drink(float amount, Individual affected);

	public abstract String getDescription();

	/** Water, only quenches thirst */
	public static class Water extends Liquid {
		@Override
		public void drink(float amount, Individual affected) {
			affected.increaseThirst(amount);
		}
		@Override
		public String getDescription() {
			return "The liquid that keeps you alive.";
		}
	}

	/** Empty */
	public static class Empty extends Liquid {
		@Override
		public void drink(float amount, Individual affected) {
		}
		@Override
		public String getDescription() {
			return "Empty";
		}
	}
}