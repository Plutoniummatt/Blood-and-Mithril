package bloodandmithril.character.faction;


/**
 * Class representing a faction
 *
 * @author Matt
 */
public class Faction {

	public final String name;
	public final int factionId;
	public String controlPassword = "11";
	public static final int NPC = 0;

	/**
	 * Constructor
	 */
	public Faction(String name, int factionId) {
		this.name = name;
		this.factionId = factionId;
	}


	public synchronized void changeControlPassword(String controlPassword) {
		this.controlPassword = controlPassword;
	}
}