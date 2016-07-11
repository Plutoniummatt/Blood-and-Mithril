package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Chunk.ChunkData;

/**
 * Sends a {@link Request} to generate a {@link Chunk}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class GenerateChunk implements Request {

	/** Chunk coordinates */
	private final int x;
	private final int y;

	/** {@link World} id */
	private int worldId;

	/**
	 * Constructor
	 */
	public GenerateChunk(final int x, final int y, final int worldId) {
		this.x = x;
		this.y = y;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		if (Domain.getWorld(worldId).getTopography().getChunkMap().doesChunkExist(x, y)) {
			// Chunk already generated and in memory
			final ChunkData fData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(true);
			final ChunkData bData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(false);
			final Response response = new GenerateChunkResponse(fData, bData, worldId);
			final Responses responses = new Response.Responses(false);
			responses.add(response);
			return responses;
		} else {
			// Chunk does not exist on chunk map, attempt to load/generate
			Response response = null;
			Wiring.injector().getInstance(ChunkLoader.class).load(Domain.getWorld(worldId), x, y, true);

			do {
				if (Domain.getWorld(worldId).getTopography().getChunkMap().doesChunkExist(x, y)) {
					final ChunkData fData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(true);
					final ChunkData bData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(false);
					response = new GenerateChunkResponse(fData, bData, worldId);
				} else {
					try {
						Thread.sleep(50);
					} catch (final Exception e) {
						throw new RuntimeException(e);
					}
				}
			} while (!Domain.getWorld(worldId).getTopography().getChunkMap().doesChunkExist(x, y) || response == null);

			final Responses responses = new Response.Responses(false);
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
		public GenerateChunkResponse(final ChunkData fData, final ChunkData bData, final int worldId) {
			this.fData = fData;
			this.bData = bData;
			this.worldId = worldId;
		}

		@Override
		public void acknowledge() {
			final Chunk received = new Chunk(fData, bData);
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