package bloodandmithril.character.skill.skills;

import bloodandmithril.character.skill.Skill;
import bloodandmithril.core.Copyright;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Smithing extends Skill {
	private static final long serialVersionUID = 8724126639508339386L;

	public Smithing(int level) {
		super(
			"Smithing",
			"Smithing is the skill that allows individuals to forge and shape objects made from metal.",
			level
		);
	}
}
