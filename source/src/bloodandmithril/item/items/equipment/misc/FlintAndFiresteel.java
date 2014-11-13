package bloodandmithril.item.items.equipment.misc;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.misc.Misc;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Basic firestarter, not good for much else
 *
 * @author Matt
 */
public class FlintAndFiresteel extends Misc {
	private static final long serialVersionUID = 1209549782426000939L;

	/**
	 * Constructor
	 */
	public FlintAndFiresteel() {
		super(0.1f, 1, false, ItemValues.FLINTANDFIRESTEEL);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return firstCap ? "Flint and firesteel" : "flint and firesteel";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return firstCap ? "Flint and firesteel" : "flint and firesteel";
	}


	@Override
	public String getDescription() {
		return "Basic fire starting kit, strike the steel against the flint to produce sparks";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof FlintAndFiresteel;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new FlintAndFiresteel();
	}
}