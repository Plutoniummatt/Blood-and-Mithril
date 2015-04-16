package bloodandmithril.persistence.world;

import static bloodandmithril.persistence.GameSaver.getSavePath;
import static bloodandmithril.persistence.PersistenceUtil.encode;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.patterns.GlobalLayers;
import bloodandmithril.persistence.GameSaver;
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;


/**
 * Saves {@link World}s using the {@link Serializable} interface
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class WorldSaver {

	/** The current chunk coordinates that is in the queue to be saved/flushed */
	private static final ConcurrentDualKeyHashMap<Integer, Integer, Boolean> chunksInQueue = new ConcurrentDualKeyHashMap<Integer, Integer, Boolean>();

	/**
	 * Sets up this class
	 */
	private static synchronized void setup() {
		if (GameSaver.saverThread == null) {
			GameSaver.saverThread = new Thread(() -> {
				while (true) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
						throw new RuntimeException("Something has interrupted the chunk loading thread.");
					}
					processItems(0);
				}
			});

			GameSaver.saverThread.setName("Saver Thread");
			GameSaver.saverThread.setDaemon(false);
			GameSaver.saverThread.start();
		}
	}


	/**
	 * Processes items in the saver thread
	 */
	private static void processItems(final int n) {
		if (GameSaver.saverTasks.isEmpty()) {
			if (n != 0) {
				Logger.saverDebug("Saver thread processed " + n + " items", LogLevel.INFO);
			}
		} else {
			GameSaver.saverTasks.poll().execute();
			processItems(n + 1);
		}
	}


	/**
	 * Saves all chunks and associated generation data
	 */
	public static void save() {
		setup();
		persistUnloadedChunks();
		if (GameSaver.saverThread.isAlive()) {
			GameSaver.saverTasks.add(() -> {
				FileHandle structures = Gdx.files.local(getSavePath() + "/world/structures.txt");
				FileHandle worlds = Gdx.files.local(getSavePath() + "/world/worlds.txt");
				FileHandle layers = Gdx.files.local(getSavePath() + "/world/layers.txt");

				structures.writeString(encode(Structures.getStructures()), false);
				worlds.writeString(encode(Domain.getWorlds()), false);
				layers.writeString(encode(GlobalLayers.layers), false);

				for (Entry<Integer, World> world : Domain.getWorlds().entrySet()) {
					saveStructureData(world);

					ZipHelper zip = new ZipHelper(getSavePath() + "/world/world" + Integer.toString(world.getKey()), "/chunkData.zip");
					ZipFile zipTemp = null;

					for (Entry<Integer, HashMap<Integer, Chunk>> columnToSave : world.getValue().getTopography().getChunkMap().chunkMap.entrySet()) {
						saveColumn(columnToSave.getKey(), columnToSave.getValue(), zip);
					}

					try {
						zipTemp = new ZipFile(getSavePath() + "/world/world" + Integer.toString(world.getKey()) + "/chunkDataTemp.zip");

						Enumeration<? extends ZipEntry> allPreviousEntries = ZipHelper.readAllEntries(zipTemp);
						while(allPreviousEntries.hasMoreElements()) {
							ZipEntry nextElement = allPreviousEntries.nextElement();
							String stringContent = ZipHelper.readEntry(zipTemp, nextElement);
							ChunkData data = PersistenceUtil.decode(stringContent);

							zip.addFile("column" + data.xChunkCoord + (data.foreground ? "/f" : "/b") + data.yChunkCoord+ "/", data.foreground ? "fData" : "bData", stringContent, true);
						}

						zipTemp.close();
						FileHandle toDelete = Gdx.files.local(getSavePath() + "/world/world" + Integer.toString(world.getKey()) + "/chunkDataTemp.zip");
						toDelete.delete();
					} catch (IOException e) {
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
	private static void persistUnloadedChunks() {
		if (GameSaver.mostRecentlyLoaded != null) {
			for (Integer world : Domain.getWorlds().keySet()) {
				FileHandle existingSavedChunks = Gdx.files.local("save/" + GameSaver.mostRecentlyLoaded.name + "/world/world" + Integer.toString(world) + "/chunkData.zip");
				existingSavedChunks.copyTo(Gdx.files.local(getSavePath() + "/world/world" + Integer.toString(world) + "/chunkDataTemp.zip"));
			}
		}
	}


	/**
	 * Saves the data used for generation
	 */
	private static void saveStructureData(Entry<Integer, World> world) {
		FileHandle superStructureKeys = Gdx.files.local(getSavePath() + "/world/world" + Integer.toString(world.getKey()) + "/superStructureKeys.txt");
		FileHandle subStructureKeys = Gdx.files.local(getSavePath() + "/world/world" + Integer.toString(world.getKey()) + "/subStructureKeys.txt");

		superStructureKeys.writeString(encode(world.getValue().getTopography().getStructures().getSuperStructureKeys()), false);
		subStructureKeys.writeString(encode(world.getValue().getTopography().getStructures().getSubStructureKeys()), false);
	}


	/**
	 * @param columnToSave
	 */
	private static void saveColumn(int x, HashMap<Integer, Chunk> columnToSave, ZipHelper zip) {
		for (Entry<Integer, Chunk> chunkToSave : columnToSave.entrySet()) {
			saveChunk(chunkToSave.getValue(), x, chunkToSave.getKey(), zip);
		}
	}


	/**
	 * @param chunk to save
	 */
	private static void saveChunk(Chunk chunk, int x, int y, ZipHelper zip) {
		zip.addFile("column" + x + "/f" + y + "/", "fData", encode(chunk.getChunkData(true)), false);
		zip.addFile("column" + x + "/b" + y + "/", "bData", encode(chunk.getChunkData(false)), false);
		chunksInQueue.remove(x, y);
	}


	/**
	 * Saves a chunk to disk and flush it from memory
	 */
	public static void saveAndFlushChunk(final int x, final int y, final Topography topography) {
		if (chunksInQueue.get(x, y) == null) {
			GameSaver.saverTasks.add(() -> {
				ZipHelper zip = new ZipHelper(getSavePath() + "/world", "/chunkData.zip");
				saveChunk(topography.getChunkMap().get(x).get(y), x, y, zip);
				topography.getChunkMap().get(x).remove(y);
			});
			chunksInQueue.put(x, y, true);
		}
	}
}
