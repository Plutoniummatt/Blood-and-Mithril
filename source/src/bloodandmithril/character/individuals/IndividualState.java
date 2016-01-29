package bloodandmithril.character.individuals;

import static com.google.common.collect.Sets.newHashSet;

import java.io.Serializable;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.conditions.Condition;
import bloodandmithril.core.Copyright;

/**
 * Encapsulates the (constantly changing) state of an {@link Individual}.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class IndividualState implements Serializable {
	private static final long serialVersionUID = 3678630824613212498L;

	public float health, maxHealth, healthRegen, stamina, staminaRegen, hunger, thirst, mana, maxMana, manaRegen;
	public final float defaultHealthRegen, defaultStaminaRegen, defaultManaRegen;
	public Vector2 position;
	public Vector2 velocity;
	public Vector2 acceleration;
	public Set<Condition> currentConditions = newHashSet();

	/**
	 * Constructor
	 */
	private IndividualState(float maxHealth, float healthRegen, float staminaRegen, float maxMana, float manaRegen) {
		this.health = maxHealth;
		this.maxHealth = maxHealth;
		this.healthRegen = healthRegen;

		this.staminaRegen = staminaRegen;

		this.mana = maxMana;
		this.maxMana = maxMana;
		this.manaRegen = manaRegen;

		this.defaultHealthRegen = healthRegen;
		this.defaultStaminaRegen = staminaRegen;
		this.defaultManaRegen = manaRegen;

		this.hunger = 1f;
		this.thirst = 1f;
		this.stamina = 1f;
	}

	/**
	 * Resets the regen values
	 */
	public void reset() {
		this.healthRegen = defaultHealthRegen;
		this.staminaRegen = defaultStaminaRegen;
		this.manaRegen = defaultManaRegen;
	}


	public static class IndividualStateBuilder {
		private float maxHealth;
		private float healthRegen;
		private float staminaRegen;
		private float maxMana;
		private float manaRegen;
		public IndividualStateBuilder withMaxHealth(float maxHealth) {
			this.maxHealth = maxHealth;
			return this;
		}
		public IndividualStateBuilder withHealthRegen(float healthRegen) {
			this.healthRegen = healthRegen;
			return this;
		}
		public IndividualStateBuilder withStaminaRegen(float staminaRegen) {
			this.staminaRegen = staminaRegen;
			return this;
		}
		public IndividualStateBuilder withMaxMana(float maxMana) {
			this.maxMana = maxMana;
			return this;
		}
		public IndividualStateBuilder withManaRegen(float manaRegen) {
			this.manaRegen = manaRegen;
			return this;
		}
		public IndividualState build() {
			return new IndividualState(maxHealth, healthRegen, staminaRegen, maxMana, manaRegen);
		}
	}
}