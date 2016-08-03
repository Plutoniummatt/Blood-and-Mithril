package bloodandmithril.item.material.mineral;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.RockItem;
import bloodandmithril.item.items.material.SlabItem;
import bloodandmithril.item.material.Material;

/**
 * {@link Material} - Rocks
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Mineral extends Material {
	private static final long serialVersionUID = 9102235184805389671L;

	public static void minerals(final Map<Class<? extends Material>, Material> materials) {
		materials.put(Hematite.class, new Hematite());
		materials.put(Coal.class, new Coal());
		materials.put(SandStone.class, new SandStone());
	}

	/**
	 * @return the value of an {@link RockItem} made from this {@link Mineral}
	 */
	public abstract long getRockValue();

	/**
	 * @return the value of an {@link SlabItem} made from this {@link Mineral}
	 */
	public abstract long getSlabValue();

	/**
	 * @return the masonry level required to craft a {@link SlabItem}
	 */
	public abstract int getSlabCraftingLevel();

	/**
	 * @return the items required to craft an {@link SlabItem}
	 */
	public abstract Map<Item, Integer> getRequiredMaterialsToCraftSlab();

	/**
	 * @return the amount of time it takes to craft a {@link SlabItem}
	 */
	public abstract float getSlabCraftingDuration();

	/**
	 * @return the {@link TextureRegion} for a {@link SlabItem}
	 */
	public abstract TextureRegion getSlabTextureRegion();

	/**
	 * @return the {@link TextureRegion} for a rock, in its natural form
	 */
	public abstract TextureRegion getRockTextureRegion();

	/**
	 * @return the description of this {@link Mineral}
	 */
	public abstract String getMineralDescription();

	@Override
	public float getCombatMultiplier() {
		return 0f;
	}
}