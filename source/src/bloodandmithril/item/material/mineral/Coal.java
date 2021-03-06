package bloodandmithril.item.material.mineral;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

@Copyright("Matthew Peck 2014")
public class Coal extends Mineral {
	private static final long serialVersionUID = -3925049718035013900L;

	public static TextureRegion COAL;

	/**
	 * Package protected constructor
	 */
	Coal() {}

	@Override
	public long getRockValue() {
		return ItemValues.COAL;
	}


	@Override
	public long getSlabValue() {
		throw new IllegalStateException("Should not be able to have a Coal slab");
	}


	@Override
	public int getSlabCraftingLevel() {
		throw new IllegalStateException("Should not be able to have a Coal slab");
	}


	@Override
	public Map<Item, Integer> getRequiredMaterialsToCraftSlab() {
		throw new IllegalStateException("Should not be able to have a Coal slab");
	}


	@Override
	public float getSlabCraftingDuration() {
		throw new IllegalStateException("Should not be able to have a Coal slab");
	}


	@Override
	public TextureRegion getSlabTextureRegion() {
		throw new IllegalStateException("Should not be able to have a Coal slab");
	}


	@Override
	public TextureRegion getRockTextureRegion() {
		return COAL;
	}


	@Override
	public String getName() {
		return "Coal";
	}


	@Override
	public String getMineralDescription() {
		return "Coal is a combustible black or brownish-black sedimentary rock usually occurring in rock strata in layers or veins called coal beds or coal seams.";
	}
}