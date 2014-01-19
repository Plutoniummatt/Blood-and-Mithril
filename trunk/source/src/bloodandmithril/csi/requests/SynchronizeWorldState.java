package bloodandmithril.csi.requests;

import java.util.LinkedList;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.Epoch;
import bloodandmithril.world.WorldState;

public class SynchronizeWorldState implements Request {

	@Override
	public Responses respond() {
		Responses responses = new Responses(false, new LinkedList<Response>());
		responses.responses.add(new SynchronizeWorldStateResponse(WorldState.currentEpoch));
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
			WorldState.currentEpoch = currentEpoch;
		}

		
		@Override
		public int forClient() {
			return -1;
		}
	}
}