package bloodandmithril.persistence.world;

import static bloodandmithril.graphics.Graphics.getGdxHeight;
import static bloodandmithril.graphics.Graphics.getGdxWidth;
import static bloodandmithril.persistence.PersistenceUtil.decode;
import static bloodandmithril.world.topography.Topography.convertToChunkCoord;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipFile;

import com.badlogic.gdx.Gdx;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.patterns.GlobalLayers;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.persistence.GameSaver;
import bloodandmithril.persistence.ZipHelper;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Chunk.ChunkData;
import bloodandmithril.world.topography.Topography;

/**
 * Responsible for loading chunks from disk
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class ChunkLoader {

	/** The thread responsible for loading */
	private final Thread loaderThread;

	/** The list of tasks the loader thread must execute */
	public final BlockingQueue<Task> loaderTasks = new ArrayBlockingQueue<>(5000);

	/** The current chunk coordinates that is in the queue to be loaded/generated */
	private final ConcurrentDualKeyHashMap<Integer, Integer, Boolean> chunksInQueue = new ConcurrentDualKeyHashMap<>();

	@Inject private GameSaver gameSaver;

	/**
	 * Constructor
	 */
	public ChunkLoader() {
		loaderThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException("Something has interrupted the chunk loading thread.");
				}
				processItems();
			}
		});

		loaderThread.setName("Loader Thread");
		loaderThread.setDaemon(false);
		loaderThread.start();
	}


	/**
	 * Processes items in the loader thread
	 */
	private void processItems() {
		int processed = 0;

		while(!loaderTasks.isEmpty()) {
			loaderTasks.poll().execute();
			processed++;
		}

		if (processed != 0) {
			Logger.loaderDebug("Loader thread processed " + processed + " items", LogLevel.DEBUG);
		}
	}


	/**
	 * @param chunkX - Chunk x-coord to load
	 * @param chunkY - Chunk y-coord to load
	 *
	 * @return whether or not the load was successful
	 */
	public boolean load(final World world, final int chunkX, final int chunkY, boolean populateChunkMap) {
		if (loaderThread.isAlive()) {
			synchronized (chunksInQueue) {
				if (chunksInQueue.get(chunkX, chunkY) == null && (world.getTopography().getChunkMap().get(chunkX) == null || world.getTopography().getChunkMap().get(chunkX).get(chunkY) == null)) {
					loaderTasks.add(() -> {
						loadSingleChunk(chunkX, chunkY, world, populateChunkMap);
					});
					chunksInQueue.put(chunkX, chunkY, true);
					return true;
				} else {
					return false;
				}
			}
		} else {
			throw new RuntimeException("Something has caused the chunk loading thread to terminate");
		}
	}


	public void loadWorlds() {
		try {
			HashMap<Integer, World> worlds = decode(Gdx.files.local(gameSaver.getSavePath() + "/world/worlds.txt"));
			Domain.setWorlds(worlds);
		} catch (Exception e) {
			Logger.loaderDebug("Failed to load worlds", LogLevel.DEBUG);
		}

		if (!Domain.getWorlds().isEmpty()) {
			for (Entry<Integer, World> world : Domain.getWorlds().entrySet()) {
				world.getValue().setTopography(new Topography(world.getKey()));

				try {
					ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keys = decode(Gdx.files.local(gameSaver.getSavePath() + "/world/world" + Integer.toString(world.getKey()) + "/superStructureKeys.txt"));
					world.getValue().getTopography().getStructures().setSuperStructureKeys(keys);
				} catch (Exception e) {
					Logger.loaderDebug("Failed to load chunk super structure structure keys", LogLevel.DEBUG);
					ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> map = new ConcurrentHashMap<>();
					world.getValue().getTopography().getStructures().setSuperStructureKeys(map);
				}

				try {
					ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> keys = decode(Gdx.files.local(gameSaver.getSavePath() + "/world/world" + Integer.toString(world.getKey()) + "/subStructureKeys.txt"));
					world.getValue().getTopography().getStructures().setSubStructureKeys(keys);
				} catch (Exception e) {
					Logger.loaderDebug("Failed to load chunk sub structure keys", LogLevel.DEBUG);
					ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> map = new ConcurrentHashMap<>();
					world.getValue().getTopography().getStructures().setSubStructureKeys(map);
				}
			}
		}
	}


	/** Loads generation data
	 * @param newGame */
	public synchronized void loadGenerationData() {
		try {
			ConcurrentHashMap<Integer, Structure> structures = decode(Gdx.files.local(gameSaver.getSavePath() + "/world/structures.txt"));
			Structures.setStructures(structures);
		} catch (Exception e) {
			Logger.loaderDebug("Failed to load structures", LogLevel.DEBUG);
		}

		try {
			GlobalLayers.layers = decode(Gdx.files.local(gameSaver.getSavePath() + "/world/layers.txt"));
		} catch (Exception e) {
			Logger.loaderDebug("Failed to load layers", LogLevel.DEBUG);
		}
	}


	/** Loads a single chunk from disk and stores it in the chunkMap */
	private void loadSingleChunk(int chunkX, int chunkY, World world, boolean populateChunkMap) {
		synchronized (chunksInQueue) {
			Logger.loaderDebug("Loading chunk: x=" + chunkX + ", y=" + chunkY, LogLevel.TRACE);

			try {
				ZipFile zipFile = new ZipFile(gameSaver.getSavePath() + "/world/world" + Integer.toString(world.getWorldId()) + "/chunkData.zip");

				boolean newCol = world.getTopography().getChunkMap().get(chunkX) == null;
				HashMap<Integer, Chunk> col = newCol ? new HashMap<Integer, Chunk>() : world.getTopography().getChunkMap().get(chunkX);

				ChunkData fData = decode(ZipHelper.readEntry(zipFile, "column" + chunkX + "/f" + chunkY + "/fData"));
				ChunkData bData = decode(ZipHelper.readEntry(zipFile, "column" + chunkX + "/b" + chunkY + "/bData"));

				Chunk loadedChunk = new Chunk(fData, bData);
				col.put(loadedChunk.getChunkData(true).yChunkCoord, loadedChunk);

				if (newCol) {
					world.getTopography().getChunkMap().putColumn(chunkX, col);
				}
			} catch (Exception e) {
				Logger.loaderDebug("No chunk found on disk, generating new chunk: " + chunkX + ", " + chunkY, LogLevel.DEBUG);
				// If load was unsuccessful, the chunk in question remains null and we
				// generate it.
				world.getGenerator().generate(chunkX, chunkY, world, populateChunkMap);
			}

			// Remove chunk from queue.
			chunksInQueue.remove(chunkX, chunkY);
		}
	}


	/**
	 * @return whether the chunks on screen are generated/loaded
	 */
	public boolean areChunksOnScreenGenerated(Graphics graphics) {
		int camX = (int) graphics.getCam().position.x;
		int camY = (int) graphics.getCam().position.y;

		int bottomLeftX = convertToChunkCoord((float)(camX - getGdxWidth() / 2));
		int bottomLeftY = convertToChunkCoord((float)(camY - getGdxHeight() / 2));
		int topRightX = bottomLeftX + convertToChunkCoord((float) getGdxWidth());
		int topRightY = bottomLeftY + convertToChunkCoord((float) getGdxHeight());

		World activeWorld = Domain.getActiveWorld();

		if (activeWorld == null) {
			return true;
		}

		Topography topography = activeWorld.getTopography();

		if (topography == null) {
			return true;
		}

		for (int chunkX = bottomLeftX - 2; chunkX <= topRightX + 2; chunkX++) {
			for (int chunkY = bottomLeftY - 2; chunkY <= topRightY + 2; chunkY++) {
				if (topography.getChunkMap().get(chunkX) == null || topography.getChunkMap().get(chunkX).get(chunkY) == null) {
					return false;
				}
			}
		}

		return true;
	}
}