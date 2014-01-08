package bloodandmithril.character.ai;

import java.io.Serializable;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.ai.task.Wait;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;
import bloodandmithril.util.Util;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.math.Vector2;

/**
 * Artificial intelligence super-class
 *
 * @author Matt
 */
public abstract class ArtificialIntelligence implements Serializable {
	private static final long serialVersionUID = 272133135274740547L;

	/** The unique identifier of the host, note we can't put a reference to host here, otherwise we would attempt to save a circular reference */
	public IndividualIdentifier hostId;

	/** The current {@link AITask} the {@link #host} is currently executing */
	protected AITask currentTask = new Idle();

	/** {@link AIMode} */
	public AIMode mode = AIMode.AUTO;

	/**
	 * Constructor, starts the AI processing thread if it has not been started.
	 */
	public ArtificialIntelligence(Individual host) {
		this.hostId = host.id;
	}


	/** Adds an item to the AI processing thread to await execution */
	public void update() {
		AIProcessor.setup();
		if (AIProcessor.aiThread != null && AIProcessor.aiThread.isAlive()) {
			if (mode == AIMode.AUTO) {
				AIProcessor.aiThreadTasks.add(new Task() {
					@Override
					public void execute() {
						switch (mode) {
						case AUTO:
							determineCurrentTask();
							break;
						case MANUAL:
							break;
						default:
							throw new IllegalStateException("AI Mode not recognised");
						}
					}
				});
			}

			if (currentTask != null) {
				AITask taskToExecute = getCurrentTask();
				taskToExecute.execute();
				Logger.aiDebug(hostId.getSimpleName() + " is: " + taskToExecute.getDescription(), LogLevel.INFO);

				if (currentTask.isComplete()) {
					currentTask.uponCompletion();
					if (!(currentTask instanceof Idle)) {
						setCurrentTask(new Idle());
					}
				}
			}
		} else if ("true".equals(System.getProperty("server"))) {
			throw new RuntimeException("Something has caused the AI thread to terminate");
		}
	}


	/** Clears current task and sets to to manual control */
	public void setToManual() {
		mode = AIMode.MANUAL;
	}


	/** Clears current task */
	public void setToAuto(boolean clearTask) {
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
	public abstract AITask getCurrentTask();

	/** Implementation-specific update method */
	protected abstract void determineCurrentTask();


	/** Calculates the distance from an individual */
	protected float distanceFrom(Individual other) {
		return GameWorld.individuals.get(hostId.id).state.position.cpy().sub(other.state.position).len();
	}


	/** Calculates the disance to a location */
	protected float distanceFrom(Vector2 location) {
		return GameWorld.individuals.get(hostId.id).state.position.cpy().sub(location).len();
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
	public void wander(float distance, boolean fly) {
		Individual host = GameWorld.individuals.get(hostId.id);

		if (Util.getRandom().nextBoolean() && getCurrentTask() instanceof Idle) {
			setCurrentTask(new GoToLocation(host, new WayPoint(new Vector2(host.state.position.x + (0.5f - Util.getRandom().nextFloat()) * distance, host.state.position.y + host.height / 2)), fly, 0f, true));
		} else if (currentTask instanceof Idle) {
			setCurrentTask(new Wait(host, Util.getRandom().nextFloat() * 5f + 3f));
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
			Individual host = GameWorld.individuals.get(hostId.id);
			setCurrentTask(new GoToLocation(host, new WayPoint(individual.state.position), fly, individual.width * 2, true));
		}
	}
}