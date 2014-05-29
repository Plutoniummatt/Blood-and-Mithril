package bloodandmithril.character.individuals;

import static com.google.common.collect.Sets.newHashSet;

import java.io.Serializable;
import java.util.Set;

import bloodandmithril.character.conditions.Condition;

import com.badlogic.gdx.math.Vector2;

/**
 * Encapsulates the (constantly changing) state of an {@link Individual}.
 *
 * @author Matt
 */
public class IndividualState implements Serializable {
	private static final long serialVersionUID = 3678630824613212498L;

	public float health, maxHealth, healthRegen, normalHealthRegen, stamina, staminaRegen, normalStaminaRegen, hunger, thirst, mana, maxMana, manaRegen, normalManaRegen;
	public Vector2 position;
	public Vector2 velocity;
	public Vector2 acceleration;
	public Set<Condition> currentConditions = newHashSet();

	/**
	 * Constructor
	 */
	public IndividualState(float health, float maxHealth, float healthRegen, float stamina, float staminaRegen, float hunger, float thirst, float mana, float maxMana, float manaRegen) {
		this.health = health;
		this.maxHealth = maxHealth;
		this.healthRegen = healthRegen;
		this.normalHealthRegen = healthRegen;
		this.stamina = stamina;
		this.staminaRegen = staminaRegen;
		this.normalStaminaRegen = staminaRegen;
		this.hunger = hunger;
		this.thirst = thirst;
		this.mana = mana;
		this.maxMana = maxMana;
		this.manaRegen = manaRegen;
		this.normalManaRegen = manaRegen;
	}

	/**
	 * Resets the regen values
	 */
	public void reset() {
		this.healthRegen = normalHealthRegen;
		this.staminaRegen = normalStaminaRegen;
		this.manaRegen = normalManaRegen;
	}
}