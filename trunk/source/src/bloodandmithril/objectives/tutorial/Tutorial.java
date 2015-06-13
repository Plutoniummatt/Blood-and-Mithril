package bloodandmithril.objectives.tutorial;

import java.util.List;

import bloodandmithril.objectives.Mission;
import bloodandmithril.objectives.Objective;
import bloodandmithril.objectives.objective.GoToLocationObjective;
import bloodandmithril.objectives.objective.function.AnyControllableIndividual;
import bloodandmithril.objectives.objective.function.NeverFailFunction;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

/**
 * Tutorial {@link Objective}s, designed to teach players about game mechanics
 *
 * @author Matt
 */
public class Tutorial extends Mission {

	/**
	 * Constructor
	 */
	public Tutorial(int worldId) {
		super(worldId);
	}


	@Override
	protected List<Objective> getNewObjectives() {
		List<Objective> objectives = Lists.newLinkedList();

		objectives.add(
			new GoToLocationObjective(
				new AnyControllableIndividual(),
				() -> {
					return new Vector2(2000, 1000);
				},
				20f,
				worldId,
				new NeverFailFunction(),
				"Go here"
			)
		);

		return objectives;
	}


	@Override
	public int getWorldId() {
		return -1;
	}


	@Override
	public String getDescription() {
		return "";
	}


	@Override
	public String getTitle() {
		return "Tutorial";
	}
}