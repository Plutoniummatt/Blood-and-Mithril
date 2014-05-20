package bloodandmithril.item.material.metal;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.material.Material;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A metal
 *
 * @author Matt
 */
public abstract class Metal extends Material {

	public static void metals(Map<Class<? extends Material>, Material> materials) {
		materials.put(Iron.class, new Iron());
		materials.put(Steel.class, new Steel());
	}

	/**
	 * @return the value of an {@link Ingot} made from this {@link Metal}
	 */
	public abstract long getIngotValue();

	/**
	 * @return the smithing level required to craft an {@link Ingot}
	 */
	public abstract int getIngotCraftingLevel();

	/**
	 * @return the items required to craft an {@link Ingot}
	 */
	public abstract Map<Item, Integer> getRequiredMaterialsToCraftIngot();

	/**
	 * @return the amount of time it takes to craft an {@link Ingot}
	 */
	public abstract float getIngotCraftingDuration();

	/**
	 * @return the {@link TextureRegion} for an {@link Ingot}
	 */
	public abstract TextureRegion getIngotTextureRegion();
}