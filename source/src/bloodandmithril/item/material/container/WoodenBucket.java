package bloodandmithril.item.material.container;

import static bloodandmithril.item.material.liquid.LiquidMixtureAnalyzer.getTitle;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.equipment.Craftable;
import bloodandmithril.item.material.liquid.Liquid;
import bloodandmithril.item.material.plant.Pine;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * Bucket that can contain {@link Liquid}s
 *
 * @author Matt
 */
public class WoodenBucket extends LiquidContainer implements Craftable {
	private static final long serialVersionUID = -4162891941797527242L;


	/**
	 * Constructor
	 */
	public WoodenBucket(Map<Class<? extends Liquid>, Float> containedLiquids) {
		super(1f, 16f, containedLiquids, ItemValues.WOODENBUCKET);
	}


	/**
	 * Constructor
	 */
	public WoodenBucket() {
		super(1f, 16f, Maps.newHashMap(), 50);
	}


	@Override
	public String getDescription() {
		return "A wooden bucket";
	}


	@Override
	protected String getCotainerTitle() {
		return "Wooden Bucket";
	}


	@Override
	public LiquidContainer clone() {
		return new WoodenBucket(newHashMap(containedLiquids));
	}


	@Override
	public String getSingular(boolean firstCap) {
		String content = "";
		if (!containedLiquids.isEmpty()) {
			content = " of " + getTitle(containedLiquids, getTotalAmount()) + " (" + String.format("%.2f", getTotalAmount()) + "/" + String.format("%.2f", maxAmount) + ")";
		}

		return (firstCap ? "W" : "w") + "ooden bucket" + content;
	}


	@Override
	public String getPlural(boolean firstCap) {
		String content = "";
		if (!containedLiquids.isEmpty()) {
			content = " of " + getTitle(containedLiquids, getTotalAmount()) + " (" + String.format("%.2f", getTotalAmount()) + "/" + String.format("%.2f", maxAmount) + ")";
		}

		return (firstCap ? "W" : "w") + "ooden buckets" + content;
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getCarpentry() >= 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = Maps.newHashMap();
		map.put(new Pine(), 2);
		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 10f;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected LiquidContainer copyContainer() {
		return new WoodenBucket();
	}
}