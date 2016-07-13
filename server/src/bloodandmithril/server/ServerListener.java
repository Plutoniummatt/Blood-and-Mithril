package bloodandmithril.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
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
	public ServerListener(final Server server) {
		this.server = server;
	}


	@Override
	public void disconnected (final Connection connection) {
		ClientServerInterface.SendNotification.notifySyncPlayerList();

		for (final Individual indi : Domain.getIndividuals()) {
			if (indi.getSelectedByClient().remove(connection.getID())) {
				indi.deselect(false, connection.getID());
			}
		}
	}


	@Override
	public void received(final Connection connection, final Object object) {
		// Bit of a crutch, but meh...
		Wiring.injector().injectMembers(object);

		if (object instanceof Request) {
			ClientServerInterface.serverThread.execute(() -> {
				// Cast to Request
				final Request request = (Request) object;

				// Send response
				if (request.tcp()) {
					final Responses responseToSend = request.respond();
					for (final Response response : responseToSend.getResponses()) {
						response.prepare();
					}
					if (request.notifyOthers()) {
						for (final Connection c : server.getConnections()) {
							c.sendTCP(responseToSend);
						}
					} else {
						connection.sendTCP(responseToSend);
					}
				} else {
					final Responses responseToSend = request.respond();
					for (final Response response : responseToSend.getResponses()) {
						response.prepare();
					}
					if (request.notifyOthers()) {
						for (final Connection c : server.getConnections()) {
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