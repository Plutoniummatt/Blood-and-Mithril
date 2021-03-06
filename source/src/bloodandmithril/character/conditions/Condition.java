package bloodandmithril.character.conditions;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.Sets;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * A condition that applies to a character
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Condition implements Serializable {
	private static final long serialVersionUID = -1125485475556985426L;

	@SuppressWarnings("unchecked")
	public static Set<Class<? extends Condition>> getAllConditions() {
		return Sets.newHashSet(
			Bleeding.class,
			Burning.class,
			Exhaustion.class,
			Hunger.class,
			Poison.class,
			Thirst.class
		);
	}

	/** Affect the character suffering from this condition */
	public abstract void affect(Individual affected, float delta);

	/** Client-side specific effects */
	public abstract void clientSideEffects(Individual affected, float delta);

	/** Infect another character */
	public abstract void infect(Individual infected, float delta);

	/** Whether this condition can be removed */
	public abstract boolean isExpired();

	/** Called when expired */
	public abstract void uponExpiry();

	/** Called when the condition is added to an individual who already has this condition */
	public abstract void stack(Condition condition);

	/** Whether this condition is detrimental to the individual */
	public abstract boolean isNegative();

	/** Gets the help text describing this condition */
	public abstract String getHelpText();

	/** The severity of this condition */
	public abstract String getName();
}