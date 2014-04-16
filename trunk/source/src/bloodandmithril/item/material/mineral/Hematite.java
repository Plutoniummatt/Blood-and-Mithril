package bloodandmithril.item.material.mineral;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Otherwise known as Iron ore.
 *
 * @author Matt
 */
public class Hematite extends Item {
	private static final long serialVersionUID = 5544474463358187047L;

	/**
	 * Constructor
	 */
	public Hematite() {
		super(2f, false, ItemValues.HEMATITE);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return (firstCap ? "H" : "h") + "ematite";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "H" : "h") + "ematite";
	}


	@Override
	public String getDescription() {
		return "Hematite is a mineral, colored black to steel or silver-gray, brown to reddish brown, or red. It is mined as the main ore of iron.";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Hematite;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected float getRenderAngle() {
		// TODO Auto-generated method stub
		return 0;
	}
}