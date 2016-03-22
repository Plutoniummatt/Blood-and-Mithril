package bloodandmithril.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;

/**
 * Server specific {@link Listener}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class ServerListener extends Listener {

	private final Server server;

	/**
	 * Constructor
	 */
	public ServerListener(Server server) {
		this.server = server;
	}


	@Override
	public void disconnected (Connection connection) {
		ClientServerInterface.SendNotification.notifySyncPlayerList();

		for (Individual indi : Domain.getIndividuals().values()) {
			if (indi.getSelectedByClient().remove(connection.getID())) {
				indi.deselect(false, connection.getID());
			}
		}
	}


	@Override
	public void received(final Connection connection, final Object object) {
		if (object instanceof Request) {
			ClientServerInterface.serverThread.execute(() -> {
				// Cast to Request
				Request request = (Request) object;

				// Send response
				if (request.tcp()) {
					Responses responseToSend = request.respond();
					for (Response response : responseToSend.getResponses()) {
						response.prepare();
					}
					if (request.notifyOthers()) {
						for (Connection c : server.getConnections()) {
							c.sendTCP(responseToSend);
						}
					} else {
						connection.sendTCP(responseToSend);
					}
				} else {
					Responses responseToSend = request.respond();
					for (Response response : responseToSend.getResponses()) {
						response.prepare();
					}
					if (request.notifyOthers()) {
						for (Connection c : server.getConnections()) {
							c.sendUDP(responseToSend);
						}
					} else {
						connection.sendUDP(responseToSend);
					}
					Logger.networkDebug("Responding to " + request.getClass().getSimpleName() + " from " + connection.getRemoteAddressTCP(), LogLevel.TRACE);
				}
			});
		}
	}
}