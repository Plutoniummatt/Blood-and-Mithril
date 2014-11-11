package bloodandmithril.character.skill;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

/**
 * Class representing a skill set
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Skills implements Serializable {
	private static final long serialVersionUID = 2154775669521547748L;

	public static final int MAX_LEVEL = 100;

	/** Represents skill levels */
	private int trading = 0;				// Better at trading, trade less for more
	private int observation = 0;			// Better at identifying the status of an individual
	private int smithing = 0;				// Blacksmithing
	private int carpentry = 0;				// Carpentry
	private int masonry = 0;				// Masonry
	private int glassworking = 0;			// Glassworking
	private int cookking = 0;				// Cooking

	/**
	 * Constructor
	 */
	public Skills() {}

	public synchronized int getTrading() {
		return trading;
	}

	public synchronized void setTrading(int trading) {
		this.trading = trading;
	}

	public synchronized int getObservation() {
		return observation;
	}

	public synchronized void setObservation(int observation) {
		this.observation = observation;
	}

	public int getSmithing() {
		return smithing;
	}

	public void setSmithing(int smithing) {
		this.smithing = smithing;
	}

	public int getCarpentry() {
		return carpentry;
	}

	public void setCarpentry(int carpentry) {
		this.carpentry = carpentry;
	}

	/**
	 * @return the ratio of a skill level to that of the max skill level
	 */
	public static float getRatioToMax(int skillLevel) {
		return (float) skillLevel / (float) MAX_LEVEL;
	}

	public int getMasonry() {
		return masonry;
	}

	public void setMasonry(int masonry) {
		this.masonry = masonry;
	}

	public int getGlassworking() {
		return glassworking;
	}

	public void setGlassworking(int glassworking) {
		this.glassworking = glassworking;
	}

	public int getCookking() {
		return cookking;
	}

	public void setCookking(int cookking) {
		this.cookking = cookking;
	}
}