package bloodandmithril.item.material.container;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.liquid.Liquid;
import bloodandmithril.item.material.liquid.Liquid.Empty;

/**
 * Bottle made from Glass
 *
 * @author Copyright (c) CHP Consulting Ltd. 2014
 */
public abstract class Bottle extends Item {
	private static final long serialVersionUID = 5895479054869624858L;
	
	protected Class<? extends Liquid> containedLiquid;
	protected float amount;
	protected final float maxAmount;

	/**
	 * Constructor
	 */
	protected Bottle(float mass, float amount, float maxAmount, Class<? extends Liquid> liquid, long value) {
		super(mass, false, value);
		this.amount = amount;
		this.maxAmount = maxAmount;
		this.containedLiquid = liquid;
	}


	public synchronized float decreaseAmount(float amount) {
		float toReturn;
		if (this.amount - amount <= 0f) {
			toReturn = this.amount;
			this.amount = 0f;
			this.containedLiquid = Empty.class;
		} else {
			toReturn = amount;
			this.amount = this.amount - amount;
		}
		return toReturn;
	}


	public synchronized void increaseAmount(float amount) {
		if (this.amount + amount >= maxAmount) {
			this.amount = maxAmount;
		} else {
			this.amount = this.amount + amount;
		}
	}


	public void drink(float amount, Individual affected) {
		try {
			containedLiquid.newInstance().drink(decreaseAmount(amount), affected);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public abstract Bottle clone();

	@Override
	public boolean sameAs(Item other) {
		if (other instanceof Bottle) {
			return this.containedLiquid.equals(((Bottle)other).containedLiquid) && this.amount == ((Bottle)other).amount && this.maxAmount == ((Bottle)other).maxAmount;
		}
		return false;
	}
}