package bloodandmithril.persistence.world;

import static bloodandmithril.persistence.PersistenceUtil.decode;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.zip.ZipFile;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.ChunkGenerator;
import bloodandmithril.persistence.PersistenceParameters;
import bloodandmithril.persistence.ZipHelper;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Task;
import bloodandmithril.util.datastructure.ConcurrentDualKeyHashMap;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Chunk.ChunkData;

/**
 * Responsible for loading chunks from disk, or generating it if can't be loaded
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class ChunkProvider {

	/** The thread responsible for loading */
	private final Thread loaderThread;

	/** The list of tasks the loader thread must execute */
	public final BlockingQueue<Task> loaderTasks = new ArrayBlockingQueue<>(5000);

	/** The current chunk coordinates that is in the queue to be loaded/generated */
	private final ConcurrentDualKeyHashMap<Integer, Integer, Boolean> chunksInQueue = new ConcurrentDualKeyHashMap<>();

	private final PersistenceParameters persistenceParameters;
	private final ChunkGenerator chunkGenerator;
	
	/**
	 * Constructor
	 */
	@Inject
	public ChunkProvider(PersistenceParameters persistenceParameters, ChunkGenerator chunkGenerator) {
		this.persistenceParameters = persistenceParameters;
		this.chunkGenerator = chunkGenerator;
		loaderThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
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
	 * Attempts to laod the chunk from disk, if loading fails, the chunk is generated
	 * 
	 * @param chunkX - Chunk x-coord to load
	 * @param chunkY - Chunk y-coord to load
	 *
	 * @return whether or not the load was successful
	 */
	public boolean provide(final World world, final int chunkX, final int chunkY, final boolean populateChunkMap) {
		if (loaderThread.isAlive()) {
			synchronized (chunksInQueue) {
				if (chunksInQueue.get(chunkX, chunkY) == null && (world.getTopography().getChunkMap().get(chunkX) == null || world.getTopography().getChunkMap().get(chunkX).get(chunkY) == null)) {
					loaderTasks.add(() -> {
						provideSingleChunk(chunkX, chunkY, world, populateChunkMap);
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


	/** Loads a single chunk from disk and stores it in the chunkMap, or generates it if can't be loaded */
	public void provideSingleChunk(final int chunkX, final int chunkY, final World world, final boolean populateChunkMap) {
		synchronized (chunksInQueue) {
			Logger.loaderDebug("Loading chunk: x=" + chunkX + ", y=" + chunkY, LogLevel.TRACE);

			try {
				final ZipFile zipFile = new ZipFile(persistenceParameters.getSavePath() + "/world/world" + Integer.toString(world.getWorldId()) + "/chunkData.zip");

				final boolean newCol = world.getTopography().getChunkMap().get(chunkX) == null;
				final HashMap<Integer, Chunk> col = newCol ? new HashMap<Integer, Chunk>() : world.getTopography().getChunkMap().get(chunkX);

				final ChunkData fData = decode(ZipHelper.readEntry(zipFile, "column" + chunkX + "/f" + chunkY + "/fData"));
				final ChunkData bData = decode(ZipHelper.readEntry(zipFile, "column" + chunkX + "/b" + chunkY + "/bData"));

				final Chunk loadedChunk = new Chunk(fData, bData);
				col.put(loadedChunk.getChunkData(true).yChunkCoord, loadedChunk);

				if (newCol) {
					world.getTopography().getChunkMap().putColumn(chunkX, col);
				}
			} catch (final Exception e) {
				Logger.loaderDebug("No chunk found on disk, generating new chunk: " + chunkX + ", " + chunkY, LogLevel.DEBUG);
				// If load was unsuccessful, the chunk in question remains null and we
				// generate it.
				chunkGenerator.generate(chunkX, chunkY, world, populateChunkMap);
			}

			// Remove chunk from queue.
			chunksInQueue.remove(chunkX, chunkY);
		}
	}
}