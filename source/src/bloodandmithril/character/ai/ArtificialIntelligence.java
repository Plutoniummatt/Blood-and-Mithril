package bloodandmithril.character.ai;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.util.Util.firstNonNull;

import java.io.Serializable;

import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.ai.task.Wait;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.math.Vector2;

/**
 * Artificial intelligence super-class
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class ArtificialIntelligence implements Serializable {
	private static final long serialVersionUID = 272133135274740547L;

	/** The unique identifier of the host, note we can't put a reference to host here, otherwise we would attempt to save a circular reference */
	private final IndividualIdentifier hostId;

	/** The current {@link AITask} the {@link #host} is currently executing */
	protected AITask currentTask = new Idle();

	/** {@link AIMode} */
	private AIMode mode = AIMode.AUTO;

	/**
	 * Constructor, starts the AI processing thread if it has not been started.
	 */
	public ArtificialIntelligence(Individual host) {
		this.hostId = host.getId();
	}


	/** Adds an item to the AI processing thread to await execution */
	public void update(float delta) {
		AIProcessor.setup();
		if (AIProcessor.aiThread != null && AIProcessor.aiThread.isAlive()) {
			if (mode == AIMode.AUTO) {
				AIProcessor.aiThreadTasks.add(() ->
					{
						switch (mode) {
						case AUTO:
							determineCurrentTask();
							break;
						case MANUAL:
							break;
						default:
							throw new IllegalStateException("AI Mode not recognised");
					}
				});
			}

			if (currentTask != null) {
				AITask taskToExecute = getCurrentTask();
				taskToExecute.execute(delta);
				Logger.aiDebug(hostId.getSimpleName() + " is: " + taskToExecute.getDescription(), LogLevel.INFO);

				if (currentTask.isComplete() && !currentTask.uponCompletion() && !(currentTask instanceof Idle)) {
					setCurrentTask(new Idle());
				}
			}
		} else if (ClientServerInterface.isServer()) {
			throw new RuntimeException("Something has caused the AI thread to terminate");
		}
	}


	/** Clears current task and sets to to manual control */
	public synchronized void setToManual() {
		mode = AIMode.MANUAL;
	}


	/** Clears current task */
	public synchronized void setToAuto(boolean clearTask) {
		mode = AIMode.AUTO;
		if (clearTask) {
			currentTask = new Idle();
		}
	}


	/** Instruct the {@link ArtificialIntelligence} to perform a {@link AITask} */
	public synchronized void setCurrentTask(AITask task) {
		currentTask = task;
	}


	/** Returns the current {@link AITask} */
	public synchronized AITask getCurrentTask() {
		return firstNonNull(currentTask, new Idle());
	}

	/** Implementation-specific update method */
	protected abstract void determineCurrentTask();


	/** Calculates the distance from an individual */
	protected float distanceFrom(Individual other) {
		return getHost().getState().position.cpy().sub(other.getState().position).len();
	}


	/** Calculates the distance to a location */
	protected float distanceFrom(Vector2 location) {
		return getHost().getState().position.cpy().sub(location).len();
	}


	/** Returns the host */
	protected Individual getHost() {
		return Domain.getIndividual(hostId.getId());
	}


	/**
	 * The mode an {@link ArtificialIntelligence} is in.
	 *
	 * @author Matt
	 */
	public enum AIMode {
		AUTO, MANUAL
	}


	/**
	 * Walk in a random direction by a random distance up to a maximum.
	 *
	 * @param distance - The maximum distance from the hosts current position that the host wanders.
	 */
	protected void wander(float distance, boolean fly) {
		Individual host = Domain.getIndividual(hostId.getId());

		if (Util.getRandom().nextBoolean() && getCurrentTask() instanceof Idle) {
			try {
				AIProcessor.sendPathfindingRequest(
					host,
					new WayPoint(
						Topography.convertToWorldCoord(
							getGroundAboveOrBelowClosestEmptyOrPlatformSpace(
								new Vector2(
									host.getState().position.x + (0.5f - Util.getRandom().nextFloat()) * distance,
									host.getState().position.y + host.getHeight() / 2
								),
								10,
								Domain.getWorld(host.getWorldId())
							),
							true
						)
					),
					fly,
					0f,
					true,
					false
				);
			} catch (NullPointerException e) {
				setCurrentTask(new Wait(host, Util.getRandom().nextFloat() * 3f + 1f));
			}
		} else if (currentTask instanceof Idle) {
			setCurrentTask(new Wait(host, Util.getRandom().nextFloat() * 3f + 1f));
		}
	}


	/**
	 * Uses {@link GoToLocation} to go to an {@link Individual}.
	 *
	 * @param individual - {@link Individual} to go to.
	 * @param tolerance - The tolerance distance.
	 */
	public void goToIndividual(Individual individual, float tolerance, boolean fly) {
		if (distanceFrom(individual) > tolerance) {
			Individual host = Domain.getIndividual(hostId.getId());
			setCurrentTask(
				goTo(
					host,
					host.getState().position.cpy(),
					new WayPoint(individual.getState().position),
					fly,
					individual.getWidth() * 2,
					true
				)
			);
		}
	}
}