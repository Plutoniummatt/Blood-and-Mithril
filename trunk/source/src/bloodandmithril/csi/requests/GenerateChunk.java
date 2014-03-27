package bloodandmithril.csi.requests;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
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
public class GenerateChunk implements Request {

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
		if (!Domain.getWorld(worldId).getTopography().getChunkMap().doesChunkExist(x, y) && Domain.getActiveWorld().getTopography().loadOrGenerateChunk(x, y)) {
			Response response = null;

			do {
				if (Domain.getWorld(worldId).getTopography().getChunkMap().doesChunkExist(x, y)) {
					ChunkData fData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(true);
					ChunkData bData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(false);
					response = new GenerateChunkResponse(fData, bData, worldId);
				}
			} while (!Domain.getWorld(worldId).getTopography().getChunkMap().doesChunkExist(x, y) || response == null);

			Responses responses = new Response.Responses(false);
			responses.add(response);
			return responses;
		} else {
			ChunkData fData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(true);
			ChunkData bData = Domain.getWorld(worldId).getTopography().getChunkMap().get(x).get(y).getChunkData(false);
			Response response = new GenerateChunkResponse(fData, bData, worldId);
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