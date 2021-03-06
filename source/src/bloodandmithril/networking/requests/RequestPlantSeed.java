package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.plantseed.PlantSeed;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.plant.seed.SeedProp;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A {@link Request} for an {@link Individual} to plant a {@link SeedProp}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class RequestPlantSeed implements Request {

	/**
	 *
	 */
	private static final long serialVersionUID = -5041237335745201360L;
	int planterId;
	SeedProp toPlant;

	/**
	 * Constructor
	 */
	public RequestPlantSeed(final Individual planter, final SeedProp seed) {
		this.planterId = planter.getId().getId();
		this.toPlant = seed;
	}


	@Override
	public Responses respond() {
		final Individual planter = Domain.getIndividual(planterId);
		try {
			planter.getAI().setCurrentTask(new PlantSeed(planter, toPlant));
		} catch (final NoTileFoundException e) {}

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