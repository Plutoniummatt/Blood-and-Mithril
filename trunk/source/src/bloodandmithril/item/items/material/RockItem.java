package bloodandmithril.item.items.material;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.mineral.Mineral;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A rock made from a {@link Mineral}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RockItem extends bloodandmithril.item.items.material.MaterialItem {
	private static final long serialVersionUID = -7786694866208809183L;

	private final Class<? extends Mineral> mineral;

	/**
	 * Private constructor
	 */
	private RockItem(Class<? extends Mineral> mineral) {
		super(1f, 5, false);
		this.mineral = mineral;
		setValue(Material.getMaterial(mineral).getRockValue());
		bounces();
	}


	/**
	 * Static instance getter
	 */
	public static RockItem rock(Class<? extends Mineral> mineral) {
		return new RockItem(mineral);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return Material.getMaterial(getMineral()).getName();
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return Material.getMaterial(getMineral()).getName();
	}


	@Override
	public String getDescription() {
		return Material.getMaterial(getMineral()).getMineralDescription();
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof RockItem) {
			return getMineral().equals(((RockItem) other).getMineral());
		}

		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return Material.getMaterial(getMineral()).getRockTextureRegion();
	}


	@Override
	protected Item internalCopy() {
		return rock(getMineral());
	}


	/**
	 * @return The mineral this {@link RockItem} is composed of
	 */
	public Class<? extends Mineral> getMineral() {
		return mineral;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}