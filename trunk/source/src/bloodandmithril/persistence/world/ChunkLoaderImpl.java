package bloodandmithril.persistence.world;

import static bloodandmithril.persistence.PersistenceUtil.decode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.zip.ZipFile;

import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.TerrainGenerator;
import bloodandmithril.generation.patterns.Layers;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.ZipHelper;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Chunk.ChunkData;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.Gdx;


/**
 * An implementation of {@link ChunkLoader} using {@link Serializable}
 *
 * @author Matt
 */
public class ChunkLoaderImpl implements ChunkLoader {

	/** The thread responsible for loading */
	private final Thread loaderThread;

	/** The list of tasks the loader thread must execute */
	public static final BlockingQueue<Task> loaderTasks = new ArrayBlockingQueue<Task>(5000);

	/** The terrain generator */
	private final TerrainGenerator generator = new TerrainGenerator();

	/** The current chunk coordinates that is in the queue to be loaded/generated */
	private final ConcurrentDualKeyHashMap<Integer, Integer, Boolean> chunksInQueue = new ConcurrentDualKeyHashMap<Integer, Integer, Boolean>();

	/**
	 * Constructor
	 */
	public ChunkLoaderImpl() {
		loaderThread = new Thread(new Runnable() {

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
				if (loaderTasks.isEmpty()) {
					if (n != 0) {
						Logger.loaderDebug("Loader thread processed " + n + " items", LogLevel.DEBUG);
					}
				} else {
					loaderTasks.poll().execute();
					processItems(n + 1);
				}
			}
		});

		loaderThread.setName("Loader Thread");
		loaderThread.setDaemon(false);
		loaderThread.start();
	}


	@Override
	public boolean load(final Topography topography, final int chunkX, final int chunkY) {
		if (loaderThread.isAlive()) {
			if (chunksInQueue.get(chunkX, chunkY) == null) {
				loaderTasks.add(new Task() {
					@Override
					public void execute() {
						loadSingleChunk(chunkX, chunkY);
					}
				});
				chunksInQueue.put(chunkX, chunkY, true);
				return true;
			} else {
				return false;
			}
		} else {
			throw new RuntimeException("Something has caused the chunk loading thread to terminate");
		}
	}


	/** Loads generation data */
	public static void loadGenerationData() {

		if (Structures.getSuperStructureKeys() == null) {
			try {
				ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keys = decode(Gdx.files.local(GameSaver.savePath + "/world/superStructureKeys.txt"));
				Structures.setSuperStructureKeys(keys);
			} catch (Exception e) {
				Logger.loaderDebug("Failed to load chunk super structure structure keys", LogLevel.WARN);
				ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> map = new ConcurrentHashMap<>();
				Structures.setSuperStructureKeys(map);
			}
		}

		if (Structures.getSubStructureKeys() == null) {
			try {
				ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keys = decode(Gdx.files.local(GameSaver.savePath + "/world/subStructureKeys.txt"));
				Structures.setSubStructureKeys(keys);
			} catch (Exception e) {
				Logger.loaderDebug("Failed to load chunk sub structure keys", LogLevel.WARN);
				ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> map = new ConcurrentHashMap<>();
				Structures.setSubStructureKeys(map);
			}
		}

		if (Structures.getStructures() == null) {
			try {
				ConcurrentHashMap<Integer, Structure> structures = decode(Gdx.files.local(GameSaver.savePath + "/world/structures.txt"));
				Structures.setStructures(structures);
			} catch (Exception e) {
				Logger.loaderDebug("Failed to load structures", LogLevel.WARN);
				Structures.setStructures(new ConcurrentHashMap<Integer, Structure>());
			}
		}

		if (Structures.getSurfaceHeight() == null) {
			try {
				HashMap<Integer, Integer> keys = decode(Gdx.files.local(GameSaver.savePath + "/world/surfaceHeight.txt"));
				Structures.setSurfaceHeight(keys);
			} catch (Exception e) {
				Logger.loaderDebug("Failed to load surface height", LogLevel.WARN);
				HashMap<Integer, Integer> map = new HashMap<>();
				Structures.setSurfaceHeight(map);
			}
		}

		if (Layers.layers == null) {
			try {
				Layers.layers = decode(Gdx.files.local(GameSaver.savePath + "/world/layers.txt"));
			} catch (Exception e) {
				Logger.loaderDebug("Failed to load layers", LogLevel.WARN);
				Layers.layers = new ConcurrentSkipListMap<Integer, TwoInts>();
			}
		}
	}


	/** Loads a single chunk from disk and stores it in the chunkMap */
	private void loadSingleChunk(int chunkX, int chunkY) {
		Logger.loaderDebug("Loading chunk: x=" + chunkX + ", y=" + chunkY, LogLevel.DEBUG);

		try {
			ZipFile zipFile = new ZipFile(GameSaver.savePath + "/world/chunkData.zip");

			boolean newCol = Topography.chunkMap.get(chunkX) == null;
			ConcurrentHashMap<Integer, Chunk> col = newCol ? new ConcurrentHashMap<Integer, Chunk>() : Topography.chunkMap.get(chunkX);

			ChunkData fData = decode(ZipHelper.readEntry(zipFile, "column" + chunkX + "/f" + chunkY + "/fData"));
			ChunkData bData = decode(ZipHelper.readEntry(zipFile, "column" + chunkX + "/b" + chunkY + "/bData"));

			Chunk loadedChunk = new Chunk(fData, bData);
			col.put(loadedChunk.getChunkData(true).yChunkCoord, loadedChunk);

			if (newCol) {
				Topography.chunkMap.putColumn(chunkX, col);
			}
		} catch (Exception e) {
			Logger.loaderDebug("No chunk found on disk, generating new chunk: " + chunkX + ", " + chunkY, LogLevel.DEBUG);
			// If load was unsuccessful, the chunk in question remains null and we
			// generate it.
			generator.generate(chunkX, chunkY);
		}

		// Remove chunk from queue.
		chunksInQueue.remove(chunkX, chunkY);
	}
}