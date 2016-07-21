package bloodandmithril.networking.requests;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.SynchronizeFaction.SynchronizeFactionResponse;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to change the password to control a {@link Faction}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ChangeFactionControlPassword implements Request {
	private static final long serialVersionUID = 8651577318551438579L;

	private final String newPassword;
	private final int factionId;

	/**
	 * Constructor
	 */
	public ChangeFactionControlPassword(final int factionId, final String newPassword) {
		this.factionId = factionId;
		this.newPassword = newPassword;
	}

	@Override
	public Responses respond() {
		Domain.getFaction(factionId).changeControlPassword(newPassword);

		final Responses responses = new Responses(true);
		for (final Faction faction : Domain.getFactions().values()) {
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
}