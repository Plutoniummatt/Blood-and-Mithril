package bloodandmithril.csi;


/**
 * A generic request used for client/server communication
 *
 * @author Matt
 */
public interface Request {
	
	/** Respond to the request */
	public Response respond();
}