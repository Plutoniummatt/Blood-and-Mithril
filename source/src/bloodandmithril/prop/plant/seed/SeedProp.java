package bloodandmithril.prop.plant.seed;

import java.util.Collection;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.plant.SeedItem;
import bloodandmithril.prop.Growable;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

import com.google.common.collect.Lists;

/**
 * {@link Prop} representation of a {@link SeedItem}, this will have been planted
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class SeedProp extends Growable implements Harvestable {
	private static final long serialVersionUID = 6958594764600730817L;

	private final SeedItem seedToRepresent;

	/** When this value reaches 1, germination will occur */
	private float germinationProgress;

	/**
	 * Constructor
	 */
	protected SeedProp(float x, float y, SeedItem seed, SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf) {
		super(x, y, Topography.TILE_SIZE, Topography.TILE_SIZE, true, Depth.MIDDLEGROUND, canPlaceOnTopOf);
		this.seedToRepresent = seed;
	}


	/**
	 * @return The {@link SeedItem} this {@link SeedProp} represents
	 */
	public SeedItem getSeed() {
		return (SeedItem) seedToRepresent.copy();
	}


	@Override
	public Collection<Item> harvest(boolean canReceive) {
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
			Domain.getWorld(getWorldId()).props().removeProp(id);
			Domain.getWorld(getWorldId()).props().addProp(germinate);
		} else {
			growth(delta);
		}
	}


	/**
	 * @param seed-specific growth
	 */
	protected abstract void growth(float delta);
}