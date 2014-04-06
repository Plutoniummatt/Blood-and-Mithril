package bloodandmithril.item.material.container;

import static bloodandmithril.item.material.liquid.LiquidMixtureAnalyzer.getTitle;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.material.liquid.Liquid;

/**
 * A {@link Bottle} made from glass
 *
 * @author Matt
 */
public class GlassBottle extends LiquidContainer {
	private static final long serialVersionUID = 3427844446930493119L;

	/**
	 * Constructor
	 */
	public GlassBottle(Map<Class<? extends Liquid>, Float> containedLiquids) {
		super(0.1f, 1f, containedLiquids, ItemValues.GLASSBOTTLE);
	}


	@Override
	public String getSingular(boolean firstCap) {
		String content = "";
		if (!containedLiquids.isEmpty()) {
			content = " of " + getTitle(containedLiquids, getTotalAmount()) + " (" + String.format("%.2f", getTotalAmount()) + "/" + String.format("%.2f", maxAmount) + ")";
		}

		return (firstCap ? "G" : "g") + "lass bottle" + content;
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "G" : "g") + "lass bottles";
	}


	@Override
	public LiquidContainer clone() {
		return new GlassBottle(newHashMap(containedLiquids));
	}


	@Override
	public Item combust(int heatLevel) {
		return this;
	}


	@Override
	public void render() {
	}


	@Override
	protected String getContainerDescription() {
		return "A bottle made from glass.";
	}


	@Override
	protected String getCotainerTitle() {
		return "Glass bottle";
	}
}