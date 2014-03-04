package bloodandmithril.csi.requests;

import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;

/**
 * {@link Request} send when a client connects
 *
 * @author Matt
 */
public class ClientConnected implements Request {

	private final int id;
	private final String name;

	/**
	 * Constructor
	 */
	public ClientConnected(int id, String name) {
		this.id = id;
		this.name = name;
	}


	@Override
	public Responses respond() {
		ClientServerInterface.connectedPlayers.put(
			id,
			name
		);
		return new Responses(false);
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}
}