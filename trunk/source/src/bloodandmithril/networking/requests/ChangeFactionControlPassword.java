package bloodandmithril.networking.requests;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.SynchronizeFaction.SynchronizeFactionResponse;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.FactionsWindow;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to change the password to control a {@link Faction}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ChangeFactionControlPassword implements Request {

	private final String newPassword;
	private final int factionId;

	/**
	 * Constructor
	 */
	public ChangeFactionControlPassword(int factionId, String newPassword) {
		this.factionId = factionId;
		this.newPassword = newPassword;
	}

	@Override
	public Responses respond() {
		Domain.getFactions().get(factionId).changeControlPassword(newPassword);

		Responses responses = new Responses(true);
		for (Faction faction : Domain.getFactions().values()) {
			responses.add(new SynchronizeFactionResponse(faction));
		}
		responses.add(new RefreshFactionWindow());

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


	public static class RefreshFactionWindow implements Response {
		@Override
		public void acknowledge() {
			for (Component component : UserInterface.getLayeredComponents()) {
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
}