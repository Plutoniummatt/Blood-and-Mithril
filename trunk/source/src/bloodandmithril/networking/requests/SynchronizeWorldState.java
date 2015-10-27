package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.ChunkGenerator;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.World;

@Copyright("Matthew Peck 2014")
public class SynchronizeWorldState implements Request {

	@Override
	public Responses respond() {
		Responses responses = new Responses(false);

		for (World w : Domain.getWorlds().values()) {
			responses.add(new SynchronizeWorldStateResponse(w));
		}

		return responses;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}


	public static class SynchronizeWorldStateResponse implements Response {

		private final float gravity;
		private final Epoch currentEpoch;
		private final ChunkGenerator chunkGenerator;
		private final int worldId;

		/** Constructor */
		public SynchronizeWorldStateResponse(World world) {
			this.worldId = world.getWorldId();
			this.gravity = world.getGravity();
			this.currentEpoch = world.getEpoch();
			this.chunkGenerator = world.getGenerator();
		}


		@Override
		public void acknowledge() {
			if (Domain.getWorlds().containsKey(worldId)) {
				Domain.getWorld(worldId).setEpoch(currentEpoch);
			} else {
				Domain.getWorlds().put(worldId, new World(gravity, currentEpoch, chunkGenerator, worldId));
			}
		}


		@Override
		public int forClient() {
			return -1;
		}


		@Override
		public void prepare() {
		}
	}
}