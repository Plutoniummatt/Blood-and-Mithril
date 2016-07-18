package bloodandmithril.networking.requests;

import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.FactionsWindow;

/**
 * {@link Response} to refresh the faction window
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class RefreshFactionWindow implements Response {

	@Inject private transient UserInterface userInterface;

	@Override
	public void acknowledge() {
		for (final Component component : userInterface.getLayeredComponents()) {
			if (component instanceof FactionsWindow) {
				((FactionsWindow) component).refreshWindow();
			}
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