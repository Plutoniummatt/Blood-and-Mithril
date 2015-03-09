package bloodandmithril.item.material.metal;

import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Ingot;
import bloodandmithril.item.material.Material;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A metal
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Metal extends Material {
	private static final long serialVersionUID = 8576471967416805489L;

	public static void metals(Map<Class<? extends Material>, Material> materials) {
		materials.put(Copper.class, new Copper());
		materials.put(Iron.class, new Iron());
		materials.put(Steel.class, new Steel());
		materials.put(Silver.class, new Silver());
		materials.put(Gold.class, new Gold());
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

	/**
	 * @return the {@link TextureRegion} for an {@link Ingot} icon.
	 */
	public abstract TextureRegion getIngotIconTextureRegion();

	/**
	 * @return an ingot made from this metal
	 */
	public Ingot getIngot() {
		return Ingot.ingot(getClass());
	}
}