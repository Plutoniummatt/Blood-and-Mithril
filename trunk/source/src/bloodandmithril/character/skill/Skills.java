package bloodandmithril.character.skill;

import java.io.Serializable;

/**
 * Class representing a skill set
 *
 * @author Matt
 */
public class Skills implements Serializable {
	private static final long serialVersionUID = 2154775669521547748L;

	public static final int MAX_LEVEL = 100;

	/** Represents skill levels */
	private int trading = 0;				// Better at trading, trade less for more
	private int observation = 0;			// Better at identifying the status of an individual
	private int fitness = 0;				// Can run for longer

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

	public int getFitness() {
		return fitness;
	}

	public void setFitness(int fitness) {
		this.fitness = fitness;
	}
}