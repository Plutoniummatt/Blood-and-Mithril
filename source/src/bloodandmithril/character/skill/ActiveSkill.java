package bloodandmithril.character.skill;

import bloodandmithril.core.Copyright;

/**
 * Represents an active skill
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class ActiveSkill extends Skill {

	/**
	 * Constructor
	 */
	protected ActiveSkill(String name) {
		super(name);
	}


	/**
	 * Executes this skill
	 */
	public abstract void execute();
}