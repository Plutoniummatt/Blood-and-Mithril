package bloodandmithril.character.skill;

import bloodandmithril.core.Copyright;

/**
 * Represents an active or passive skill.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Skill {

	private final String name;

	/**
	 * Constructor
	 */
	protected Skill(String name) {
		this.name = name;
	}


	/**
	 * @return the name of this skill
	 */
	public String getName() {
		return name;
	}
}