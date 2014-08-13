package bloodandmithril.item.items.mineral.earth;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Ashes extends Earth {
	private static final long serialVersionUID = 988154990456038686L;
	public static final String description = "The residue of combustion, mostly consisting of metal oxides.";

	/**
	 * Constructor
	 */
	public Ashes() {
		super(0.2f, false, ItemValues.ASHES);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return "Ashes";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return "Ashes";
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Ashes;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Ashes();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}