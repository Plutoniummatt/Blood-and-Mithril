package bloodandmithril.item.material.container;

import static bloodandmithril.item.material.liquid.LiquidMixtureAnalyzer.getDescription;
import static bloodandmithril.item.material.liquid.LiquidMixtureAnalyzer.getTitle;

import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Maps;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.liquid.Liquid;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

/**
 * Contains {@link Liquid}s
 *
 * @author Matt
 */
public abstract class LiquidContainer extends Item {
	private static final long serialVersionUID = 5895479054869624858L;
	
	protected Map<Class<? extends Liquid>, Float> containedLiquids;
	protected final float maxAmount;

	/**
	 * Constructor
	 */
	protected LiquidContainer(float mass, float maxAmount, Map<Class<? extends Liquid>, Float> containedLiquids, long value) {
		super(mass, false, value);
		this.maxAmount = maxAmount;
		this.containedLiquids = containedLiquids;
	}


	public void drinkFrom(float amount, Individual affected) {
		float fraction = amount/getTotalAmount();
		
		try {
			for (Entry<Class<? extends Liquid>, Float> entry : Maps.newHashMap(containedLiquids).entrySet()) {
				if (fraction >= 1f) {
					entry.getKey().newInstance().drink(entry.getValue(), affected);
					containedLiquids.remove(entry.getKey());
				} else {
					System.out.println("Drinking " + entry.getValue() * fraction + " of " + entry.getKey().getSimpleName());
					entry.getKey().newInstance().drink(entry.getValue() * fraction, affected);
					containedLiquids.put(entry.getKey(), entry.getValue() * (1f - fraction));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@Override
	public Window getInfoWindow() {
		try {
			return new MessageWindow(
				containedLiquids.isEmpty() ? getContainerDescription() : getDescription(containedLiquids, getTotalAmount()),
				Color.ORANGE,
				BloodAndMithrilClient.WIDTH/2 - 175,
				BloodAndMithrilClient.HEIGHT/2 + 100,
				300,
				200,
				containedLiquids.isEmpty() ? getCotainerTitle() : getCotainerTitle() + " of " + getTitle(containedLiquids, getTotalAmount()),
				true,
				300,
				200
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * @return The description of the container
	 */
	protected abstract String getContainerDescription();


	/**
	 * @return The title of the container
	 */
	protected abstract String getCotainerTitle();


	/**
	 * Return the total amount of fluid in this container
	 */
	protected float getTotalAmount() {
		return (float) containedLiquids.values().stream().mapToDouble(f -> {return f;}).sum();
	}

	
	@Override
	public boolean sameAs(Item other) {
		if (other instanceof LiquidContainer) {
			if (containedLiquids.size() != ((LiquidContainer)other).containedLiquids.size()) {
				return false;
			}
			
			for (Entry<Class<? extends Liquid>, Float> entry : containedLiquids.entrySet()) {
				Float otherAmount = ((LiquidContainer) other).containedLiquids.get(entry.getKey());
				if (otherAmount != null && otherAmount.equals(entry.getValue())) {
					continue;
				} else {
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	
	@Override
	public abstract LiquidContainer clone();
}