package bloodandmithril.character.skill;

import java.io.Serializable;

/**
 * Represents a skill
 *
 * @author Matt
 */
public abstract class Proficiency implements Serializable {
	private static final long serialVersionUID = 2602006776166912149L;
	public static final int MAX_SKILL_LEVEL = 100;

	private final String name;
	private final String description;
	private int level;

	/**
	 * Constructor
	 */
	protected Proficiency(String name, String description, int level) {
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
	
	
	public void levelUp() {
		this.level = level + 1 > MAX_SKILL_LEVEL ? MAX_SKILL_LEVEL : level + 1;
	}
	
	
	public void levelDown() {
		this.level = level - 1 < 0 ? 0 : level - 1;
	}


	/**
	 * @return the ratio of a skill level to that of the max skill level
	 */
	public static float getRatioToMax(int skillLevel) {
		return skillLevel / (float) Proficiency.MAX_SKILL_LEVEL;
	}
}
