package bloodandmithril.objectives.objective;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.event.Event;
import bloodandmithril.objectives.Objective;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.Domain;

/**
 * An {@link Objective} for an {@link Individual} to go to a location, within tolerance
 *
 * @author Matt
 */
@Copyright("Matthew Peck")
public class GoToLocationObjective implements Objective {
	private static final long serialVersionUID = -8800497202398295512L;

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
			final SerializableMappingFunction<Individual, Boolean> individualIdentificationFunction,
			final SerializableFunction<Vector2> location,
			final float tolerance,
			final int worldId,
			final SerializableFunction<Boolean> failureFunction,
			final String title) {
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

		for (final Individual individual : Domain.getWorld(worldId).getPositionalIndexMap().getNearbyEntities(Individual.class, location.call())) {
			if (individualIdentificationFunction.apply(individual)) {
				if (individual.getState().position.cpy().sub(location.call().cpy()).len() <= tolerance) {
					return ObjectiveStatus.COMPLETE;
				}
			}
		};

		return ObjectiveStatus.ACTIVE;
	}


	@Override
	public void listen(final Event event) {
	}


	@Override
	public void uponCompletion() {
	}
}