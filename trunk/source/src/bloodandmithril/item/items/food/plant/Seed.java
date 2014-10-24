package bloodandmithril.item.items.food.plant;

import bloodandmithril.item.items.Item;
import bloodandmithril.prop.plant.Plant;

/**
 * A seed, for growing {@link Plant}s
 *
 * @author Matt
 */
public abstract class Seed extends Item {
	private static final long serialVersionUID = -9042024316031391240L;

	/**
	 * Constructor
	 */
	protected Seed(float mass, long value) {
		super(mass, false, value);
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other.getClass().equals(getClass());
	}


	@Override
	public String getType() {
		return "Seed";
	}


	/**
	 * @return a new instance of {@link bloodandmithril.prop.plant.seed.SeedProp} which represents this {@link Seed}
	 */
	public abstract bloodandmithril.prop.plant.seed.SeedProp getPropSeed();
}