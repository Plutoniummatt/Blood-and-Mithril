package bloodandmithril.character.ai;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;

import java.util.concurrent.ArrayBlockingQueue;

import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.Jump;
import bloodandmithril.character.ai.task.Travel;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;

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
	public static ArrayBlockingQueue<Task> aiThreadTasks = new ArrayBlockingQueue<Task>(5000);
	public static ArrayBlockingQueue<Task> pathFinderTasks = new ArrayBlockingQueue<Task>(5000);

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


	/**
	 * Processes items in the AI thread
	 */
	private static void processItems(final int n) {
		if (aiThreadTasks.isEmpty()) {
			Logger.aiDebug("Processed " + n + " AI items", LogLevel.TRACE);
		} else {
			aiThreadTasks.poll().execute();
			processItems(n + 1);
		}
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
						((Travel) currentTask).addGotoLocation(goTo(host, host.getState().position.cpy(), destination, fly, forceTolerance, safe));
					} else {
						Travel travel = new Travel(host.getId());
						travel.addGotoLocation(goTo(host, host.getState().position.cpy(), destination, fly, forceTolerance, safe));
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
							((Travel) currentTask).addJump(new Jump(host.getId(), start, destination));
						} else {
							Travel travel = new Travel(host.getId());
							travel.addJump(new Jump(host.getId(), start, destination));
							host.getAI().setCurrentTask(travel);
						}
					}
				}
			);
	}
}
