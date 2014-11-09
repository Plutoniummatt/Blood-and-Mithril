package bloodandmithril.item.items.material;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.mineral.Mineral;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * {@link Item} representing {@link YellowBrickTile}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Bricks extends bloodandmithril.item.items.material.Material {
	private static final long serialVersionUID = -7756119539482746265L;
	
	/** {@link TextureRegion} of the {@link Bricks} */
	public static TextureRegion BRICKS;
	public final Class<? extends Mineral> material;

	/**
	 * Constructor
	 */
	private Bricks(Class<? extends Mineral> material) {
		super(1f, 4, false, ItemValues.YELLOWBRICK);
		this.material = material;
	}
	
	
	public static Bricks bricks(Class<? extends Mineral> material) {
		return new Bricks(material);
	}
	

	@Override
	protected String internalGetSingular(boolean firstCap) {
		if (firstCap) {
			return Material.getMaterial(material).getName() + " Bricks";
		}
		return Material.getMaterial(material).getName() + " bricks";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return internalGetSingular(firstCap);
	}


	@Override
	public String getDescription() {
		return "Bricks, These are made from " + Material.getMaterial(material).getName();
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Bricks;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return BRICKS;
	}


	@Override
	protected Item internalCopy() {
		return new Bricks(material);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}
}