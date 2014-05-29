package bloodandmithril.character.conditions;

import java.io.Serializable;

import bloodandmithril.character.individuals.Individual;

/**
 * A condition that applies to a character
 *
 * @author Matt
 */
public abstract class Condition implements Serializable {
	private static final long serialVersionUID = -1125485475556985426L;

	/** Affect the character suffering from this condition */
	public abstract void affect(Individual affected, float delta);

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