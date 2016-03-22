package bloodandmithril.persistence;

import static bloodandmithril.persistence.PersistenceUtil.encode;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.character.IndividualSaver;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.persistence.world.WorldSaver;
import bloodandmithril.util.Task;
import bloodandmithril.world.Domain;

/**
 * Class for game saving
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class GameSaver {

	@Inject private FactionControlService factionControlService;
	@Inject private WorldSaver worldSaver;
	@Inject private ParameterPersistenceService parameterPersistenceService;
	@Inject private IndividualSaver individualSaver;
	@Inject private ChunkLoader chunkLoader;

	/** The list of tasks the saver thread must execute */
	public final BlockingQueue<Task> saverTasks = new ArrayBlockingQueue<Task>(500);

	/** File path for saved games */
	private String savePath;

	/** Name to use for saved game */
	private String savedGameName = null;

	/** The meta data of the most recently loaded saved game */
	public PersistenceMetaData mostRecentlyLoaded;

	/** Boolean switches used for processing */
	private AtomicBoolean pending = new AtomicBoolean(false), saving = new AtomicBoolean(false), andExit = new AtomicBoolean(false), exiting = new AtomicBoolean(false);

	/**
	 * Saves the game
	 */
	public synchronized void save(String name, boolean exitAfter) {
		savedGameName = name;
		savePath = "save/" + name;
		pending.set(true);
		andExit.set(exitAfter);
	}


	public synchronized void setPersistencePath(String savePath) {
		this.savePath = savePath;
	}


	public String getSavePath() {
		return savePath;
	}


	/**
	 * @return True if currently saving or pending to save
	 */
	public synchronized boolean isSaving() {
		return saving.get() || pending.get();
	}


	private void internalSave() {

		// Save parameters
		saverTasks.add(
			() -> {
				parameterPersistenceService.saveParameters();
			}
		);

		saveMetaData();

		// Save chunks + generation data
		worldSaver.save();

		saveFactions();

		// Save all individuals
		saverTasks.add(
			() -> {
				individualSaver.saveAll();
				saveCompleted();
			}
		);
	}


	private void saveFactions() {
		FileHandle factiondata = Gdx.files.local(this.savePath + "/world/factions.txt");
		factiondata.writeString(encode(Domain.getFactions()), false);

		if (ClientServerInterface.isClient()) {
			FileHandle controlled = Gdx.files.local(this.savePath + "/world/controlledfactions.txt");
			controlled.writeString(encode(factionControlService.getControlledFactions()), false);
		}
	}


	/** Saves metadata */
	private void saveMetaData() {
		FileHandle metadata = Gdx.files.local(this.savePath + "/metadata.txt");
		metadata.writeString(encode(new PersistenceMetaData(savedGameName)), false);
	}


	/** Notifies this class that saving is complete */
	private synchronized void saveCompleted() {
		saving.set(false);
		exiting.set(andExit.get());
	}


	public synchronized void update() {
		if (pending.get()) {
			if (!outstandingTasks()) {
				pending.set(false);
				saving.set(true);
				internalSave();
			}
		} else {
			if (saving.get()) {
				return;
			} else {
				if (exiting.get()) {
					Gdx.app.exit();
				}
				return;
			}
		}
	}


	/**
	 * @return true if there are outstanding tasks in AI thread, loader thread or pathfinding thread
	 */
	private boolean outstandingTasks() {
		return AIProcessor.getNumberOfOutstandingTasks() + chunkLoader.loaderTasks.size() != 0;
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
