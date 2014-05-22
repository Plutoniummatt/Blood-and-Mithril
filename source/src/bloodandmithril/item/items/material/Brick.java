package bloodandmithril.item.items.material;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * {@link Item} representing {@link YellowBrickTile}
 *
 * @author Matt
 */
public class Brick extends bloodandmithril.item.items.material.Material {
	private static final long serialVersionUID = -7756119539482746265L;

	/**
	 * Constructor
	 */
	public Brick() {
		super(10f, false, ItemValues.YELLOWBRICK);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		if (firstCap) {
			return "Bricks";
		}
		return "bricks";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return internalGetSingular(firstCap);
	}


	@Override
	public String getDescription() {
		return "Bricks, clay hardened under intense heat";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Brick;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Brick();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}
}