package bloodandmithril.persistence;

import static bloodandmithril.persistence.PersistenceUtil.encode;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.character.IndividualSaver;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.persistence.world.WorldSaver;
import bloodandmithril.util.Task;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Class for game saving
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class GameSaver {

	/** The thread responsible for loading */
	public static Thread saverThread;

	/** The list of tasks the saver thread must execute */
	public static final BlockingQueue<Task> saverTasks = new ArrayBlockingQueue<Task>(500);

	/** File path for saved games */
	private static String savePath;

	/** Name to use for saved game */
	private static String savedGameName = null;

	/** The meta data of the most recently loaded saved game */
	public static PersistenceMetaData mostRecentlyLoaded;

	/** Boolean switches used for processing */
	private static boolean pending = false, saving = false, andExit = false, exiting = false;

	/**
	 * Saves the game
	 */
	public static synchronized void save(String name, boolean exitAfter) {
		savedGameName = name;
		savePath = "save/" + name;
		pending = true;
		andExit = exitAfter;
	}
	
	
	public static synchronized void setPersistencePath(String savePath) {
		GameSaver.savePath = savePath;
	}
	
	
	public static String getSavePath() {
		return savePath;
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

		saveMetaData();

		// Save chunks + generation data
		WorldSaver.save();

		saveFactions();

		// Save all individuals
		saverTasks.add(
			() -> {
				IndividualSaver.saveAll();
				saveCompleted();
			}
		);
	}


	private static void saveFactions() {
		FileHandle factiondata = Gdx.files.local(GameSaver.savePath + "/world/factions.txt");
		factiondata.writeString(encode(Domain.getFactions()), false);
		
		if (ClientServerInterface.isClient()) {
			FileHandle controlled = Gdx.files.local(GameSaver.savePath + "/world/controlledfactions.txt");
			controlled.writeString(encode(BloodAndMithrilClient.controlledFactions), false);
		}
	}


	/** Saves metadata */
	private static void saveMetaData() {
		FileHandle metadata = Gdx.files.local(GameSaver.savePath + "/metadata.txt");
		metadata.writeString(encode(new PersistenceMetaData(savedGameName)), false);
	}


	/** Notifies this class that saving is complete */
	private static synchronized void saveCompleted() {
		saving = false;
		exiting = andExit;
	}


	public static synchronized void update() {
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


	/**
	 * Persistence meta data, containing meta data of saved games
	 *
	 * @author Matt
	 */
	public static class PersistenceMetaData implements Serializable {
		private static final long serialVersionUID = 7818486179446462250L;

		public PersistenceMetaData(String name) {
			this.name = name;
		}

		public String name;
		public Date date = new Date();
	}
}
