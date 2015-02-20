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
 * Class representing a skill set
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Skills implements Serializable {
	private static final long serialVersionUID = 2154775669521547748L;

	/** Represents skill levels */
	private Trading trading = new Trading(0);
	private Smithing smithing = new Smithing(0);
	private Carpentry carpentry = new Carpentry(0);
	private Masonry masonry = new Masonry(0);
	private Cooking cooking = new Cooking(0);
	private Glassworking glassworking = new Glassworking(0);

	/**
	 * Constructor
	 */
	public Skills() {
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

	public Collection<Skill> getAllSkills() {
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