package bloodandmithril.csi.requests;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to begin smelting of the contents of a {@link Furnace}
 *
 * @author Matt
 */
public class FurnaceSmelt implements Request {

	private int furnaceId;

	/**
	 * Constructor
	 */
	public FurnaceSmelt(int furnaceId) {
		this.furnaceId = furnaceId;
	}
	
	
	@Override
	public Responses respond() {
		Prop prop = Domain.getProps().get(furnaceId);
		if (prop instanceof Furnace) {
			((Furnace) prop).smelt();
		} else {
			throw new RuntimeException("Expected a furnace, but got a " + prop.getClass().getSimpleName());
		}
		
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