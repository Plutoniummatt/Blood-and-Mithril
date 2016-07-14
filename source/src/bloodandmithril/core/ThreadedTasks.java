package bloodandmithril.core;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.google.inject.Singleton;

import bloodandmithril.util.Task;

/**
 * Holds state of outstanding tasks to execute
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ThreadedTasks {

	/** The list of tasks the saver thread must execute */
	public final BlockingQueue<Task> saverTasks = new ArrayBlockingQueue<Task>(500);
}