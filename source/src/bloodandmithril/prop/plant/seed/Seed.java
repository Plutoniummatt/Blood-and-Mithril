package bloodandmithril.prop.plant.seed;

import java.util.Collection;

import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.topography.Topography;

/**
 * {@link Prop} representation of a {@link bloodandmithril.item.items.food.plant.Seed}, this will have been planted
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Seed extends Harvestable {
	private static final long serialVersionUID = 6958594764600730817L;

	private final bloodandmithril.item.items.food.plant.Seed seedToRepresent;


	/**
	 * Constructor
	 */
	protected Seed(float x, float y, bloodandmithril.item.items.food.plant.Seed seed) {
		super(x, y, Topography.TILE_SIZE, Topography.TILE_SIZE, true);
		this.seedToRepresent = seed;
	}


	/**
	 * @return The {@link bloodandmithril.item.items.food.plant.Seed} this {@link Seed} represents
	 */
	public bloodandmithril.item.items.food.plant.Seed getSeed() {
		return seedToRepresent;
	}
	
	
	@Override
	public Collection<Item> harvest() {
		return Lists.newArrayList(seedToRepresent);
	}
}