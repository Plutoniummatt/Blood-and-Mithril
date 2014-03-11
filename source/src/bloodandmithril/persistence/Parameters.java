package bloodandmithril.persistence;

import java.io.Serializable;
import java.util.Random;

import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Epoch;

import com.badlogic.gdx.math.Vector2;

/**
 * Parameters that require saving
 *
 * @author Matt
 */
public class Parameters implements Serializable {
	private static final long serialVersionUID = -4405235813990330113L;

	private int structureKeyCounter = 0;
	private int initialSeed = 0;
	private int individualIdCounter = 0;
	private int lightCounter = 0;
	private int propIdCounter = 0;
	private int worldIdCounter = 0;

	private Vector2 camera;
	private Epoch currentEpoch;

	/**
	 * Package-protected constructor, only {@link ParameterPersistenceService} has access
	 */
	Parameters() {
		super();
		Random random = new Random();
		initialSeed = random.nextInt();
	}


	/**
	 * Returns the current structure key counter
	 */
	public synchronized int getNextStructureKey() {
		structureKeyCounter++;
		return structureKeyCounter;
	}
	
	
	/**
	 * Returns the world id counter
	 */
	public synchronized int getNextWorldKey() {
		worldIdCounter++;
		return worldIdCounter;
	}


	/**
	 * Returns the next unique identifier to use for {@link IndividualIdentifier}
	 */
	public synchronized int getNextIndividualId() {
		individualIdCounter++;
		return individualIdCounter;
	}
	
	
	/**
	 * Returns the next unique identifier to use for {@link Light}
	 */
	public synchronized int getNextLightId() {
		lightCounter++;
		return lightCounter;
	}


	/**
	 * Returns the next unique identifier to use for {@link Prop}
	 */
	public synchronized int getNextPropId() {
		propIdCounter++;
		return propIdCounter;
	}


	/**
	 * Returns the current structure key counter
	 */
	public synchronized int getSeed() {
		return initialSeed;
	}


	/**
	 * Returns the saved camera position
	 */
	public Vector2 getSavedCameraPosition() {
		return camera;
	}


	/**
	 * Sets the saved camera position
	 */
	public void setSavedCameraPosition(Vector2 camera) {
		this.camera = camera;
	}


	/**
	 * @return the saved global {@link Epoch}
	 */
	public Epoch getCurrentEpoch() {
		return currentEpoch;
	}


	/**
	 * Sets the current global {@link Epoch}
	 */
	public void setCurrentEpoch(Epoch currentEpoch) {
		this.currentEpoch = currentEpoch;
	}
}