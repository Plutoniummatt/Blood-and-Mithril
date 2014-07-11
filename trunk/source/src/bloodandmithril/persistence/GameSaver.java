package bloodandmithril.persistence;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.persistence.character.IndividualSaver;
import bloodandmithril.persistence.item.ItemSaver;
import bloodandmithril.persistence.prop.PropSaver;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.persistence.world.ChunkSaver;
import bloodandmithril.util.Task;

import com.badlogic.gdx.Gdx;

/**
 * Class for game saving
 *
 * @author Matt
 */
public class GameSaver {

	/** The thread responsible for loading */
	public static Thread saverThread;

	/** The list of tasks the saver thread must execute */
	public static final BlockingQueue<Task> saverTasks = new ArrayBlockingQueue<Task>(500);

	/** File path for saved games */
	public static final String savePath = "save/testWorld";

	/** Boolean switches used for processing */
	private static boolean pending = false, saving = false, andExit = false, exiting = false;

	/**
	 * Saves the game
	 */
	public static synchronized void save(boolean exitAfter) {
		pending = true;
		andExit = exitAfter;
	}


	/**
	 * @return True if currently saving or pending to save
	 */
	public static synchronized boolean isSaving() {
		return saving || pending;
	}


	private static void internalSave() {

		// Save parameters
		saverTasks.add(
			() -> {
				ParameterPersistenceService.saveParameters();
			}
		);

		// Save chunks + generation data
		ChunkSaver.save();

		// Save all individuals
		saverTasks.add(
			() -> {
				IndividualSaver.saveAll();
				saveCompleted();
			}
		);

		PropSaver.saveAll();
		ItemSaver.saveAll();
	}


	/** Notifies this class that saving is complete */
	private static synchronized void saveCompleted() {
		saving = false;
		exiting = andExit;
	}


	public static void update() {
		if (pending) {
			if (!outstandingTasks()) {
				pending = false;
				saving = true;
				internalSave();
			}
		} else {
			if (saving) {
				return;
			} else {
				if (exiting) {
					Gdx.app.exit();
				}
				return;
			}
		}
	}


	/**
	 * @return true if there are outstanding tasks in AI thread, loader thread or pathfinding thread
	 */
	private static boolean outstandingTasks() {
		return AIProcessor.aiThreadTasks.size() + AIProcessor.pathFinderTasks.size() + ChunkLoader.loaderTasks.size() != 0;
	}
}
