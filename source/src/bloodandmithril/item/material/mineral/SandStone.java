package bloodandmithril.item.material.mineral;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.RockItem;

/**
 * Sand stone.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class SandStone extends Mineral {
	private static final long serialVersionUID = 3697380136082090890L;

	public static TextureRegion SANDSTONE;

	/**
	 * Package protected constructor
	 */
	SandStone() {}

	@Override
	public long getRockValue() {
		return ItemValues.SANDSTONE;
	}


	@Override
	public long getSlabValue() {
		return ItemValues.SANDSTONESLAB;
	}


	@Override
	public int getSlabCraftingLevel() {
		return 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterialsToCraftSlab() {
		final Map<Item, Integer> map = Maps.newHashMap();
		map.put(RockItem.rock(SandStone.class), 2);
		return map;
	}


	@Override
	public float getSlabCraftingDuration() {
		return 5f;
	}


	@Override
	public TextureRegion getSlabTextureRegion() {
		return null;
	}


	@Override
	public TextureRegion getRockTextureRegion() {
		return SANDSTONE;
	}


	@Override
	public String getMineralDescription() {
		return "Sandstone is a clastic sedimentary rock composed mainly of sand-sized minerals or rock grains.";
	}


	@Override
	public String getName() {
		return "Sandstone";
	}
}