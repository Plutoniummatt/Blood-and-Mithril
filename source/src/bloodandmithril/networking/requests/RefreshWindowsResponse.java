package bloodandmithril.networking.requests;

import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;
import bloodandmithril.ui.UserInterface;

/**
 * {@link Response} to acknowledge and refresh windows
 *
 * @author Matt
 */
@Copyright("Matthew Peck")
public class RefreshWindowsResponse implements Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 898090512872543483L;
	@Inject private transient UserInterface userInterface;

	@Override
	public void acknowledge() {
		userInterface.refreshRefreshableWindows();
	}

	@Override
	public int forClient() {
		return -1;
	}

	@Override
	public void prepare() {
	}
}
