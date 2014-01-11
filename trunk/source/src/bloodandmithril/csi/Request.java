package bloodandmithril.csi;

import bloodandmithril.csi.Response.Responses;


/**
 * A generic request used for client/server communication
 *
 * @author Matt
 */
public interface Request {

	/** Respond to the request */
	public Responses respond();

	/** Whether this request uses the TCP protocol */
	public boolean tcp();

	/** Whether the server needs to notify all connected clients when responding */
	public boolean notifyOthers();
}