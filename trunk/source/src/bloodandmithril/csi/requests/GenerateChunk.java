package bloodandmithril.csi.requests;

import java.util.List;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.GameWorld;
import bloodandmithril.world.topography.Chunk;
import bloodandmithril.world.topography.Chunk.ChunkData;
import bloodandmithril.world.topography.Topography;

import com.google.common.collect.Lists;

/**
 * Sends a {@link Request} to generate a {@link Chunk}
 *
 * @author Matt
 */
public class GenerateChunk implements Request {

	/** Chunk coordinates */
	private final int x;
	private final int y;

	/**
	 * Constructor
	 */
	public GenerateChunk(int x, int y) {
		this.x = x;
		this.y = y;
	}


	@Override
	public List<Response> respond() {
		if (!Topography.chunkMap.doesChunkExist(x, y) && GameWorld.topography.loadOrGenerateChunk(x, y)) {
			Response response = null;

			do {
				if (Topography.chunkMap.doesChunkExist(x, y)) {
					ChunkData fData = Topography.chunkMap.get(x).get(y).getChunkData(true);
					ChunkData bData = Topography.chunkMap.get(x).get(y).getChunkData(false);
					response = new GenerateChunkResponse(fData, bData);
				}
			} while (!Topography.chunkMap.doesChunkExist(x, y) || response == null);

			return Lists.newArrayList(response);
		} else {
			ChunkData fData = Topography.chunkMap.get(x).get(y).getChunkData(true);
			ChunkData bData = Topography.chunkMap.get(x).get(y).getChunkData(false);
			Response response = new GenerateChunkResponse(fData, bData);
			return Lists.newArrayList(response);
		}
	}


	public static class GenerateChunkResponse implements Response {

		/** Chunk coordinates */
		private final ChunkData fData;
		private final ChunkData bData;

		/**
		 * Constructor
		 */
		public GenerateChunkResponse(ChunkData fData, ChunkData bData) {
			this.fData = fData;
			this.bData = bData;
		}

		@Override
		public void acknowledge() {
			Chunk received = new Chunk(fData, bData);
			Topography.chunkMap.addChunk(fData.xChunkCoord, fData.yChunkCoord, received);
			Logger.networkDebug("Received chunk: [" + fData.xChunkCoord + "," + fData.yChunkCoord +"]" , LogLevel.INFO);
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