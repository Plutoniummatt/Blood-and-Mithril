package bloodandmithril.item.material.wood;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Log;
import bloodandmithril.item.items.material.Plank;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

@Copyright("Matthew Peck 2014")
public class StandardWood extends Wood {
	private static final long serialVersionUID = 2072576595725963733L;

	public static TextureRegion WOODPLANK;
	public static TextureRegion WOODLOG;

	/**
	 * Package protected constructor
	 */
	StandardWood() {}


	@Override
	public long getLogValue() {
		return ItemValues.WOODLOG;
	}


	@Override
	public long getPlankValue() {
		return ItemValues.WOODPLANK;
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
		return WOODPLANK;
	}


	@Override
	public TextureRegion getLogTextureRegion() {
		return WOODLOG;
	}


	@Override
	public String getWoodDescription() {
		return "Wood is a porous and fibrous structural tissue found in the stems and roots of trees and other woody plants. It has been used for thousands of years for both fuel and as a construction material";
	}


	@Override
	public String getName() {
		return "Wood";
	}


	@Override
	public Map<Item, Integer> getRequiredMaterialsToCraftStick() {
		HashMap<Item, Integer> map = Maps.newHashMap();
		map.put(Plank.plank(this.getClass()), 1);
		return map;
	}


	@Override
	public float getStickCraftingDuration() {
		return 10;
	}


	@Override
	public float getCombatMultiplier() {
		return 1f;
	}
}