package bloodandmithril.persistence;

import static bloodandmithril.persistence.PersistenceUtil.encode;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AIProcessor;
import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.ThreadedTasks;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.character.IndividualSaver;
import bloodandmithril.persistence.world.ChunkProvider;
import bloodandmithril.persistence.world.WorldSaver;
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
	@Inject private ChunkProvider chunkLoader;
	@Inject private PersistenceParameters persistenceParameters;
	@Inject private ThreadedTasks threadedTasks;

	/** Boolean switches used for processing */
	private AtomicBoolean pending = new AtomicBoolean(false), saving = new AtomicBoolean(false), andExit = new AtomicBoolean(false), exiting = new AtomicBoolean(false);

	/**
	 * Saves the game
	 */
	public synchronized void save(final String name, final boolean exitAfter) {
		persistenceParameters.setSavedGameName(name);
		persistenceParameters.setSavePath("save/" + name);
		pending.set(true);
		andExit.set(exitAfter);
	}


	/**
	 * @return True if currently saving or pending to save
	 */
	public synchronized boolean isSaving() {
		return saving.get() || pending.get();
	}


	private void internalSave() {

		// Save parameters
		threadedTasks.saverTasks.add(
			() -> {
				parameterPersistenceService.saveParameters();
			}
		);

		saveMetaData();

		// Save chunks + generation data
		worldSaver.save();

		saveFactions();

		// Save all individuals
		threadedTasks.saverTasks.add(
			() -> {
				individualSaver.saveAll();
				saveCompleted();
			}
		);
	}


	private void saveFactions() {
		final FileHandle factiondata = Gdx.files.local(persistenceParameters.getSavePath() + "/world/factions.txt");
		factiondata.writeString(encode(Domain.getFactions()), false);

		if (ClientServerInterface.isClient()) {
			final FileHandle controlled = Gdx.files.local(persistenceParameters.getSavePath() + "/world/controlledfactions.txt");
			controlled.writeString(encode(factionControlService.getControlledFactions()), false);
		}
	}


	/** Saves metadata */
	private void saveMetaData() {
		final FileHandle metadata = Gdx.files.local(persistenceParameters.getSavePath() + "/metadata.txt");
		metadata.writeString(encode(new PersistenceMetaData(persistenceParameters.getSavedGameName())), false);
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

		public PersistenceMetaData(final String name) {
			this.name = name;
		}

		public String name;
		public Date date = new Date();
	}
}
