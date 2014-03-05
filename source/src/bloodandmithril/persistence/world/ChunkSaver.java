package bloodandmithril.persistence.world;

import static bloodandmithril.persistence.PersistenceUtil.encode;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.generation.Structures;
import bloodandmithril.generation.patterns.Layers;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.ZipHelper;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;


/**
 * Saves {@link Chunk}s using the {@link Serializable} interface
 *
 * @author Matt
 */
public class ChunkSaver {

	/** The current chunk coordinates that is in the queue to be saved/flushed */
	private static final ConcurrentDualKeyHashMap<Integer, Integer, Boolean> chunksInQueue = new ConcurrentDualKeyHashMap<Integer, Integer, Boolean>();

	/**
	 * Sets up this class
	 */
	private static void setup() {
		if (GameSaver.saverThread == null) {
			GameSaver.saverThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
							throw new RuntimeException("Something has interrupted the chunk loading thread.");
						}
						processItems(0);
					}
				}

				private void processItems(final int n) {
					if (GameSaver.saverTasks.isEmpty()) {
						if (n != 0) {
							Logger.saverDebug("Saver thread processed " + n + " items", LogLevel.INFO);
						}
					} else {
						GameSaver.saverTasks.poll().execute();
						processItems(n + 1);
					}
				}
			});

			GameSaver.saverThread.setName("Saver Thread");
			GameSaver.saverThread.setDaemon(false);
			GameSaver.saverThread.start();
		}
	}


	/**
	 * Saves all chunks and associated generation data
	 */
	public static void save() {
		setup();
		if (GameSaver.saverThread.isAlive()) {
			GameSaver.saverTasks.add(new Task() {
				@Override
				public void execute() {
					saveGenerationData();

					ZipHelper zip = new ZipHelper(GameSaver.savePath + "/world", "/chunkData.zip");

					for (Entry<Integer, ConcurrentHashMap<Integer, Chunk>> columnToSave : Topography.chunkMap.chunkMap.entrySet()) {
						saveColumn(columnToSave.getKey(), columnToSave.getValue(), zip);
					}

					zip.makeZip();
				}

			});
		} else {
			throw new RuntimeException("Something caused the saver thread to terminate");
		}
	}


	/**
	 * Saves the data used for generation
	 */
	private static void saveGenerationData() {

		FileHandle superStructureKeys = Gdx.files.local(GameSaver.savePath + "/world/superStructureKeys.txt");
		FileHandle subStructureKeys = Gdx.files.local(GameSaver.savePath + "/world/subStructureKeys.txt");
		FileHandle structures = Gdx.files.local(GameSaver.savePath + "/world/structures.txt");
		FileHandle surfaceHeight = Gdx.files.local(GameSaver.savePath + "/world/surfaceHeight.txt");
		FileHandle layers = Gdx.files.local(GameSaver.savePath + "/world/layers.txt");

		superStructureKeys.writeString(encode(Structures.getSuperStructureKeys()), false);
		subStructureKeys.writeString(encode(Structures.getSubStructureKeys()), false);
		structures.writeString(encode(Structures.getStructures()), false);
		surfaceHeight.writeString(encode(Structures.getSurfaceHeight()), false);
		layers.writeString(encode(Layers.layers), false);
	}


	/**
	 * @param columnToSave
	 */
	private static void saveColumn(int x, ConcurrentHashMap<Integer, Chunk> columnToSave, ZipHelper zip) {
		for (Entry<Integer, Chunk> chunkToSave : columnToSave.entrySet()) {
			saveChunk(chunkToSave.getValue(), x, chunkToSave.getKey(), zip);
		}
	}


	/**
	 * @param chunk to save
	 */
	private static void saveChunk(Chunk chunk, int x, int y, ZipHelper zip) {
		zip.addFile("column" + x + "/f" + y + "/", "fData", encode(chunk.getChunkData(true)));
		zip.addFile("column" + x + "/b" + y + "/", "bData", encode(chunk.getChunkData(false)));
		chunksInQueue.remove(x, y);
	}


	/**
	 * Saves a chunk to disk and flush it from memory
	 */
	public static void saveAndFlushChunk(final int x, final int y) {
		if (chunksInQueue.get(x, y) == null) {
			GameSaver.saverTasks.add(new Task() {
				@Override
				public void execute() {
					ZipHelper zip = new ZipHelper(GameSaver.savePath + "/world", "/chunkData.zip");
					saveChunk(Topography.chunkMap.get(x).get(y), x, y, zip);
					Topography.chunkMap.get(x).remove(y);
				}
			});
			chunksInQueue.put(x, y, true);
		}
	}
}
