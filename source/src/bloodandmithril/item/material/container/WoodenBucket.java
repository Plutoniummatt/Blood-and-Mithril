package bloodandmithril.item.material.container;

import static bloodandmithril.item.material.liquid.LiquidMixtureAnalyzer.getTitle;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.material.liquid.Liquid;

/**
 * Bucket that can contain {@link Liquid}s
 *
 * @author Matt
 */
public class WoodenBucket extends LiquidContainer {
	private static final long serialVersionUID = -4162891941797527242L;
	
	
	public WoodenBucket(Map<Class<? extends Liquid>, Float> containedLiquids) {
		super(1f, 16f, containedLiquids, 50);
	}


	@Override
	protected String getContainerDescription() {
		return "A wooden bucket";
	}

	
	@Override
	protected String getCotainerTitle() {
		return "Bucket";
	}

	
	@Override
	public LiquidContainer clone() {
		return new WoodenBucket(containedLiquids);
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
	public Item combust(int heatLevel) {
		return this;
	}

	
	@Override
	public void render() {
	}
}