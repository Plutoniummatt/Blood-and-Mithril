package bloodandmithril.csi.request;


/**
 * A response sent as a result of a {@link Request}
 *
 * @author Matt
 */
public interface Response {
	
	/** Acknowledges the response */
	public void Acknowledge();
}