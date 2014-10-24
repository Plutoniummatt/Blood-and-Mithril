package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.PlantSeed;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.plant.seed.SeedProp;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} for an {@link Individual} to plant a {@link SeedProp}
 *
 * @author Matt
 */
public class RequestPlantSeed implements Request {
	
	int planterId;
	SeedProp toPlant;
	
	/**
	 * Constructor
	 */
	public RequestPlantSeed(Individual planter, SeedProp seed) {
		this.planterId = planter.getId().getId();
		this.toPlant = seed;
	}
	

	@Override
	public Responses respond() {
		Individual planter = Domain.getIndividuals().get(planterId);
		((Individual) planter).getAI().setCurrentTask(new PlantSeed((Individual) planter, toPlant));
		
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