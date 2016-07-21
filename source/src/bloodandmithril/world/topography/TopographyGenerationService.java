package bloodandmithril.world.topography;

import static bloodandmithril.graphics.Graphics.getGdxHeight;
import static bloodandmithril.graphics.Graphics.getGdxWidth;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.persistence.world.ChunkProvider;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

/**
 * Responsible for loading/generating chunks for {@link Topography}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TopographyGenerationService {

	@Inject	private ChunkProvider chunkProvider;
	@Inject	private Graphics graphics;

	/**
	 * Generates/Loads any missing chunks
	 */
	public final void loadOrGenerateNullChunksAccordingToPosition(final World world) {
		final float x = graphics.getCam().position.x;
		final float y = graphics.getCam().position.y;
		final Topography topo = world.getTopography();

		final int bottomLeftX = Topography.convertToChunkCoord(x - getGdxWidth() / 2);
		final int bottomLeftY = Topography.convertToChunkCoord(y - getGdxHeight() / 2);
		final int topRightX = bottomLeftX + Topography.convertToChunkCoord((float)getGdxWidth());
		final int topRightY = bottomLeftY + Topography.convertToChunkCoord((float)getGdxHeight());

		for (int chunkX = bottomLeftX - 2; chunkX <= topRightX + 2; chunkX++) {
			for (int chunkY = bottomLeftY - 2; chunkY <= topRightY + 2; chunkY++) {
				if (topo.getChunkMap().get(chunkX) == null || topo.getChunkMap().get(chunkX).get(chunkY) == null) {
					if (ClientServerInterface.isClient() && !ClientServerInterface.isServer()) {
						if (topo.getRequestedForGeneration().get(chunkX, chunkY) == null || !topo.getRequestedForGeneration().get(chunkX, chunkY)) {
							ClientServerInterface.SendRequest.sendGenerateChunkRequest(chunkX, chunkY, world.getWorldId());
							topo.getRequestedForGeneration().put(chunkX, chunkY, true);
						}
					} else {
						loadOrGenerateChunk(world, chunkX, chunkY, true);
					}
				}
			}
		}
	}


	public final boolean loadOrGenerateChunk(final World world, final int chunkX, final int chunkY, final boolean populateChunkMap) {
		//Attempt to load the chunk from disk - If chunk does not exist, it will be generated
		return chunkProvider.provide(Domain.getWorld(world.getWorldId()), chunkX, chunkY, populateChunkMap);
	}
}