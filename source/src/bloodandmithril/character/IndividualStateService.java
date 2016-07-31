package bloodandmithril.character;

import static com.google.common.collect.Sets.newHashSet;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.conditions.Condition;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;

/**
 * Contains operations that alters an {@link Individual}'s state
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class IndividualStateService {

	@Inject private GameClientStateTracker gameClientStateTracker;
	@Inject private UserInterface userInterface;

	/**
	 * Kills the given {@link Individual}
	 *
	 * @param individual
	 */
	public void kill(final Individual individual) {
		individual.setDead(true);
		if (ClientServerInterface.isClient()) {
			gameClientStateTracker.removeSelectedIndividual(individual);
		}
		individual.getEquipped().keySet().forEach(eq -> {
			individual.unequip((Equipable ) eq);
		});
		individual.getInventory().entrySet().forEach(entry -> {
			ContainerImpl.discard(individual, entry.getKey(), entry.getValue());
		});
		individual.getState().currentConditions.clear();
		individual.deselect(true, 0);
		individual.getSelectedByClient().clear();
		individual.internalKill();
		userInterface.refreshRefreshableWindows();
	}


	/**
	 * Damages an {@link Individual}
	 *
	 * @param individual
	 * @param amount
	 */
	public final void damage(final Individual individual, final float amount) {
		if (individual.getState().health == 0f) {
			return;
		}

		if (individual.getState().health - amount <= 0f) {
			individual.getState().health = 0f;
			kill(individual);
		} else {
			individual.getState().health = individual.getState().health - amount;
		}
	}


	/**
	 * Heals an {@link Individual}
	 *
	 * @param individual
	 * @param amount
	 */
	public synchronized void heal(final Individual individual, final float amount) {
		if (individual.getState().health + amount > individual.getState().maxHealth) {
			individual.getState().health = individual.getState().maxHealth;
		} else {
			individual.getState().health = individual.getState().health + amount;
		}
	}


	/**
	 * Add a {@link Condition} to this {@link Individual}, if there is already an existing {@link Condition}
	 * that is of the same class as the condition trying to be added, stack them by calling {@link Condition#stack(Condition)}
	 */
	public final synchronized void addCondition(final Individual individual, final Condition condition) {
		for (final Condition existing : newHashSet(individual.getState().currentConditions)) {
			if (condition.getClass().equals(existing.getClass())) {
				existing.stack(condition);
				return;
			}
		}
		individual.getState().currentConditions.add(condition);
	}


	/**
	 * Decreases thirst value of an {@link Individual}, can not go below 0
	 *
	 * @param individual
	 * @param amount
	 */
	public final synchronized void decreaseThirst(final Individual individual, final float amount) {
		if (individual.getState().thirst - amount <= 0f) {
			individual.getState().thirst = 0f;
		} else {
			individual.getState().thirst = individual.getState().thirst - amount;
		}
	}


	/**
	 * Increases thirst value of an {@link Individual}, can not go above 1
	 *
	 * @param individual
	 * @param amount
	 */
	public final synchronized void increaseThirst(final Individual individual, final float amount) {
		if (individual.getState().thirst + amount > 1f) {
			individual.getState().thirst = 1f;
		} else {
			individual.getState().thirst = individual.getState().thirst + amount;
		}
	}


	/**
	 * Increases hunger value of an {@link Individual}, can not go above 1
	 *
	 * @param individual
	 * @param amount
	 */
	public final synchronized void increaseHunger(final Individual individual, final float amount) {
		if (individual.getState().hunger + amount >= 1f) {
			individual.getState().hunger = 1f;
		} else {
			individual.getState().hunger = individual.getState().hunger + amount;
		}
	}


	/**
	 * Decreases hunger value of an {@link Individual}, can not go below 0
	 *
	 * @param individual
	 * @param amount
	 */
	public final synchronized void decreaseHunger(final Individual individual, final float amount) {
		if (individual.getState().hunger - amount <= 0f) {
			individual.getState().hunger = 0f;
		} else {
			individual.getState().hunger = individual.getState().hunger - amount;
		}
	}


	/**
	 * Increases mana value of an {@link Individual}, can not go above max mana
	 *
	 * @param individual
	 * @param amount
	 */
	public final synchronized void increaseMana(final Individual individual, final float amount) {
		if (individual.getState().mana + amount >= individual.getState().maxMana) {
			individual.getState().mana = individual.getState().maxMana;
		} else {
			individual.getState().mana = individual.getState().hunger + amount;
		}
	}


	/**
	 * Decreases mana value of an {@link Individual}, can not go below 0
	 *
	 * @param individual
	 * @param amount
	 */
	public final synchronized void decreaseMana(final Individual individual, final float amount) {
		if (individual.getState().mana - amount <= 0f) {
			individual.getState().mana = 0f;
		} else {
			individual.getState().mana = individual.getState().mana - amount;
		}
	}


	/**
	 * Increases stamina value of an {@link Individual}, can not go above 1
	 *
	 * @param individual
	 * @param amount
	 */
	public final synchronized void increaseStamina(final Individual individual, final float amount) {
		if (individual.getState().stamina + amount >= 1f) {
			individual.getState().stamina = 1f;
		} else {
			individual.getState().stamina = individual.getState().stamina + amount;
		}
	}


	/**
	 * Decreases stamina value of an {@link Individual}, can not go below 0
	 *
	 * @param individual
	 * @param amount
	 */
	public final synchronized void decreaseStamina(final Individual individual, final float amount) {
		if (individual.getState().stamina - amount <= 0f) {
			individual.getState().stamina = 0f;
		} else {
			individual.getState().stamina = individual.getState().stamina - amount;
		}
	}
}