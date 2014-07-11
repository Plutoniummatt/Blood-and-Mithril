package bloodandmithril.networking.requests;

import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.ui.Refreshable;
import bloodandmithril.ui.UserInterface;

/**
 * Request to refresh all {@link Refreshable} windows
 *
 * @author Matt
 */
public class RefreshWindows implements Request {

	@Override
	public Responses respond() {
		Responses responses = new Responses(false);
		responses.add(new RefreshWindowsResponse());
		return responses;
	}

	@Override
	public boolean tcp() {
		return true;
	}

	@Override
	public boolean notifyOthers() {
		return true;
	}


	public static class RefreshWindowsResponse implements Response {
		@Override
		public void acknowledge() {
			UserInterface.refreshRefreshableWindows();
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