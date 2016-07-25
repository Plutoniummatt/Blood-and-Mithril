package bloodandmithril.networking.requests;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.task.placeprop.PlaceProp;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.PropItem;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.PropPlacementService;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * {@link Request} to place a prop
 *
 * @author Matt
 */
@Copyright("Matthew Peck")
public class PlacePropRequest implements Request {
	private static final long serialVersionUID = -3329786910978246100L;
	
	@Inject private transient PropPlacementService propPlacementService;
	
	private final Prop prop;
	private final int worldId;
	private final float x;
	private final float y;
	private final Integer individualId;
	private final PropItem propItem;

	/**
	 * Constructor
	 * @param i 
	 */
	public PlacePropRequest(PropItem propItem, Integer individualId, Prop prop, float x, float y, int worldId) {
		this.propItem = propItem;
		this.individualId = individualId;
		this.prop = prop;
		this.x = x;
		this.y = y;
		this.worldId = worldId;
	}


	@Override
	public Responses respond() {
		boolean canPlace = false;
		try {
			canPlace = propPlacementService.canPlaceAt(prop, new Vector2(x, y));
		} catch (NoTileFoundException e) {}
		if (canPlace) {
			if (individualId == null) {
				Domain.getWorld(worldId).props().addProp(prop);
			} else {
				Individual individual = Domain.getIndividual(individualId);
				individual.getAI().setCurrentTask(new PlaceProp(individual, prop.position, propItem));
			}
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