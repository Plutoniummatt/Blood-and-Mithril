package bloodandmithril.character.faction;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.world.Domain;


/**
 * Class representing a faction
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Faction implements Serializable {
	private static final long serialVersionUID = -1391877335358331635L;

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


	public static Faction getNature() {
		Wrapper<Faction> nature = new Wrapper<Faction>(null);
		Domain.getFactions().values().forEach(f -> {
			if (f.name.equals("Nature")) {
				nature.t = f;
			}
		});

		return nature.t;
	}
}