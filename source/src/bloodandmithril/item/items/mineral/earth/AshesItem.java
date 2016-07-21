package bloodandmithril.item.items.mineral.earth;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

@Copyright("Matthew Peck 2016")
public class AshesItem extends EarthItem {
	private static final long serialVersionUID = 988154990456038686L;
	public static final String description = "The residue of combustion, mostly consisting of metal oxides.";

	public static TextureRegion ASHES;

	/**
	 * Constructor
	 */
	public AshesItem() {
		super(1f, 1, false, ItemValues.ASHES);
	}


	@Override
	protected String internalGetSingular(final boolean firstCap) {
		return "Ashes";
	}


	@Override
	protected String internalGetPlural(final boolean firstCap) {
		return "Ashes";
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public boolean throwable() {
		return false;
	}


	@Override
	protected boolean internalSameAs(final Item other) {
		return other instanceof AshesItem;
	}


	@Override
	public boolean rotates() {
		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return ASHES;
	}


	@Override
	protected Item internalCopy() {
		return new AshesItem();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}