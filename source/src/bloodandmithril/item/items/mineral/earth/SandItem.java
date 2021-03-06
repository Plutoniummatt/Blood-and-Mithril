package bloodandmithril.item.items.mineral.earth;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.sedimentary.SandTile;

/**
 * Sand, obtained as a result of mining {@link SandTile}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class SandItem extends EarthItem {
	private static final long serialVersionUID = -7756119539773387265L;

	public static TextureRegion SAND;

	/**
	 * Constructor
	 */
	public SandItem() {
		super(1f, 1, false, ItemValues.YELLOWSAND);
	}


	@Override
	protected String internalGetSingular(final boolean firstCap) {
		if (firstCap) {
			return "Sand";
		}
		return "sand";
	}


	@Override
	protected String internalGetPlural(final boolean firstCap) {
		if (firstCap) {
			return "Sand";
		}
		return "sand";
	}


	@Override
	public boolean throwable() {
		return false;
	}


	@Override
	public String getDescription() {
		return "Sand is a naturally occurring granular material composed of finely divided rock and mineral particles.";
	}


	@Override
	protected boolean internalSameAs(final Item other) {
		return other instanceof SandItem;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return SAND;
	}


	@Override
	protected Item internalCopy() {
		return new SandItem();
	}


	@Override
	public boolean rotates() {
		return false;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}