package bloodandmithril.csi.requests;

import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} to change the temperature of a {@link Furnace}
 *
 * @author Matt
 */
public class ChangeFurnaceTemperature implements Request {

	private int propId;
	private float newTemp;

	/**
	 * Constructor
	 */
	public ChangeFurnaceTemperature(int propId, float newTemp) {
		this.propId = propId;
		this.newTemp = newTemp;
	}
	
	
	@Override
	public Responses respond() {
		Prop maybeFurnace = Domain.getProps().get(propId);
		if (maybeFurnace instanceof Furnace) {
			((Furnace) maybeFurnace).setCombustionDurationRemaining(((Furnace) maybeFurnace).getCombustionDurationRemaining() * (((Furnace) maybeFurnace).getTemperature() / newTemp));
			((Furnace) maybeFurnace).setTemperature(newTemp);
		} else {
			throw new RuntimeException("Expected a furnace, but got a " + maybeFurnace.getClass().getSimpleName());
		}
		
		return new Responses(false);
	}

	
	@Override
	public boolean tcp() {
		return false;
	}

	
	@Override
	public boolean notifyOthers() {
		return false;
	}
}