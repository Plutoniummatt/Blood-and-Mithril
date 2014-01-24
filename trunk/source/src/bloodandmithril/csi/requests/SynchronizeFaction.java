package bloodandmithril.csi.requests;

import java.util.LinkedList;

import bloodandmithril.character.faction.Faction;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.GameWorld;

/**
 * Synchronizes {@link Faction}s
 *
 * @author Copyright (c) CHP Consulting Ltd. 2014
 */
public class SynchronizeFaction implements Request {


	@Override
	public Responses respond() {
		Responses responses = new Responses(true, new LinkedList<Response>());
		for (Faction faction : GameWorld.factions.values()) {
			responses.responses.add(new SynchronizeFactionResponse(faction));
		}
		responses.responses.add(new ChangeFactionControlPassword.RefreshFactionWindow());
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
			Faction fac = GameWorld.factions.get(faction.factionId);
			if (fac != null) {
				fac.controlPassword = faction.controlPassword;
			} else {
				GameWorld.factions.put(faction.factionId, faction);
			}
		}

		@Override
		public int forClient() {
			return -1;
		}
	}

}