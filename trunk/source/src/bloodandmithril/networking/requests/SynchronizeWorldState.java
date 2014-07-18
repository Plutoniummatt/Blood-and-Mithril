package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.WorldState;

@Copyright("Matthew Peck 2014")
public class SynchronizeWorldState implements Request {

	@Override
	public Responses respond() {
		Responses responses = new Responses(false);
		responses.add(new SynchronizeWorldStateResponse(WorldState.getCurrentEpoch()));
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

		private Epoch currentEpoch;

		/** Constructor */
		public SynchronizeWorldStateResponse(Epoch currentEpoch) {
			this.currentEpoch = currentEpoch;
		}


		@Override
		public void acknowledge() {
			WorldState.setCurrentEpoch(currentEpoch);
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