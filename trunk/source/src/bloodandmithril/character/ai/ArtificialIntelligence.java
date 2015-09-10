package bloodandmithril.character.ai;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.util.Util.firstNonNull;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.perception.Stimulus;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
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
	private IndividualIdentifier hostId;

	/** The current {@link AITask} the {@link #host} is currently executing */
	protected AITask currentTask = new Idle();

	/** {@link AIMode} */
	private AIMode mode = AIMode.AUTO;

	/** Delay between process ticks of individual-specific AI */
	private float aiRoutineProcessingTimer;

	/** Stimuli as perceived by the host */
	private LinkedBlockingQueue<Stimulus> stimuli = new LinkedBlockingQueue<Stimulus>();

	private LinkedList<Routine> aiRoutines = new LinkedList<>();

	public ArtificialIntelligence copy() {
		ArtificialIntelligence internalCopy = internalCopy();
		internalCopy.hostId = hostId;
		internalCopy.currentTask = currentTask;
		internalCopy.mode = mode;
		internalCopy.stimuli = new LinkedBlockingQueue<Stimulus>(stimuli);
		internalCopy.aiRoutines = new LinkedList<>(aiRoutines);

		return internalCopy;
	}


	protected abstract ArtificialIntelligence internalCopy();

	/**
	 * Adds routines for this {@link ArtificialIntelligence}
	 */
	protected abstract void addRoutines();


	/**
	 * Constructor, starts the AI processing thread if it has not been started.
	 */
	public ArtificialIntelligence(Individual host) {
		this.hostId = host.getId();
	}


	/** Adds an item to the AI processing thread to await execution */
	public void update(float delta) {
		AIProcessor.setup();

		if (!getHost().isAlive()) {
			return;
		}

		if (aiRoutines.isEmpty()) {
			addRoutines();
		}

		if (AIProcessor.aiThread != null && AIProcessor.aiThread.isAlive()) {
			if (mode == AIMode.AUTO && !getHost().isAISuppressed()) {
				AIProcessor.addTaskToAIThread(() ->
					{
						switch (mode) {
						case AUTO:
							aiRoutineProcessingTimer += delta;
							if (aiRoutineProcessingTimer > 0.5f) {
								processAIRoutines();
							}
							reactToStimuli();
							determineCurrentTask();
							break;
						case MANUAL:
							break;
						default:
							throw new IllegalStateException("AI Mode not recognised");
					}
				});
			}

			if (getCurrentTask() != null) {
				AITask taskToExecute = getCurrentTask();
				taskToExecute.execute(delta);
				Logger.aiDebug(hostId.getSimpleName() + " is: " + taskToExecute.getDescription(), LogLevel.INFO);

				if (taskToExecute.isComplete() && !taskToExecute.uponCompletion() && !(taskToExecute instanceof Idle)) {
					setCurrentTask(new Idle());
				}
			}
		} else if (ClientServerInterface.isServer()) {
			throw new RuntimeException("Something has caused the AI thread to terminate");
		}
	}


	/**
	 * Processes specific AI routines
	 */
	private void processAIRoutines() {
		for (Routine routine : aiRoutines) {
			if (!routine.areExecutionConditionsMet()) {
				continue;
			}

			AITask internalCurrentTask = getCurrentTask();
			if (internalCurrentTask instanceof Routine) {
				if (routine.getPriority() > ((Routine) internalCurrentTask).getPriority()) {
					routine.prepare();
					setCurrentTask(routine);
					break;
				}
			} else {
				routine.prepare();
				setCurrentTask(routine);
			}
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


	public AIMode getAIMode() {
		return mode;
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

	/** Reacts to any stimuli */
	private synchronized void reactToStimuli() {
		while(!stimuli.isEmpty()) {
			Stimulus polled = stimuli.poll();
			for (Routine r : aiRoutines) {
				if (r instanceof StimulusDrivenRoutine) {
					((StimulusDrivenRoutine) r).attemptTrigger(polled);
				}
			}
			polled.stimulate(getHost());
		}
	}

	/** Calculates the distance from an individual */
	protected float distanceFrom(Individual other) {
		return getHost().getState().position.cpy().sub(other.getState().position).len();
	}


	/** Gets all stimuli */
	public synchronized void addStimulus(Stimulus stimulus) {
		stimuli.add(stimulus);
	}


	/** Clears all stimuli */
	public synchronized void clearStimuli() {
		stimuli.clear();
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
			} catch (Exception e) {
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


	public LinkedList<Routine> getAiRoutines() {
		return aiRoutines;
	}


	public void addRoutine(Routine routine) {
		aiRoutines.add(routine);
	}
}