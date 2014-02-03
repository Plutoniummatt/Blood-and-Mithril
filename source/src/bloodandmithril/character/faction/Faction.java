package bloodandmithril.character.faction;


/**
 * Class representing a faction
 *
 * @author Matt
 */
public class Faction {

	public final String name;
	public final int factionId;
	public final boolean controllable;
	public String controlPassword = "";
	public static final int NPC = 0;

	/**
	 * Constructor
	 */
	public Faction(String name, int factionId, boolean controllable) {
		this.name = name;
		this.factionId = factionId;
		this.controllable = controllable;
	}


	public synchronized void changeControlPassword(String controlPassword) {
		this.controlPassword = controlPassword;
	}
}