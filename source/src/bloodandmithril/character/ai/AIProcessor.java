package bloodandmithril.character.ai;

import java.util.concurrent.ArrayBlockingQueue;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Task;
import bloodandmithril.util.Logger.LogLevel;


/**
 * Class responsible for AI processing
 *
 * @author Matt
 */
public class AIProcessor {

	/** Threads that handles AI, priority thread for processing selected individuals */
	static Thread aiThread, pathFinderThread;

	/** {@link #aiThread} and {@link #pathFinderThread} processing {@link Task}s */
	public static ArrayBlockingQueue<Task> aiThreadTasks = new ArrayBlockingQueue<Task>(5000);
	public static ArrayBlockingQueue<Task> pathFinderTasks = new ArrayBlockingQueue<Task>(5000);

	/**
	 * Sets up this class
	 */
	public static void setup() {
		if (aiThread == null) {
			aiThread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
							throw new RuntimeException("Something interrupted the AI thread");
						}
						processItems(0);
					}
				}

				private void processItems(final int n) {
					if (aiThreadTasks.isEmpty()) {
						Logger.aiDebug("Processed " + n + " AI items", LogLevel.TRACE);
					} else {
						aiThreadTasks.poll().execute();
						processItems(n + 1);
					}
				}
			});

			aiThread.setName("AI Thread");
			aiThread.setDaemon(false);
			aiThread.start();
		}

		if (pathFinderThread == null) {
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
	 * Queue a task for {@link #pathFinderThread}.
	 */
	public static void sendPathfindingRequest(final Individual host, final WayPoint destination, final boolean fly, final float forceTolerance) {
		pathFinderTasks.add(
			new Task() {
				@Override
				public void execute() {
					host.ai.setCurrentTask(new GoToLocation(host, destination, fly, forceTolerance));
				}
			}
		);
	}
}
