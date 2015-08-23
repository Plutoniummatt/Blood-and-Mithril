package bloodandmithril.persistence;

import java.io.Serializable;
import java.util.Random;

import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.prop.Prop;

import com.badlogic.gdx.math.Vector2;

/**
 * Parameters that require saving
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Parameters implements Serializable {
	private static final long serialVersionUID = -4405235813990330113L;

	private long eventIdCounter = 0;
	private int structureKeyCounter = 0;
	private int initialSeed = 0;
	private int individualIdCounter = 0;
	private int propIdCounter = 0;
	private int factionIdCounter = 0;
	private int worldIdCounter = 0;
	private int itemCounter = 0;
	private int projectileCounter = 0;
	private int activeWorldId = 0;
	private long particleCounter = 0;

	private Vector2 camera;

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
	 * Returns the current event ID
	 */
	public synchronized long getNextEventId() {
		eventIdCounter++;
		return eventIdCounter;
	}
	

	/**
	 * Returns the faction key counter
	 */
	public synchronized int getNextFactionId() {
		factionIdCounter++;
		return factionIdCounter;
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
	 * Returns the next unique identifier to use for {@link Item}
	 */
	public synchronized int getNextItemId() {
		itemCounter++;
		return itemCounter;
	}


	/**
	 * Returns the next unique identifier to use for {@link Projectile}
	 */
	public synchronized int getNextProjectileId() {
		projectileCounter++;
		return projectileCounter;
	}


	/**
	 * Returns the next unique identifier to use for {@link Particle}
	 */
	public synchronized long getNextParticleId() {
		particleCounter++;
		return particleCounter;
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
	 * Sets the saved active world id
	 */
	public int getActiveWorldId() {
		return activeWorldId;
	}


	/**
	 * Returns the saved active world id
	 */
	public void setActiveWorldId(int activeWorldId) {
		this.activeWorldId = activeWorldId;
	}
}