package bloodandmithril.item.items.container;

import static bloodandmithril.character.ai.perception.Visible.getVisible;
import static bloodandmithril.util.Util.round2dp;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.prop.furniture.LiquidContainerProp.PropLiquidContainerItem;
import bloodandmithril.ui.components.window.ItemInfoWindow;
import bloodandmithril.ui.components.window.Window;

/**
 * Contains {@link Liquid}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public abstract class LiquidContainerItem extends Item {
	private static final long serialVersionUID = 5895479054869624858L;

	protected Map<Class<? extends Liquid>, Float> containedLiquids;
	protected final float maxAmount;

	/**
	 * Constructor
	 */
	protected LiquidContainerItem(final float mass, final int volume, final float maxAmount, final Map<Class<? extends Liquid>, Float> containedLiquids, final long value) {
		super(mass, volume, false, value);
		this.maxAmount = maxAmount;
		this.containedLiquids = Maps.newHashMap(containedLiquids);
	}


	public void drinkFrom(final float amount, final Individual affected) {
		final float fraction = amount/getTotalAmount();

		try {
			SoundService.play(SoundService.swallow, affected.getState().position, true, getVisible(affected));
			for (final Entry<Class<? extends Liquid>, Float> entry : Maps.newHashMap(containedLiquids).entrySet()) {
				if (fraction >= 1f) {
					entry.getKey().newInstance().drink(entry.getValue(), affected);
					containedLiquids.remove(entry.getKey());
				} else {
					entry.getKey().newInstance().drink(entry.getValue() * fraction, affected);
					containedLiquids.put(entry.getKey(), round2dp(entry.getValue() * (1f - fraction)));
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Subtract an amount from this {@link LiquidContainerItem}, returning a Map of the fluids subtracted.
	 */
	public Map<Class<? extends Liquid>, Float> subtract(final float amount) {
		final float fraction = amount/getTotalAmount();
		final Map<Class<? extends Liquid>, Float> subtracted = Maps.newHashMap();

		try {
			for (final Entry<Class<? extends Liquid>, Float> entry : Maps.newHashMap(containedLiquids).entrySet()) {
				if (fraction >= 1f) {
					containedLiquids.remove(entry.getKey());
					subtracted.put(entry.getKey(), round2dp(entry.getValue()));
				} else {
					subtracted.put(entry.getKey(), round2dp(entry.getValue() * fraction));
					containedLiquids.put(entry.getKey(), round2dp(entry.getValue() * (1f - fraction)));
				}
			}

			for (final Entry<Class<? extends Liquid>, Float> entry : Maps.newHashMap(subtracted).entrySet()) {
				if (entry.getValue() < 0.01f) {
					subtracted.remove(entry.getKey());
				}
			}

			return subtracted;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	protected Item internalCopy() {
		final LiquidContainerItem container = copyContainer();

		final Map<Class<? extends Liquid>, Float> copy = Maps.newHashMap();

		for (final Entry<Class<? extends Liquid>, Float> entry : containedLiquids.entrySet()) {
			copy.put(entry.getKey(), entry.getValue().floatValue());
		}

		container.containedLiquids = copy;

		return container;
	}


	/** Copies the container itself, not the content */
	protected abstract LiquidContainerItem copyContainer();


	@Override
	public Window getInfoWindow() {
		return new ItemInfoWindow(
			this,
			400,
			450
		);
	}


	/**
	 * @return the description of the item type
	 */
	@Override
	public ItemCategory getType() {
		return ItemCategory.CONTAINER;
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
	 * @return the remaining capacity of this {@link LiquidContainerItem}
	 */
	public float getRemainingCapacity() {
		return maxAmount - getTotalAmount();
	}


	/**
	 * Adds a map of fluid-amounts to this liquid container, returning the remainder.
	 */
	public Map<Class<? extends Liquid>, Float> add(final Map<Class<? extends Liquid>, Float> toAdd) {
		final float amountToAdd = (float) toAdd.entrySet().stream().mapToDouble(entry -> {
			return entry.getValue();
		}).sum();

		if (amountToAdd <= getRemainingCapacity()) {
			for (final Entry<Class<? extends Liquid>, Float> entryToAdd : toAdd.entrySet()) {
				if (containedLiquids.containsKey(entryToAdd.getKey())) {
					containedLiquids.put(entryToAdd.getKey(), round2dp(containedLiquids.get(entryToAdd.getKey()) + entryToAdd.getValue()));
				} else {
					containedLiquids.put(entryToAdd.getKey(), round2dp(entryToAdd.getValue()));
				}
			}

			return Maps.newHashMap();
		} else {
			final float fractionOfAmountToAdd = getRemainingCapacity() / amountToAdd;

			for (final Entry<Class<? extends Liquid>, Float> entryToAdd : toAdd.entrySet()) {
				if (containedLiquids.containsKey(entryToAdd.getKey())) {
					containedLiquids.put(entryToAdd.getKey(), round2dp(containedLiquids.get(entryToAdd.getKey()) + fractionOfAmountToAdd * entryToAdd.getValue()));
				} else {
					containedLiquids.put(entryToAdd.getKey(), round2dp(entryToAdd.getValue() * fractionOfAmountToAdd));
				}
			}

			final Map<Class<? extends Liquid>, Float> transformValues = Maps.transformValues(toAdd, value -> {
				return round2dp(value * (1-fractionOfAmountToAdd));
			});

			for (final Entry<Class<? extends Liquid>, Float> entry : Maps.newHashMap(transformValues).entrySet()) {
				if (entry.getValue() < 0.01f) {
					transformValues.remove(entry.getKey());
				}
			}

			return transformValues;
		}
	}


	@Override
	protected boolean internalSameAs(final Item other) {
		if (other instanceof LiquidContainerItem) {
			if (!other.getClass().equals(this.getClass())) {
				return false;
			}

			if (containedLiquids.size() != ((LiquidContainerItem)other).containedLiquids.size()) {
				return false;
			}

			for (final Entry<Class<? extends Liquid>, Float> entry : containedLiquids.entrySet()) {
				final Float otherAmount = ((LiquidContainerItem) other).containedLiquids.get(entry.getKey());
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


	public static void transfer(final Individual individual, final LiquidContainerItem from, final LiquidContainerItem to, final float amount) {
		individual.takeItem(from);
		individual.takeItem(to);
		final LiquidContainerItem newContainer = from.clone();
		final Map<Class<? extends Liquid>, Float> subtracted = newContainer.subtract(amount);
		final Map<Class<? extends Liquid>, Float> remainder = to.add(subtracted);
		if (!remainder.isEmpty()) {
			newContainer.add(remainder);
		}
		individual.giveItem(newContainer);
		individual.giveItem(to);
	}


	public static void transfer(final Individual individual, final PropLiquidContainerItem from, final LiquidContainerItem to, final float amount) {
		individual.takeItem(to);
		if (amount >= 0f) {
			final Map<Class<? extends Liquid>, Float> subtracted = from.subtract(amount);
			final Map<Class<? extends Liquid>, Float> remainder = to.add(subtracted);
			if (!remainder.isEmpty()) {
				from.add(remainder);
			}
		} else {
			final Map<Class<? extends Liquid>, Float> subtracted = to.subtract(amount);
			final Map<Class<? extends Liquid>, Float> remainder = from.add(subtracted);
			if (!remainder.isEmpty()) {
				to.add(remainder);
			}
		}
		individual.giveItem(to);
	}


	@Override
	public abstract LiquidContainerItem clone();

	@Override
	public float getUprightAngle() {
		return 90f;
	}
}