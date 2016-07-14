package bloodandmithril.character.proficiency.proficiencies;

import com.google.inject.Inject;

import bloodandmithril.character.proficiency.Proficiency;
import bloodandmithril.core.Copyright;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Smithing extends Proficiency {
	private static final long serialVersionUID = 8724126639508339386L;

	@Inject
	public Smithing() {
		super(
			"Smithing",
			"Smithing is the skill that allows individuals to forge and shape objects made from metal.",
			0
		);
	}
}
