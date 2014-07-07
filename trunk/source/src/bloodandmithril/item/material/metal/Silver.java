package bloodandmithril.item.material.metal;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Silver extends Metal {
	private static final long serialVersionUID = 2380898580519004901L;

	public static TextureRegion SILVERINGOT;
	public static TextureRegion SILVERINGOTICON;

	/**
	 * Package protected constructor
	 */
	Silver() {}


	@Override
	public long getIngotValue() {
		return ItemValues.SILVERINGOT;
	}


	@Override
	public int getIngotCraftingLevel() {
		return 45;
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
		return SILVERINGOT;
	}


	@Override
	public TextureRegion getIngotIconTextureRegion() {
		return SILVERINGOTICON;
	}


	@Override
	public String getName() {
		return "Silver";
	}


	@Override
	public float getCombatMultiplier() {
		return 0.5f;
	}
}