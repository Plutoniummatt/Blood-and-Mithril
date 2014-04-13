package bloodandmithril.item.material.container;

import static bloodandmithril.item.material.liquid.LiquidMixtureAnalyzer.getTitle;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.material.liquid.Liquid;

import com.google.common.collect.Maps;

/**
 * Bucket that can contain {@link Liquid}s
 *
 * @author Matt
 */
public class WoodenBucket extends LiquidContainer {
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
		return "Bucket";
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

		return (firstCap ? "B" : "b") + "ucket" + content;
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "B" : "b") + "uckets";
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return this;
	}


	@Override
	public void render() {
	}
}