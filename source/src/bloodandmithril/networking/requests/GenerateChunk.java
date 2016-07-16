package bloodandmithril.networking.requests;

import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Chunk.ChunkData;
import bloodandmithril.world.topography.TopographyGenerationService;

/**
 * Sends a {@link Request} to generate a {@link Chunk}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class GenerateChunk implements Request {
	
	@Inject
	private transient TopographyGenerationService topographyGenerationService;

	/** Chunk coordinates */
	private final int x;
	private final int y;

	/** {@link World} id */
	private int worldId;

	/**
	 * Constructor
	 */
	public GenerateChunk(int x, int y, int worldId) {
		this.x = x;
		this.y = y;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		if (Domain.getWorld(worldId).getTopography().getChunkMap().doesChunkExist(x, y)) {
			// Chunk already generated and in memory
			ChunkData fData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(true);
			ChunkData bData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(false);
			Response response = new GenerateChunkResponse(fData, bData, worldId);
			Responses responses = new Response.Responses(false);
			responses.add(response);
			return responses;
		} else {
			// Chunk does not exist on chunk map, attempt to load/generate
			Response response = null;
			topographyGenerationService.loadOrGenerateChunk(Domain.getWorld(worldId), x, y, true);

			do {
				if (Domain.getWorld(worldId).getTopography().getChunkMap().doesChunkExist(x, y)) {
					ChunkData fData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(true);
					ChunkData bData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(false);
					response = new GenerateChunkResponse(fData, bData, worldId);
				} else {
					try {
						Thread.sleep(50);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			} while (!Domain.getWorld(worldId).getTopography().getChunkMap().doesChunkExist(x, y) || response == null);

			Responses responses = new Response.Responses(false);
			responses.add(response);
			return responses;
		}
	}


	public static class GenerateChunkResponse implements Response {

		/** Chunk coordinates */
		private final ChunkData fData;
		private final ChunkData bData;
		private int worldId;

		/**
		 * Constructor
		 */
		public GenerateChunkResponse(ChunkData fData, ChunkData bData, int worldId) {
			this.fData = fData;
			this.bData = bData;
			this.worldId = worldId;
		}

		@Override
		public void acknowledge() {
			Chunk received = new Chunk(fData, bData);
			Domain.getWorld(worldId).getTopography().getChunkMap().addChunk(fData.xChunkCoord, fData.yChunkCoord, received);
			Logger.networkDebug("Received chunk: [" + fData.xChunkCoord + "," + fData.yChunkCoord +"]" , LogLevel.INFO);
		}

		@Override
		public int forClient() {
			return -1;
		}

		@Override
		public void prepare() {
			Logger.networkDebug("Preparing to send generated chunk response at (" + fData.xChunkCoord + ", " + fData.yChunkCoord + ")", LogLevel.DEBUG);
		}
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}
}