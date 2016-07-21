package bloodandmithril.item.items.food.plant;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.plant.PlantProp;

/**
 * A seed, for growing {@link PlantProp}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public abstract class SeedItem extends Item {
	private static final long serialVersionUID = -9042024316031391240L;

	/**
	 * Constructor
	 */
	protected SeedItem(final float mass, final long value) {
		super(mass, 0, false, value);
	}

	@Override
	protected boolean internalSameAs(final Item other) {
		return other.getClass().equals(getClass());
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.SEED;
	}

	@Override
	public float getUprightAngle() {
		return 90f;
	}

	/**
	 * @return a new instance of {@link bloodandmithril.prop.plant.seed.SeedProp} which represents this {@link SeedItem}
	 */
	public abstract bloodandmithril.prop.plant.seed.SeedProp getPropSeed();
}