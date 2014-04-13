package bloodandmithril.item.material.container;

import static bloodandmithril.item.material.liquid.LiquidMixtureAnalyzer.getTitle;
import static bloodandmithril.util.Util.round2dp;

import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.liquid.Liquid;
import bloodandmithril.item.material.liquid.LiquidMixtureAnalyzer;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Maps;

/**
 * Contains {@link Liquid}s
 *
 * @author Matt
 */
public abstract class LiquidContainer extends Item {
	private static final long serialVersionUID = 5895479054869624858L;

	protected Map<Class<? extends Liquid>, Float> containedLiquids;
	protected final float maxAmount;

	private Map<Class<? extends Liquid>, Float> transformValues;

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
					containedLiquids.put(entry.getKey(), round2dp(entry.getValue() * (1f - fraction)));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Subtract an amount from this {@link LiquidContainer}, returning a Map of the fluids subtracted.
	 */
	public Map<Class<? extends Liquid>, Float> subtract(float amount) {
		float fraction = amount/getTotalAmount();
		Map<Class<? extends Liquid>, Float> subtracted = Maps.newHashMap();

		try {
			for (Entry<Class<? extends Liquid>, Float> entry : Maps.newHashMap(containedLiquids).entrySet()) {
				if (fraction >= 1f) {
					containedLiquids.remove(entry.getKey());
					subtracted.put(entry.getKey(), round2dp(entry.getValue()));
				} else {
					subtracted.put(entry.getKey(), round2dp(entry.getValue() * fraction));
					containedLiquids.put(entry.getKey(), round2dp(entry.getValue() * (1f - fraction)));
				}
			}

			for (Entry<Class<? extends Liquid>, Float> entry : Maps.newHashMap(subtracted).entrySet()) {
				if (entry.getValue() < 0.01f) {
					subtracted.remove(entry);
				}
			}

			return subtracted;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public Window getInfoWindow() {
		try {
			return new MessageWindow(
				containedLiquids.isEmpty() ? getDescription() : getDescription() + " containing " + LiquidMixtureAnalyzer.getDescription(containedLiquids, getTotalAmount()),
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
	 * @return The title of the container
	 */
	protected abstract String getCotainerTitle();


	/**
	 * Return the total amount of fluid in this container
	 */
	protected float getTotalAmount() {
		return (float) containedLiquids.values().stream().mapToDouble(f -> {return f;}).sum();
	}


	public boolean isEmpty() {
		return getTotalAmount() == 0f;
	}


	/**
	 * @return the remaining capacity of this {@link LiquidContainer}
	 */
	public float getRemainingCapacity() {
		return maxAmount - getTotalAmount();
	}


	/**
	 * Adds a map of fluid-amounts to this liquid container, returning the remainder.
	 */
	public Map<Class<? extends Liquid>, Float> add(Map<Class<? extends Liquid>, Float> toAdd) {
		float amountToAdd = (float) toAdd.entrySet().stream().mapToDouble(entry -> {
			return entry.getValue();
		}).sum();

		if (amountToAdd <= getRemainingCapacity()) {
			for (Entry<Class<? extends Liquid>, Float> entryToAdd : toAdd.entrySet()) {
				if (containedLiquids.containsKey(entryToAdd.getKey())) {
					containedLiquids.put(entryToAdd.getKey(), round2dp(containedLiquids.get(entryToAdd.getKey()) + entryToAdd.getValue()));
				} else {
					containedLiquids.put(entryToAdd.getKey(), round2dp(entryToAdd.getValue()));
				}
			}

			return Maps.newHashMap();
		} else {
			float fractionOfAmountToAdd = getRemainingCapacity() / amountToAdd;

			for (Entry<Class<? extends Liquid>, Float> entryToAdd : toAdd.entrySet()) {
				if (containedLiquids.containsKey(entryToAdd.getKey())) {
					containedLiquids.put(entryToAdd.getKey(), round2dp(containedLiquids.get(entryToAdd.getKey()) + fractionOfAmountToAdd * entryToAdd.getValue()));
				} else {
					containedLiquids.put(entryToAdd.getKey(), round2dp(entryToAdd.getValue() * fractionOfAmountToAdd));
				}
			}

			transformValues = Maps.transformValues(toAdd, value -> {
				return round2dp(value * (1-fractionOfAmountToAdd));
			});

			for (Entry<Class<? extends Liquid>, Float> entry : Maps.newHashMap(transformValues).entrySet()) {
				if (entry.getValue() < 0.01f) {
					transformValues.remove(entry.getKey());
				}
			}

			return transformValues;
		}
	}


	@Override
	public boolean sameAs(Item other) {
		if (other instanceof LiquidContainer) {
			if (!other.getClass().equals(this.getClass())) {
				return false;
			}

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