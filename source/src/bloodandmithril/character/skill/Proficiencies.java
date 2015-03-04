package bloodandmithril.character.skill;

import java.io.Serializable;
import java.util.Collection;

import bloodandmithril.character.skill.skills.Carpentry;
import bloodandmithril.character.skill.skills.Cooking;
import bloodandmithril.character.skill.skills.Glassworking;
import bloodandmithril.character.skill.skills.Masonry;
import bloodandmithril.character.skill.skills.Smithing;
import bloodandmithril.character.skill.skills.Trading;
import bloodandmithril.core.Copyright;

import com.google.common.collect.Lists;

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
	private Masonry masonry = new Masonry(0);
	private Cooking cooking = new Cooking(0);
	private Glassworking glassworking = new Glassworking(0);

	/**
	 * Constructor
	 */
	public Proficiencies() {
	}

	public Trading getTrading() {
		return trading;
	}

	public Smithing getSmithing() {
		return smithing;
	}

	public Carpentry getCarpentry() {
		return carpentry;
	}

	public Masonry getMasonry() {
		return masonry;
	}

	public Cooking getCookking() {
		return cooking;
	}

	public Glassworking getGlassworking() {
		return glassworking;
	}

	public Collection<Proficiency> getAllProficiencies() {
		return Lists.newArrayList(
			trading,
			smithing,
			carpentry,
			masonry,
			cooking,
			glassworking
		);
	}
}