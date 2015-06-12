package bloodandmithril.objectives.tutorial;

import java.util.List;

import bloodandmithril.objectives.Mission;
import bloodandmithril.objectives.Objective;
import bloodandmithril.objectives.objective.GoToLocationObjective;
import bloodandmithril.objectives.objective.function.AnyControllableIndividual;
import bloodandmithril.objectives.objective.function.NeverFailFunction;

import com.google.common.collect.Lists;

/**
 * Tutorial {@link Objective}s, designed to teach players about game mechanics
 *
 * @author Matt
 */
public class Tutorial extends Mission {

	private int worldId;

	/**
	 * Constructor
	 */
	public Tutorial(int worldId) {
		this.worldId = worldId;
	}


	@Override
	protected List<Objective> getNewObjectives() {
		List<Objective> objectives = Lists.newLinkedList();

		objectives.add(
			new GoToLocationObjective(
				new AnyControllableIndividual(),
				null, // TODO
				20f,
				worldId,
				new NeverFailFunction()
			)
		);

		return objectives;
	}


	@Override
	public int getWorldId() {
		return -1;
	}
}