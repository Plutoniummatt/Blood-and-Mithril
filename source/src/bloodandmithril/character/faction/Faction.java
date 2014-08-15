package bloodandmithril.character.faction;

import bloodandmithril.core.Copyright;


/**
 * Class representing a faction
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Faction {

	public final String name;
	public final int factionId;
	public final boolean controllable;
	public String controlPassword = "";
	public String description;
	public static final int NPC = 0;

	/**
	 * Constructor
	 */
	public Faction(String name, int factionId, boolean controllable, String description) {
		this.name = name;
		this.factionId = factionId;
		this.controllable = controllable;
		this.description = description;
	}


	public synchronized void changeControlPassword(String controlPassword) {
		this.controlPassword = controlPassword;
	}
}