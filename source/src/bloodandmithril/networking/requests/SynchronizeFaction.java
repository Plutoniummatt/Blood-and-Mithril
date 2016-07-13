package bloodandmithril.networking.requests;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * Synchronizes {@link Faction}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class SynchronizeFaction implements Request {


	@Override
	public Responses respond() {
		final Responses responses = new Responses(true);
		for (final Faction faction : Domain.getFactions().values()) {
			responses.add(new SynchronizeFactionResponse(faction));
		}
		responses.add(new ChangeFactionControlPassword.RefreshFactionWindow());
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


	public static class SynchronizeFactionResponse implements Response {
		private final Faction faction;

		/** Constructor */
		public SynchronizeFactionResponse(final Faction faction) {
			this.faction = faction;
		}

		@Override
		public void acknowledge() {
			final Faction fac = Domain.getFaction(faction.factionId);
			if (fac != null) {
				fac.controlPassword = faction.controlPassword;
			} else {
				Domain.addFaction(faction);
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