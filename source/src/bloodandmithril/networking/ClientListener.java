package bloodandmithril.networking;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.google.inject.Inject;

import bloodandmithril.core.Threading;
import bloodandmithril.core.Wiring;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.DestroyTile.DestroyTileResponse;
import bloodandmithril.networking.requests.GenerateChunk.GenerateChunkResponse;
import bloodandmithril.networking.requests.SynchronizeIndividual.SynchronizeIndividualResponse;
import bloodandmithril.persistence.world.ChunkLoader;
import bloodandmithril.world.topography.Topography;

public class ClientListener extends Listener {

	@Inject private ChunkLoader chunkLoader;
	@Inject private Threading threading;

	private Client client;

	public ClientListener(Client client) {
		this.client = client;
		Wiring.injector().injectMembers(this);
	}

	@Override
	public void received(Connection connection, final Object object) {
		if (!(object instanceof Responses)) {
			return;
		}

		Responses resp = (Responses) object;

		if (resp.executeInSingleThread()) {
			for (final Response response : resp.getResponses()) {
				if (response.forClient() != -1 && response.forClient() != client.getID()) {
					continue;
				}
				response.acknowledge();
			}
			return;
		}

		for (final Response response : resp.getResponses()) {
			if (response.forClient() != -1 && response.forClient() != client.getID()) {
				continue;
			}
			if (response instanceof GenerateChunkResponse) {
				chunkLoader.loaderTasks.add(
					() -> {
						response.acknowledge();
					}
				);
			} else if (response instanceof SynchronizeIndividualResponse) {
				threading.clientProcessingThreadPool.execute(
					() -> {
						response.acknowledge();
					}
				);
			} else if (response instanceof DestroyTileResponse) {
				Topography.addTask(() -> {
					response.acknowledge();
				});
			} else {
				threading.clientProcessingThreadPool.execute(
					() -> {
						try {
							response.acknowledge();
						} catch (Throwable t) {
							throw new RuntimeException(t);
						}
					}
				);
			}
		}
	}
}
