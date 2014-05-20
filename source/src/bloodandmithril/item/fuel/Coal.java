package bloodandmithril.item.fuel;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.material.earth.Ashes;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Coal extends Item implements Fuel {
	private static final long serialVersionUID = 6399640412435082388L;

	public static TextureRegion COAL;

	/**
	 * Constructor
	 */
	public Coal() {
		super(0.1f, false, ItemValues.COAL);
	}


	@Override
	public float getCombustionDuration() {
		return 20;
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return firstCap ? "Coal" : "coal";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return firstCap ? "Coal" : "coal";
	}


	@Override
	public String getDescription() {
		return "Coal is a combustible black or brownish-black sedimentary rock usually occurring in rock strata in layers or veins called coal beds or coal seams";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Coal;
	}


	@Override
	public Item consume() {
		return new Ashes();
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return COAL;
	}


	@Override
	protected Item internalCopy() {
		return new Coal();
	}
}