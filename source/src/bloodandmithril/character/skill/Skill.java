package bloodandmithril.character.skill;

import java.io.Serializable;

/**
 * Represents a skill
 *
 * @author Matt
 */
public abstract class Skill implements Serializable {
	private static final long serialVersionUID = 2602006776166912149L;
	public static final int MAX_SKILL_LEVEL = 100;

	private final String name;
	private final String description;
	private int level;

	/**
	 * Constructor
	 */
	protected Skill(String name, String description, int level) {
		this.name = name;
		this.description = description;
		this.level = level;
	}


	/**
	 * @return The name of this skill
	 */
	public String getName() {
		return name;
	}


	/**
	 * @return The description of this skill
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * @return The skill level
	 */
	public int getLevel() {
		return level;
	}


	/**
	 * @param level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}


	/**
	 * @return the ratio of a skill level to that of the max skill level
	 */
	public static float getRatioToMax(int skillLevel) {
		return skillLevel / (float) Skill.MAX_SKILL_LEVEL;
	}
}
