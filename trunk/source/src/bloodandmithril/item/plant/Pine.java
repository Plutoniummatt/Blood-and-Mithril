package bloodandmithril.item.plant;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Pine extends Item {
	private static final long serialVersionUID = 1882318163053390592L;

	/**
	 * Constructor
	 */
	public Pine() {
		super(1f, false, ItemValues.PINE);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "P" : "p") + "ine";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "P" : "p") + "ine";
	}


	@Override
	public String getDescription() {
		return "Logs of pine trees.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Pine;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Pine();
	}
}