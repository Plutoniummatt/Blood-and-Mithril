package bloodandmithril.item.material.wood;

import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.LogItem;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.items.material.StickItem;
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
		materials.put(StandardWood.class, new StandardWood());
	}

	/**
	 * @return the value of an {@link LogItem} made from this {@link Wood}
	 */
	public abstract long getLogValue();

	/**
	 * @return the value of an {@link PlankItem} made from this {@link Wood}
	 */
	public abstract long getPlankValue();

	/**
	 * @return the carpentry level required to craft a {@link PlankItem}
	 */
	public abstract int getPlankCraftingLevel();

	/**
	 * @return the items required to craft a {@link PlankItem}
	 */
	public abstract Map<Item, Integer> getRequiredMaterialsToCraftPlank();

	/**
	 * @return the items required to craft a {@link StickItem}
	 */
	public abstract Map<Item, Integer> getRequiredMaterialsToCraftStick();

	/**
	 * @return the amount of time it takes to craft a {@link PlankItem}
	 */
	public abstract float getPlankCraftingDuration();

	/**
	 * @return the amount of time it takes to craft a {@link StickItem}
	 */
	public abstract float getStickCraftingDuration();

	/**
	 * @return the {@link TextureRegion} for a {@link PlankItem}
	 */
	public abstract TextureRegion getPlankTextureRegion();

	/**
	 * @return the {@link TextureRegion} for a {@link LogItem}
	 */
	public abstract TextureRegion getLogTextureRegion();

	/**
	 * @return the description of this {@link Wood}
	 */
	public abstract String getWoodDescription();
}