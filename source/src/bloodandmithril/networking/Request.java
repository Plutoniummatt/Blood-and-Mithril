package bloodandmithril.networking;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Response.Responses;


/**
 * A generic request used for client/server communication
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Request extends Serializable {

	/** Respond to the request */
	public Responses respond();

	/** Whether this request uses the TCP protocol */
	public boolean tcp();

	/** Whether the server needs to notify all connected clients when responding */
	public boolean notifyOthers();
}