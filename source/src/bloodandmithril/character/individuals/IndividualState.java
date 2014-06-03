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
	public IndividualState(float maxHealth, float healthRegen, float staminaRegen, float maxMana, float manaRegen) {
		this.health = maxHealth;
		this.maxHealth = maxHealth;
		this.healthRegen = healthRegen;
		this.normalHealthRegen = healthRegen;
		this.staminaRegen = staminaRegen;
		this.normalStaminaRegen = staminaRegen;
		this.mana = maxMana;
		this.maxMana = maxMana;
		this.manaRegen = manaRegen;
		this.normalManaRegen = manaRegen;
		this.hunger = 1f;
		this.thirst = 1f;
		this.stamina = 1f;
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