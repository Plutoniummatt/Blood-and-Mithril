package bloodandmithril.objectives.tutorial;

import java.util.List;

import bloodandmithril.event.Event;
import bloodandmithril.objectives.Mission;
import bloodandmithril.objectives.Objective;
import bloodandmithril.objectives.objective.MoveIndividualObjective;

import com.google.common.collect.Lists;

/**
 * Tutorial {@link Objective}s, designed to teach players about game mechanics
 *
 * @author Matt
 */
public class Tutorial extends Mission {
	private static final long serialVersionUID = -3942281398077815457L;

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
			new MoveIndividualObjective()
		);

		return objectives;
	}


	@Override
	public int getWorldId() {
		return -1;
	}


	@Override
	public String getDescription() {
		return "Follow these instructions...More to come...";
	}


	@Override
	public String getTitle() {
		return "Tutorial";
	}


	@Override
	public void listen(Event event) {
		Objective currentObjective = getCurrentObjective();
		if (currentObjective != null) {
			currentObjective.listen(event);
		}
		update();
	}
}