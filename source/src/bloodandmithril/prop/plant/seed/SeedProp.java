package bloodandmithril.prop.plant.seed;

import java.util.Collection;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.plant.SeedItem;
import bloodandmithril.prop.Growable;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

/**
 * {@link Prop} representation of a {@link SeedItem}, this will have been planted
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@UpdatedBy()
public abstract class SeedProp extends Growable implements Harvestable {
	private static final long serialVersionUID = 6958594764600730817L;

	private final SeedItem seedToRepresent;

	/** When this value reaches 1, germination will occur */
	private float germinationProgress;

	/**
	 * Constructor
	 */
	protected SeedProp(float x, float y, SeedItem seed, SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf) {
		super(x, y, Topography.TILE_SIZE, Topography.TILE_SIZE, true, Depth.MIDDLEGROUND, canPlaceOnTopOf, false);
		this.seedToRepresent = seed;
	}
	
	
	@Override
	public Color getContextMenuColor() {
		return Color.OLIVE;
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


	/**
	 * @param seed-specific growth
	 */
	public abstract void growth(float delta);
}