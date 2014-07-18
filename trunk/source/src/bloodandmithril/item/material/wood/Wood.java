package bloodandmithril.item.material.wood;

import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Log;
import bloodandmithril.item.items.material.Plank;
import bloodandmithril.item.material.Material;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * {@link Material} - Wood
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Wood extends Material {
	private static final long serialVersionUID = 232426834124731122L;

	public static void woods(Map<Class<? extends Material>, Material> materials) {
		materials.put(Pine.class, new Pine());
	}

	/**
	 * @return the value of an {@link Log} made from this {@link Wood}
	 */
	public abstract long getLogValue();

	/**
	 * @return the value of an {@link Plank} made from this {@link Wood}
	 */
	public abstract long getPlankValue();

	/**
	 * @return the carpentry level required to craft a {@link Plank}
	 */
	public abstract int getPlankCraftingLevel();

	/**
	 * @return the items required to craft an {@link Plank}
	 */
	public abstract Map<Item, Integer> getRequiredMaterialsToCraftPlank();

	/**
	 * @return the amount of time it takes to craft a {@link Plank}
	 */
	public abstract float getPlankCraftingDuration();

	/**
	 * @return the {@link TextureRegion} for a {@link Plank}
	 */
	public abstract TextureRegion getPlankTextureRegion();

	/**
	 * @return the {@link TextureRegion} for a {@link Log}
	 */
	public abstract TextureRegion getLogTextureRegion();

	/**
	 * @return the description of this {@link Wood}
	 */
	public abstract String getWoodDescription();
}