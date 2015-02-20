package bloodandmithril.character.skill.skills;

import bloodandmithril.character.skill.Skill;
import bloodandmithril.core.Copyright;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Masonry extends Skill {
	private static final long serialVersionUID = -8405186147243633432L;

	public Masonry(int level) {
		super(
			"Masonry",
			"Masonry allows individuals to work with everything stone.",
			level
		);
	}
}
