package bloodandmithril.objectives.tutorial;

import java.util.List;

import com.google.common.collect.Lists;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.objectives.Mission;
import bloodandmithril.objectives.Objective;
import bloodandmithril.objectives.objective.ConstructConstructionObjective;
import bloodandmithril.objectives.objective.GoToLocationObjective;
import bloodandmithril.objectives.objective.MoveIndividualObjective;
import bloodandmithril.objectives.objective.function.AnyControllableIndividual;
import bloodandmithril.objectives.objective.function.NeverFailFunction;
import bloodandmithril.prop.construction.craftingstation.BlacksmithWorkshop;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.world.Domain;

/**
 * Tutorial {@link Objective}s, designed to teach players about game mechanics
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class Tutorial extends Mission {
	private static final long serialVersionUID = -3942281398077815457L;

	/**
	 * Constructor
	 */
	public Tutorial(final int worldId) {
		super(worldId);
	}


	@Override
	protected List<Objective> getNewObjectives() {
		final List<Objective> objectives = Lists.newLinkedList();

		objectives.addAll(
			Lists.newArrayList(
				new MoveIndividualObjective(),
				new ConstructConstructionObjective(BlacksmithWorkshop.class) {
					private static final long serialVersionUID = 4321918095858974779L;
					@Override
					public void uponCompletion() {
						addObjective(new GoToLocationObjective(
							new AnyControllableIndividual(),
							() -> {
								return Domain.getWorld(worldId).props().getAnyProp(BlacksmithWorkshop.class).position;
							},
							32f,
							worldId,
							new NeverFailFunction(),
							"Go to " + BlacksmithWorkshop.class.getAnnotation(Name.class).name()
						));
					}
				},
				new ConstructConstructionObjective(Campfire.class),
				new ConstructConstructionObjective(Furnace.class),
				new ConstructConstructionObjective(WorkBench.class)
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
		return "Follow these instructions...More to come...";
	}


	@Override
	public String getTitle() {
		return "Tutorial";
	}


	@Override
	public void uponCompletion() {
	}
}