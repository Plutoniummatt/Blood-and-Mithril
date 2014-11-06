package bloodandmithril.item.items.mineral.earth;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.world.topography.tile.tiles.sedimentary.SandTile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Sand, obtained as a result of mining {@link SandTile}
 *
 * @author Matt
 */
public class Sand extends Earth {
	private static final long serialVersionUID = -7756119539773387265L;


	/**
	 * Constructor
	 */
	public Sand() {
		super(10f, 10, false, ItemValues.YELLOWSAND);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		if (firstCap) {
			return "Sand";
		}
		return "sand";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		if (firstCap) {
			return "Sand";
		}
		return "sand";
	}


	@Override
	public String getDescription() {
		return "Sand is a naturally occurring granular material composed of finely divided rock and mineral particles.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Sand;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Sand();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}