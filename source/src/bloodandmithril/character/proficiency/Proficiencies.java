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

/**
 * Class representing the proficiencies of an individual
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Proficiencies implements Serializable {
	private static final long serialVersionUID = 2154775669521547748L;

	/** Represents proficiencies */
	private Trading trading = new Trading(0);
	private Smithing smithing = new Smithing(0);
	private Carpentry carpentry = new Carpentry(0);
	private Cooking cooking = new Cooking(0);

	private Map<Class<? extends Proficiency>, Proficiency> map = Maps.newHashMap();

	/**
	 * Constructor
	 */
	public Proficiencies() {
		getAllProficiencies().stream().forEach(proficiency -> {
			map.put(proficiency.getClass(), proficiency);
		});
	}

	public Proficiency getProficiency(Class<? extends Proficiency> clazz) {
		return map.get(clazz);
	}

	public Collection<Proficiency> getAllProficiencies() {
		return Lists.newArrayList(
			trading,
			smithing,
			carpentry,
			cooking
		);
	}
}