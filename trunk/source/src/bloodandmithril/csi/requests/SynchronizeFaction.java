package bloodandmithril.csi.requests;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * Synchronizes {@link Faction}s
 *
 * @author Copyright (c) CHP Consulting Ltd. 2014
 */
public class SynchronizeFaction implements Request {


	@Override
	public Responses respond() {
		Responses responses = new Responses(true);
		for (Faction faction : Domain.getFactions().values()) {
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
		public SynchronizeFactionResponse(Faction faction) {
			this.faction = faction;
		}

		@Override
		public void acknowledge() {
			Faction fac = Domain.getFactions().get(faction.factionId);
			if (fac != null) {
				fac.controlPassword = faction.controlPassword;
			} else {
				Domain.getFactions().put(faction.factionId, faction);
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