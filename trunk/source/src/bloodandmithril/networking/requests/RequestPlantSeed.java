package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.PlantSeed;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.plant.seed.Seed;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} for an {@link Individual} to plant a {@link Seed}
 *
 * @author Matt
 */
public class RequestPlantSeed implements Request {
	
	int planterId;
	Seed toPlant;
	
	/**
	 * Constructor
	 */
	public RequestPlantSeed(Individual planter, Seed seed) {
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