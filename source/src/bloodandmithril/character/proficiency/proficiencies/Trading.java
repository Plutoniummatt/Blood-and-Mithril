package bloodandmithril.character.proficiency.proficiencies;

import com.google.inject.Inject;

import bloodandmithril.character.proficiency.Proficiency;
import bloodandmithril.core.Copyright;

/**
 * The trading skill
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class Trading extends Proficiency {
	private static final long serialVersionUID = -1129808804239818810L;

	/**
	 * Constructor
	 */
	@Inject
	public Trading() {
		super(
			"Trading",
			"The proficiency at trading with others, the better one's trading skill, the more one can gain for less... materialistically speaking.",
			0
		);
	}
}
