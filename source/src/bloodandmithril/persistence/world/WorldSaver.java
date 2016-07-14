package bloodandmithril.persistence.world;

import static bloodandmithril.persistence.PersistenceUtil.encode;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.ThreadedTasks;
import bloodandmithril.core.Threading;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.patterns.GlobalLayers;
import bloodandmithril.persistence.PersistenceParameters;
import bloodandmithril.persistence.PersistenceUtil;
import bloodandmithril.persistence.ZipHelper;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Chunk.ChunkData;
import bloodandmithril.world.topography.Topography;


/**
 * Saves {@link World}s using the {@link Serializable} interface
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class WorldSaver {

	@Inject private ThreadedTasks threadedTasks;
	@Inject private Threading threading;
	@Inject private PersistenceParameters persistenceParameters;

	/** The current chunk coordinates that is in the queue to be saved/flushed */
	private static final ConcurrentDualKeyHashMap<Integer, Integer, Boolean> chunksInQueue = new ConcurrentDualKeyHashMap<Integer, Integer, Boolean>();

	/**
	 * Sets up this class
	 */
	private synchronized void setup() {
		if (threading.persistenceThread == null) {
			threading.persistenceThread = new Thread(() -> {
				while (true) {
					try {
						Thread.sleep(100);
					} catch (final InterruptedException e) {
						e.printStackTrace();
						throw new RuntimeException("Something has interrupted the chunk loading thread.");
					}
					processItems(0);
				}
			});

			threading.persistenceThread.setName("Saver Thread");
			threading.persistenceThread.setDaemon(false);
			threading.persistenceThread.start();
		}
	}


	/**
	 * Processes items in the saver thread
	 */
	private void processItems(final int n) {
		if (threadedTasks.saverTasks.isEmpty()) {
			if (n != 0) {
				Logger.saverDebug("Saver thread processed " + n + " items", LogLevel.INFO);
			}
		} else {
			threadedTasks.saverTasks.poll().execute();
			processItems(n + 1);
		}
	}


	/**
	 * Saves all chunks and associated generation data
	 */
	public void save() {
		setup();
		persistUnloadedChunks();
		if (threading.persistenceThread.isAlive()) {
			threadedTasks.saverTasks.add(() -> {
				final FileHandle structures = Gdx.files.local(persistenceParameters.getSavePath() + "/world/structures.txt");
				final FileHandle worlds = Gdx.files.local(persistenceParameters.getSavePath() + "/world/worlds.txt");
				final FileHandle layers = Gdx.files.local(persistenceParameters.getSavePath() + "/world/layers.txt");

				structures.writeString(encode(Structures.getStructures()), false);
				worlds.writeString(encode(Domain.getWorldMap()), false);
				layers.writeString(encode(GlobalLayers.layers), false);

				for (final World world : Domain.getAllWorlds()) {
					saveStructureData(world);

					final ZipHelper zip = new ZipHelper(persistenceParameters.getSavePath() + "/world/world" + Integer.toString(world.getWorldId()), "/chunkData.zip");
					ZipFile zipTemp = null;

					for (final Entry<Integer, HashMap<Integer, Chunk>> columnToSave : world.getTopography().getChunkMap().chunkMap.entrySet()) {
						saveColumn(columnToSave.getKey(), columnToSave.getValue(), zip);
					}

					try {
						zipTemp = new ZipFile(persistenceParameters.getSavePath() + "/world/world" + Integer.toString(world.getWorldId()) + "/chunkDataTemp.zip");

						final Enumeration<? extends ZipEntry> allPreviousEntries = ZipHelper.readAllEntries(zipTemp);
						while(allPreviousEntries.hasMoreElements()) {
							final ZipEntry nextElement = allPreviousEntries.nextElement();
							final String stringContent = ZipHelper.readEntry(zipTemp, nextElement);
							final ChunkData data = PersistenceUtil.decode(stringContent);

							zip.addFile("column" + data.xChunkCoord + (data.foreground ? "/f" : "/b") + data.yChunkCoord+ "/", data.foreground ? "fData" : "bData", stringContent, true);
						}

						zipTemp.close();
						final FileHandle toDelete = Gdx.files.local(persistenceParameters.getSavePath() + "/world/world" + Integer.toString(world.getWorldId()) + "/chunkDataTemp.zip");
						toDelete.delete();
					} catch (final IOException e) {
						Logger.loaderDebug("No previous chunks", LogLevel.DEBUG);
					}

					zip.makeZip();
				}
			});
		} else {
			throw new RuntimeException("Something caused the saver thread to terminate");
		}
	}


	/**
	 * Persists any unloaded chunks that have been saved from a previous save.
	 */
	private void persistUnloadedChunks() {
		if (persistenceParameters.getMostRecentlyLoaded() != null) {
			for (final Integer world : Domain.getAllWorldIds()) {
				final FileHandle existingSavedChunks = Gdx.files.local("save/" + persistenceParameters.getMostRecentlyLoaded().name + "/world/world" + Integer.toString(world) + "/chunkData.zip");
				existingSavedChunks.copyTo(Gdx.files.local(persistenceParameters.getSavePath() + "/world/world" + Integer.toString(world) + "/chunkDataTemp.zip"));
			}
		}
	}


	/**
	 * Saves the data used for generation
	 */
	private void saveStructureData(final World world) {
		final FileHandle superStructureKeys = Gdx.files.local(persistenceParameters.getSavePath() + "/world/world" + Integer.toString(world.getWorldId()) + "/superStructureKeys.txt");
		final FileHandle subStructureKeys = Gdx.files.local(persistenceParameters.getSavePath() + "/world/world" + Integer.toString(world.getWorldId()) + "/subStructureKeys.txt");

		superStructureKeys.writeString(encode(world.getTopography().getStructures().getSuperStructureKeys()), false);
		subStructureKeys.writeString(encode(world.getTopography().getStructures().getSubStructureKeys()), false);
	}


	/**
	 * @param columnToSave
	 */
	private void saveColumn(final int x, final HashMap<Integer, Chunk> columnToSave, final ZipHelper zip) {
		for (final Entry<Integer, Chunk> chunkToSave : columnToSave.entrySet()) {
			saveChunk(chunkToSave.getValue(), x, chunkToSave.getKey(), zip);
		}
	}


	/**
	 * @param chunk to save
	 */
	private void saveChunk(final Chunk chunk, final int x, final int y, final ZipHelper zip) {
		zip.addFile("column" + x + "/f" + y + "/", "fData", encode(chunk.getChunkData(true)), false);
		zip.addFile("column" + x + "/b" + y + "/", "bData", encode(chunk.getChunkData(false)), false);
		chunksInQueue.remove(x, y);
	}


	/**
	 * Saves a chunk to disk and flush it from memory
	 */
	public void saveAndFlushChunk(final int x, final int y, final Topography topography) {
		if (chunksInQueue.get(x, y) == null) {
			threadedTasks.saverTasks.add(() -> {
				final ZipHelper zip = new ZipHelper(persistenceParameters.getSavePath() + "/world", "/chunkData.zip");
				saveChunk(topography.getChunkMap().get(x).get(y), x, y, zip);
				topography.getChunkMap().get(x).remove(y);
			});
			chunksInQueue.put(x, y, true);
		}
	}
}
