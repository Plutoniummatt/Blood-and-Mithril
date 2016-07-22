package bloodandmithril.character.ai.task.construct;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.GoToLocation;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.construction.Construction;

/**
 * {@link AITask} that represents the construction of a {@link Construction}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Construct extends CompositeAITask {
	private static final long serialVersionUID = 8175661093571274804L;

	/**
	 * Constructor
	 */
	public Construct(final Individual host, final Construction construction, final boolean deconstruct) {
		super(
			host.getId(),
			"Constructing",
			GoToLocation.goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(construction.position, 32),
				false,
				32f,
				true
			)
		);

		appendTask(new Constructing(hostId, construction.id, deconstruct));
	}


	public static class Constructing extends AITask {
		private static final long serialVersionUID = -6557725570349017304L;
		int constructionId;
		boolean stop;
		boolean deconstruct;

		/**
		 * Constructor
		 */
		public Constructing(final IndividualIdentifier hostId, final int constructionId, final boolean deconstruct) {
			super(hostId);
			this.constructionId = constructionId;
			this.deconstruct = deconstruct;
		}


		@Override
		public String getShortDescription() {
			return "Constructing";
		}
	}
}