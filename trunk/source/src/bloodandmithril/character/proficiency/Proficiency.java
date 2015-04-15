package bloodandmithril.character.proficiency;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.TreeMap;

import bloodandmithril.core.Copyright;

import com.google.common.collect.Maps;

/**
 * Represents a skill
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Proficiency implements Serializable {
	private static final long serialVersionUID = 2602006776166912149L;
	public static final int MAX_SKILL_LEVEL = 100;

	private final String name;
	private final String description;
	private int level;
	private float experience;

	private static TreeMap<Integer, Integer> experienceRequiredToLevelUp = Maps.newTreeMap();

	static {
		for (int i = 0; i <= MAX_SKILL_LEVEL; i++) {
			experienceRequiredToLevelUp.put(i, (int) round(sqrt(i + 1) * 1000));
		}

		experienceRequiredToLevelUp.put(MAX_SKILL_LEVEL, Integer.MAX_VALUE);
	}

	/**
	 * Constructor
	 */
	protected Proficiency(String name, String description, int level) {
		this.name = name;
		this.description = description;
		this.level = level;

		recalculateExperience();
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
	 * Increases experiences, returning true if leveling up
	 */
	public boolean increaseExperience(float toIncrease) {
		this.experience = this.experience + toIncrease;
		return recalculateLevel();
	}


	public float getExperience() {
		return experience;
	}


	private boolean recalculateLevel() {
		float e = this.experience;
		for (Entry<Integer, Integer> entry : experienceRequiredToLevelUp.entrySet()) {
			if (e < entry.getValue()) {
				boolean levelled = false;
				if (this.level < entry.getKey()) {
					levelled = true;
				}
				this.level = entry.getKey();
				return levelled;
			} else {
				e -= entry.getValue();
			}
		}

		return false;
	}


	/**
	 * @return The skill level
	 */
	public int getLevel() {
		return level;
	}


	private void recalculateExperience() {
		int minExperience = 0;
		for (Entry<Integer, Integer> entry : experienceRequiredToLevelUp.entrySet()) {
			if (this.level > entry.getKey()) {
				minExperience += entry.getValue();
			} else {
				break;
			}
		}

		if (this.experience <= minExperience) {
			this.experience = minExperience;
		} else {
			this.experience = minExperience + experienceRequiredToLevelUp.get(level) - 1;
		}
	}


	public void levelUp() {
		this.level = level + 1 > MAX_SKILL_LEVEL ? MAX_SKILL_LEVEL : level + 1;
		recalculateExperience();
	}


	public void levelDown() {
		this.level = level - 1 < 0 ? 0 : level - 1;
		recalculateExperience();
	}


	/**
	 * @return the ratio of a skill level to that of the max skill level
	 */
	public static float getRatioToMax(int skillLevel) {
		return skillLevel / (float) Proficiency.MAX_SKILL_LEVEL;
	}
}
