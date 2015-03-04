package bloodandmithril.character.skill.skills;

import bloodandmithril.character.skill.Proficiency;
import bloodandmithril.core.Copyright;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Cooking extends Proficiency {
	private static final long serialVersionUID = -8405186147243633432L;

	public Cooking(int level) {
		super(
			"Cooking",
			"Cooking allows individuals to work with everything food.",
			level
		);
	}
}
