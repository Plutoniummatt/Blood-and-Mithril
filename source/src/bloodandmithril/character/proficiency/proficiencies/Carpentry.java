package bloodandmithril.character.proficiency.proficiencies;

import bloodandmithril.character.proficiency.Proficiency;
import bloodandmithril.core.Copyright;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Carpentry extends Proficiency {
	private static final long serialVersionUID = -8405186147243633432L;

	public Carpentry(int level) {
		super(
			"Carpentry",
			"Carpentry allows individuals to work with everything wooden.",
			level
		);
	}
}
