package bloodandmithril.item.material.mineral;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Clay
 *
 * @author Matt
 */
public class Clay extends Item {
	private static final long serialVersionUID = 883456114549112166L;

	/**
	 * Constructor
	 */
	public Clay() {
		super(0.5f, false, ItemValues.CLAY);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return (firstCap ? "C" : "c") + "lay";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "C" : "c") + "lay";
	}


	@Override
	public String getDescription() {
		return "Clay";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Clay;
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