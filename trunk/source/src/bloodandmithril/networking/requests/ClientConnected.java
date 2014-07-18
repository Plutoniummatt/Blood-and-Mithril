package bloodandmithril.networking.requests;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;

/**
 * {@link Request} send when a client connects
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
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