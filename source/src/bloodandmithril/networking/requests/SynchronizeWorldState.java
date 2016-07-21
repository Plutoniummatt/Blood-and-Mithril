package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.biome.BiomeDecider;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.World;

@Copyright("Matthew Peck 2014")
public class SynchronizeWorldState implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3436328783410906576L;


	@Override
	public Responses respond() {
		final Responses responses = new Responses(false);

		for (final World w : Domain.getAllWorlds()) {
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

		/**
		 * 
		 */
		private static final long serialVersionUID = 4694224818215818773L;
		private final float gravity;
		private final Epoch currentEpoch;
		private final int worldId;
		private final Class<? extends BiomeDecider> biomeDecider;

		/** Constructor */
		public SynchronizeWorldStateResponse(final World world) {
			this.worldId = world.getWorldId();
			this.gravity = world.getGravity();
			this.currentEpoch = world.getEpoch();
			this.biomeDecider = world.getBiomeDecider();
		}


		@Override
		public void acknowledge() {
			if (Domain.getWorld(worldId) != null) {
				Domain.getWorld(worldId).setEpoch(currentEpoch);
			} else {
				Domain.addWorld(new World(gravity, currentEpoch, worldId, biomeDecider));
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