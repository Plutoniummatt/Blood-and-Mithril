package bloodandmithril.item.material.mineral;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.world.topography.tile.tiles.sedimentary.SandTile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Sand, obtained as a result of mining {@link SandTile}
 *
 * @author Matt
 */
public class Sand extends Item {
	private static final long serialVersionUID = -7756119539773387265L;


	/**
	 * Constructor
	 */
	public Sand() {
		super(10f, false, ItemValues.YELLOWSAND);
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
}