package bloodandmithril.character.ai;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.character.ai.task.gotolocation.GoToLocation.goTo;
import static bloodandmithril.util.Util.firstNonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.perception.Stimulus;
import bloodandmithril.character.ai.routine.stimulusdriven.StimulusDrivenRoutine;
import bloodandmithril.character.ai.task.gotolocation.GoToLocation;
import bloodandmithril.character.ai.task.idle.Idle;
import bloodandmithril.character.ai.task.wait.Wait;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.topography.Topography;

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
	private LinkedBlockingQueue<Stimulus> stimuli = new LinkedBlockingQueue<>();

	private LinkedList<Routine> aiRoutines = new LinkedList<>();

	public ArtificialIntelligence copy() {
		final ArtificialIntelligence internalCopy = internalCopy();
		internalCopy.hostId = hostId;
		internalCopy.currentTask = currentTask;
		internalCopy.mode = mode;
		internalCopy.stimuli = new LinkedBlockingQueue<>(stimuli);
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
	public ArtificialIntelligence(final Individual host) {
		this.hostId = host.getId();
	}


	/** Adds an item to the AI processing thread to await execution */
	public void update(final float delta) {
		AIProcessor.setup();

		if (!getHost().isAlive()) {
			return;
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
				}, getHost().getId().getId());
			}

			if (getCurrentTask() != null) {
				final AITask taskToExecute = getCurrentTask();
				final AITaskExecutor executor = Wiring.injector().getInstance(taskToExecute.getClass().getAnnotation(ExecutedBy.class).value());
				executor.execute(taskToExecute, delta);
				Logger.aiDebug(hostId.getSimpleName() + " is: " + taskToExecute.getShortDescription(), LogLevel.INFO);

				if (executor.isComplete(taskToExecute) && !executor.uponCompletion(taskToExecute) && !(taskToExecute instanceof Idle)) {
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
		final LinkedList<Routine> sortedRoutines = Lists.newLinkedList(aiRoutines);
		Collections.sort(sortedRoutines, (r1, r2) -> {
			return new Integer(r2.getPriority()).compareTo(new Integer(r1.getPriority()));
		});

		for (final Routine routine : sortedRoutines) {
			if (!routine.isEnabled() || !routine.areExecutionConditionsMet()) {
				continue;
			}

			if (!routine.isValid() && routine.isEnabled()) {
				routine.setEnabled(false);
			}

			final Epoch current = Domain.getWorld(routine.getHost().getWorldId()).getEpoch().copy();
			if (routine.getLastOcurrence() != null) {
				final Epoch lastOcurrence = routine.getLastOcurrence().copy();
				lastOcurrence.incrementGameTime(routine.getTimeBetweenOcurrences());

				if (lastOcurrence.isLaterThan(current)) {
					continue;
				}
			}

			final AITask internalCurrentTask = getCurrentTask();
			if (internalCurrentTask instanceof Routine) {
				if (routine.getPriority() > ((Routine) internalCurrentTask).getPriority()) {
					routine.generateTask();
					setCurrentTask(routine);
					routine.setLastOcurrence(current.copy());
					break;
				}
			} else {
				routine.generateTask();
				setCurrentTask(routine);
				routine.setLastOcurrence(current.copy());
			}
		}
	}


	/** Clears current task and sets to to manual control */
	public synchronized void setToManual() {
		mode = AIMode.MANUAL;
	}


	/** Clears current task */
	public synchronized void setToAuto(final boolean clearTask) {
		mode = AIMode.AUTO;
		if (clearTask) {
			currentTask = new Idle();
		}
	}


	public AIMode getAIMode() {
		return mode;
	}


	/** Instruct the {@link ArtificialIntelligence} to perform a {@link AITask} */
	public synchronized void setCurrentTask(final AITask task) {
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
			final Stimulus polled = stimuli.poll();
			for (final Routine r : aiRoutines) {
				if (r instanceof StimulusDrivenRoutine) {
					((StimulusDrivenRoutine) r).attemptTrigger(polled);
				}
			}
			polled.stimulate(getHost());
		}
	}

	/** Calculates the distance from an individual */
	protected float distanceFrom(final Individual other) {
		return getHost().getState().position.cpy().sub(other.getState().position).len();
	}


	/** Gets all stimuli */
	public synchronized void addStimulus(final Stimulus stimulus) {
		stimuli.add(stimulus);
	}


	/** Clears all stimuli */
	public synchronized void clearStimuli() {
		stimuli.clear();
	}


	/** Calculates the distance to a location */
	protected float distanceFrom(final Vector2 location) {
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
	protected void wander(final float distance, final boolean fly) {
		final Individual host = Domain.getIndividual(hostId.getId());

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
							).get(),
							true
						)
					),
					fly,
					0f,
					true,
					false
				);
			} catch (final Exception e) {
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
	public void goToIndividual(final Individual individual, final float tolerance, final boolean fly) {
		if (distanceFrom(individual) > tolerance) {
			final Individual host = Domain.getIndividual(hostId.getId());
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


	public void addRoutine(final Routine routine) {
		for (final Routine r : aiRoutines) {
			r.setPriority(r.getPriority() + 1);
		}
		routine.setPriority(0);
		aiRoutines.add(routine);
	}


	public void removeRoutine(final Routine routine) {
		for (final Routine r : aiRoutines) {
			if (r.getPriority() > routine.getPriority()) {
				r.setPriority(r.getPriority() - 1);
			}
		}

		aiRoutines.remove(routine);
	}
}