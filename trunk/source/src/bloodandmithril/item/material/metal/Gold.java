package bloodandmithril.item.material.metal;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Gold extends Metal {
	private static final long serialVersionUID = 2380898580519004901L;

	public static TextureRegion GOLDINGOT;
	public static TextureRegion GOLDINGOTICON;

	/**
	 * Package protected constructor
	 */
	Gold() {}


	@Override
	public long getIngotValue() {
		return ItemValues.GOLDINGOT;
	}


	@Override
	public int getIngotCraftingLevel() {
		return 55;
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
		return GOLDINGOT;
	}


	@Override
	public TextureRegion getIngotIconTextureRegion() {
		return GOLDINGOTICON;
	}


	@Override
	public String getName() {
		return "Gold";
	}
}