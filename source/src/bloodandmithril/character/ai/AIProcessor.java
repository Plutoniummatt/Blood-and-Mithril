package bloodandmithril.character.ai;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import static bloodandmithril.world.Domain.getIndividual;

import java.util.concurrent.ArrayBlockingQueue;

import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.JitAITask;
import bloodandmithril.character.ai.task.Jump;
import bloodandmithril.character.ai.task.Travel;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.Task;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;


/**
 * Class responsible for AI processing
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class AIProcessor {

	/** Threads that handles AI, priority thread for processing selected individuals */
	static Thread aiThread, pathFinderThread;

	/** {@link #aiThread} and {@link #pathFinderThread} processing {@link Task}s */
	private static ArrayBlockingQueue<Task> aiThreadTasks = new ArrayBlockingQueue<Task>(5000);
	private static ArrayBlockingQueue<Task> pathFinderTasks = new ArrayBlockingQueue<Task>(5000);

	/**
	 * Sets up this class
	 */
	public static synchronized void setup() {
		if (aiThread == null && ClientServerInterface.isServer()) {
			aiThread = new Thread(() -> {

				while (true) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
						throw new RuntimeException("Something interrupted the AI thread");
					}
					processItems(0);
				}
			});

			aiThread.setName("AI Thread");
			aiThread.setDaemon(false);
			aiThread.start();
		}

		if (pathFinderThread == null && ClientServerInterface.isServer()) {
			pathFinderThread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
							throw new RuntimeException("Something interrupted the pathfinder thread");
						}
						processItems(0);
					}
				}

				private void processItems(final int n) {

					// StackOverflow
					if (n > 50) {
						return;
					}

					if (pathFinderTasks.isEmpty()) {
						Logger.aiDebug("Processed " + n + " pathfinder items", LogLevel.TRACE);
					} else {
						pathFinderTasks.poll().execute();
						processItems(n + 1);
					}
				}
			});

			pathFinderThread.setName("Pathfinder Thread");
			pathFinderThread.setDaemon(false);
			pathFinderThread.start();
		}
	}


	public static int getNumberOfOutstandingTasks() {
		return AIProcessor.aiThreadTasks.size() + AIProcessor.pathFinderTasks.size();
	}


	/**
	 * Processes items in the AI thread
	 */
	private static void processItems(final int n) {
		while (!aiThreadTasks.isEmpty()) {
			aiThreadTasks.poll().execute();
		}
		Logger.aiDebug("Processed " + n + " AI items", LogLevel.TRACE);
	}


	/**
	 * Queue a task for {@link #pathFinderThread} for {@link GoToLocation}.
	 */
	public static void sendPathfindingRequest(final Individual host, final WayPoint destination, final boolean fly, final float forceTolerance, final boolean safe, boolean add) {
		pathFinderTasks.add(
			() -> {
				synchronized (host) {
					AITask currentTask = host.getAI().getCurrentTask();
					if (currentTask instanceof Travel && add) {
						((Travel) currentTask).addGotoLocation(new JitGoToLocation(host.getId(), destination, fly, forceTolerance, safe));
					} else {
						Travel travel = new Travel(host.getId());
						travel.addGotoLocation(new JitGoToLocation(host.getId(), destination, fly, forceTolerance, safe));
						host.getAI().setCurrentTask(travel);
					}
				}
			}
		);
	}


	/**
	 * Queue a task for {@link #pathFinderThread} for a jump.
	 */
	public static void sendJumpResolutionRequest(final Individual host, final Vector2 start, final Vector2 destination, boolean add) {
		pathFinderTasks.add(
			() -> {
				synchronized (host) {
					AITask currentTask = host.getAI().getCurrentTask();
					if (currentTask instanceof Travel && add) {
						((Travel) currentTask).addJump(new Jump(host.getId(), new ReturnIndividualPosition(host), destination));
					} else {
						Travel travel = new Travel(host.getId());
						travel.addJump(new Jump(host.getId(), new ReturnIndividualPosition(host), destination));
						host.getAI().setCurrentTask(travel);
					}
				}
			}
		);
	}


	/**
	 * @param t task to add
	 */
	public static void addTaskToAIThread(Task t) {
		aiThreadTasks.add(t);
	}


	public static class ReturnIndividualPosition implements SerializableFunction<Vector2> {
		private static final long serialVersionUID = 1752990224855404895L;
		private int individualId;

		public ReturnIndividualPosition(Individual individual) {
			this.individualId = individual.getId().getId();

		}

		@Override
		public Vector2 call() {
			return getIndividual(individualId).getState().position;
		}
	}


	public static class JitGoToLocation extends JitAITask {
		private static final long serialVersionUID = 7866039883039620197L;
		private WayPoint destination;

		public JitGoToLocation(IndividualIdentifier hostId, WayPoint destination, boolean fly, float forceTolerance, boolean safe) {
			super(hostId, new JitGoToLocationFunction(hostId.getId(), destination, fly, forceTolerance, safe));
			this.destination = destination;
		}

		public WayPoint getDestination() {
			return destination;
		}
	}


	public static class JitGoToLocationFunction implements SerializableFunction<GoToLocation> {
		private static final long serialVersionUID = -1856893010442317666L;
		private int hostId;
		private WayPoint destination;
		private boolean fly;
		private float forceTolerance;
		private boolean safe;

		/**
		 * Constructor
		 */
		public JitGoToLocationFunction(int hostId, WayPoint destination, boolean fly, float forceTolerance, boolean safe) {
			this.hostId = hostId;
			this.destination = destination;
			this.fly = fly;
			this.forceTolerance = forceTolerance;
			this.safe = safe;
		}

		@Override
		public GoToLocation call() {
			Individual host = Domain.getIndividual(hostId);
			return goTo(host, host.getState().position.cpy(), destination, fly, forceTolerance, safe);
		}
	}
}
