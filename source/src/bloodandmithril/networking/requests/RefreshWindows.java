package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.ui.Refreshable;

/**
 * Request to refresh all {@link Refreshable} windows
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class RefreshWindows implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8472945642300077771L;

	@Override
	public Responses respond() {
		final Responses responses = new Responses(false);
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
}