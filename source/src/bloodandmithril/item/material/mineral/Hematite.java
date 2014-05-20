package bloodandmithril.item.material.mineral;

import java.util.Map;

import bloodandmithril.item.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Hematite extends Mineral {

	public static TextureRegion HEMATITE;

	/**
	 * Package protected constructor
	 */
	Hematite() {}

	@Override
	public long getSlabValue() {
		throw new IllegalStateException("Should not be able to have a Hematite slab");
	}


	@Override
	public int getSlabCraftingLevel() {
		throw new IllegalStateException("Should not be able to have a Hematite slab");
	}


	@Override
	public Map<Item, Integer> getRequiredMaterialsToCraftSlab() {
		throw new IllegalStateException("Should not be able to have a Hematite slab");
	}


	@Override
	public float getSlabCraftingDuration() {
		throw new IllegalStateException("Should not be able to have a Hematite slab");
	}


	@Override
	public TextureRegion getSlabTextureRegion() {
		throw new IllegalStateException("Should not be able to have a Hematite slab");
	}


	@Override
	public TextureRegion getRockTextureRegion() {
		return HEMATITE;
	}


	@Override
	public String getName() {
		return "Hematite";
	}

	@Override
	public String getMineralDescription() {
		return "Hematite is a mineral, colored black to steel or silver-gray, brown to reddish brown, or red. It is mined as the main ore of iron.";
	}
}