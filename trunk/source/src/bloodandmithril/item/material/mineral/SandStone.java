package bloodandmithril.item.material.mineral;

import java.util.Map;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Rock;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * Sand stone.
 *
 * @author Matt
 */
public class SandStone extends Mineral {
	private static final long serialVersionUID = 3697380136082090890L;
	
	public static TextureRegion SANDSTONE;
	
	public static final Color color = new Color(166f/255f, 124f/255f, 82f/255f, 1f);

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
		Map<Item, Integer> map = Maps.newHashMap();
		map.put(Rock.rock(SandStone.class), 2);
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

	@Override
	public Color getColor() {
		return color;
	}
}