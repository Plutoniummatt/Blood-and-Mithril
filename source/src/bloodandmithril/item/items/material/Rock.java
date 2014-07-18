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
public class Rock extends bloodandmithril.item.items.material.Material {
	private static final long serialVersionUID = -7786694866208809183L;

	private final Class<? extends Mineral> mineral;

	/**
	 * Private constructor
	 */
	private Rock(Class<? extends Mineral> mineral) {
		super(2f, false);
		this.mineral = mineral;
		setValue(Material.getMaterial(mineral).getRockValue());
	}


	/**
	 * Static instance getter
	 */
	public static Rock rock(Class<? extends Mineral> mineral) {
		return new Rock(mineral);
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
		if (other instanceof Rock) {
			return getMineral().equals(((Rock) other).getMineral());
		}

		return false;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return Material.getMaterial(getMineral()).getRockTextureRegion();
	}


	@Override
	protected Item internalCopy() {
		return rock(getMineral());
	}


	/**
	 * @return The mineral this {@link Rock} is composed of
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