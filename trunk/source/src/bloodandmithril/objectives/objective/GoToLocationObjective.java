package bloodandmithril.objectives.objective;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.objectives.Objective;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

/**
 * An {@link Objective} for an {@link Individual} to go to a location, within tolerance
 *
 * @author Matt
 */
@Copyright("Matthew Peck")
public class GoToLocationObjective implements Objective {

	private SerializableMappingFunction<Individual, Boolean> individualIdentificationFunction;
	private SerializableFunction<Boolean> failureFunction;
	private SerializableFunction<Vector2> location;
	private float tolerance;
	private int worldId;
	private String title;

	/**
	 * Constructor
	 */
	public GoToLocationObjective(
			SerializableMappingFunction<Individual, Boolean> individualIdentificationFunction,
			SerializableFunction<Vector2> location,
			float tolerance,
			int worldId,
			SerializableFunction<Boolean> failureFunction,
			String title) {
		this.individualIdentificationFunction = individualIdentificationFunction;
		this.location = location;
		this.tolerance = tolerance;
		this.worldId = worldId;
		this.failureFunction = failureFunction;
		this.title = title;
	}


	@Override
	public int getWorldId() {
		return worldId;
	}


	@Override
	public void renderHints() {
	}


	@Override
	public String getTitle() {
		return title;
	}


	@Override
	public ObjectiveStatus getStatus() {
		if (failureFunction.call()) {
			return ObjectiveStatus.FAILED;
		}

		for (Individual individual : Domain.getWorld(worldId).getPositionalIndexMap().getNearbyEntities(Individual.class, location.call())) {
			if (individualIdentificationFunction.apply(individual)) {
				if (individual.getState().position.cpy().sub(location.call().cpy()).len() <= tolerance) {
					return ObjectiveStatus.COMPLETE;
				}
			}
		};

		return ObjectiveStatus.ACTIVE;
	}
}