package bloodandmithril.item.material.wood;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Log;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

@Copyright("Matthew Peck 2014")
public class Pine extends Wood {
	private static final long serialVersionUID = 2072576595725963733L;

	public static TextureRegion PINEPLANK;
	public static TextureRegion PINELOG;

	/**
	 * Package protected constructor
	 */
	Pine() {}


	@Override
	public long getLogValue() {
		return ItemValues.PINELOG;
	}


	@Override
	public long getPlankValue() {
		return ItemValues.PINEPLANK;
	}


	@Override
	public int getPlankCraftingLevel() {
		return 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterialsToCraftPlank() {
		HashMap<Item, Integer> map = Maps.newHashMap();
		map.put(Log.log(this.getClass()), 1);
		return map;
	}


	@Override
	public float getPlankCraftingDuration() {
		return 10f;
	}


	@Override
	public TextureRegion getPlankTextureRegion() {
		return PINEPLANK;
	}


	@Override
	public TextureRegion getLogTextureRegion() {
		return PINELOG;
	}


	@Override
	public String getWoodDescription() {
		return "An evergreen coniferous tree which has clusters of long needle-shaped leaves. Many kinds are grown for the soft timber, which is widely used for furniture.";
	}


	@Override
	public String getName() {
		return "Pine";
	}
}