package bloodandmithril.prop.plant.seed;

import java.util.Collection;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.plant.Seed;
import bloodandmithril.prop.Growable;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

import com.google.common.collect.Lists;

/**
 * {@link Prop} representation of a {@link Seed}, this will have been planted
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class SeedProp extends Harvestable {
	private static final long serialVersionUID = 6958594764600730817L;

	private final Seed seedToRepresent;

	/** When this value reaches 1, germination will occur */
	private float germinationProgress;

	/**
	 * Constructor
	 */
	protected SeedProp(float x, float y, Seed seed) {
		super(x, y, Topography.TILE_SIZE, Topography.TILE_SIZE, true);
		this.seedToRepresent = seed;
	}


	/**
	 * @return The {@link Seed} this {@link SeedProp} represents
	 */
	public Seed getSeed() {
		return (Seed) seedToRepresent.copy();
	}


	@Override
	public Collection<Item> harvest() {
		return Lists.newArrayList(seedToRepresent.copy());
	}


	/**
	 * @return a {@link Growable} that represents this {@link SeedProp} germinating
	 */
	public abstract Growable germinate();


	public float getGerminationProgress() {
		return germinationProgress;
	}


	public void setGerminationProgress(float germinationProgress) {
		this.germinationProgress = germinationProgress;
	}


	@Override
	public void update(float delta) {
		if (germinationProgress >= 1f) {
			germinationProgress = 1f;
			Growable germinate = germinate();
			Domain.removeProp(id);
			Domain.addProp(germinate);
		} else {
			growth(delta);
		}
	}


	/**
	 * @param seed-specific growth
	 */
	protected abstract void growth(float delta);
}