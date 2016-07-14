package bloodandmithril.character.proficiency;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import bloodandmithril.character.proficiency.proficiencies.Carpentry;
import bloodandmithril.character.proficiency.proficiencies.Cooking;
import bloodandmithril.character.proficiency.proficiencies.Smithing;
import bloodandmithril.character.proficiency.proficiencies.Trading;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;

/**
 * Class representing the proficiencies of an individual
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Proficiencies implements Serializable {
	private static final long serialVersionUID = 2154775669521547748L;

	@SuppressWarnings("unchecked")
	public static Collection<Class<? extends Proficiency>> allProficiencies =
	Lists.newArrayList(
		Trading.class,
		Smithing.class,
		Carpentry.class,
		Cooking.class
	);

	/** Represents proficiencies */
	private Map<Class<? extends Proficiency>, Proficiency> map = Maps.newHashMap();

	/**
	 * Constructor
	 */
	public Proficiencies() {
		allProficiencies.stream().forEach(proficiency -> {
			map.put(proficiency, Wiring.injector().getInstance(proficiency));
		});
	}


	public Proficiency getProficiency(final Class<? extends Proficiency> clazz) {
		return map.get(clazz);
	}


	public Collection<Proficiency> getAllProficiencies() {
		return map.values();
	}
}