package bloodandmithril.world.topography;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Task;

/**
 * Executes topography related tasks, this needs to be called in the main thread
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TopographyTaskExecutor {
	
	/** Any non-main thread topography tasks queued here */
	private BlockingQueue<Task> topographyTasks = new ArrayBlockingQueue<Task>(500000);
	
	/** Adds a task to be processed */
	public synchronized final void addTask(Task task) {
		topographyTasks.add(task);
	}


	/** Executes any tasks queued in {@link #topographyTasks} by other threads */
	public synchronized final void executeBackLog() {
		while (!topographyTasks.isEmpty()) {
			topographyTasks.poll().execute();
		}
	}
}