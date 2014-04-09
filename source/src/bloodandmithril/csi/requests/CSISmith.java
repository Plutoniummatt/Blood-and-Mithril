package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Smith;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.furniture.Anvil;
import bloodandmithril.world.Domain;

/**
 * Request to tell the server that an {@link Individual} would like to {@link TradeWith} something.
 */
public class CSISmith implements Request {

	private final int anvilId;
	private final int individualId;
	private final int connectionId;

	/**
	 * Constructor
	 */
	public CSISmith(int individualId, int anvilId, int connectionId) {
		this.individualId = individualId;
		this.anvilId = anvilId;
		this.connectionId = connectionId;
	}


	@Override
	public Responses respond() {
		Responses response = new Response.Responses(false);

		Individual smith = Domain.getIndividuals().get(individualId);
		Prop prop = Domain.getProps().get(anvilId);
		if (prop instanceof Anvil) {
			smith.getAI().setCurrentTask(
				new Smith(smith, (Anvil)prop, connectionId)
			);
		}

		return response;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}


	public static class NotifyOpenAnvilWindow implements Response {

		private final int smithId;
		private final int anvilId;

		public NotifyOpenAnvilWindow(int smithId, int anvilId) {
			this.smithId = smithId;
			this.anvilId = anvilId;
		}

		@Override
		public void acknowledge() {
			Prop prop = Domain.getProps().get(anvilId);
			if (prop instanceof Anvil) {
				Smith.openAnvilWindow(Domain.getIndividuals().get(smithId), (Anvil)prop);
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