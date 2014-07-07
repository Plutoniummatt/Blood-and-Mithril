package bloodandmithril.item.material.metal;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Copper extends Metal {
	private static final long serialVersionUID = 2380898580519004901L;

	public static TextureRegion COPPERINGOT;
	public static TextureRegion COPPERINGOTICON;

	/**
	 * Package protected constructor
	 */
	Copper() {}


	@Override
	public long getIngotValue() {
		return ItemValues.COPPERINGOT;
	}


	@Override
	public int getIngotCraftingLevel() {
		return 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterialsToCraftIngot() {
		Map<Item, Integer> map = newHashMap();
		return map;
	}


	@Override
	public float getIngotCraftingDuration() {
		return 5f;
	}


	@Override
	public TextureRegion getIngotTextureRegion() {
		return COPPERINGOT;
	}


	@Override
	public TextureRegion getIngotIconTextureRegion() {
		return COPPERINGOTICON;
	}


	@Override
	public String getName() {
		return "Copper";
	}


	@Override
	public float getCombatMultiplier() {
		return 0.8f;
	}
}