package bloodandmithril.item.material.mineral;

import java.util.Map;

import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.items.material.Slab;
import bloodandmithril.item.material.Material;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * {@link Material} - Rocks
 *
 * @author Matt
 */
public abstract class Mineral extends Material {
	private static final long serialVersionUID = 9102235184805389671L;

	public static void minerals(Map<Class<? extends Material>, Material> materials) {
		materials.put(Hematite.class, new Hematite());
		materials.put(Coal.class, new Coal());
	}

	/**
	 * @return the value of an {@link Rock} made from this {@link Mineral}
	 */
	public abstract long getRockValue();

	/**
	 * @return the value of an {@link Slab} made from this {@link Mineral}
	 */
	public abstract long getSlabValue();

	/**
	 * @return the masonry level required to craft a {@link Slab}
	 */
	public abstract int getSlabCraftingLevel();

	/**
	 * @return the items required to craft an {@link Slab}
	 */
	public abstract Map<Item, Integer> getRequiredMaterialsToCraftSlab();

	/**
	 * @return the amount of time it takes to craft a {@link Slab}
	 */
	public abstract float getSlabCraftingDuration();

	/**
	 * @return the {@link TextureRegion} for a {@link Slab}
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
}