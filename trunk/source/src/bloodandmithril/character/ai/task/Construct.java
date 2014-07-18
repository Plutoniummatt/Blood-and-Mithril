package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.world.Domain;

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
	public Construct(Individual host, Construction construction) {
		super(
			host.getId(),
			"Constructing",
			new GoToLocation(
				host,
				new WayPoint(construction.position, 32),
				false,
				32f,
				true
			)
		);

		appendTask(new Constructing(hostId, construction.id));
	}


	public static class Constructing extends AITask {
		private static final long serialVersionUID = -6557725570349017304L;
		private int constructionId;

		/**
		 * Constructor
		 */
		public Constructing(IndividualIdentifier hostId, int constructionId) {
			super(hostId);
			this.constructionId = constructionId;
		}


		@Override
		public String getDescription() {
			return "Constructing";
		}


		@Override
		public boolean isComplete() {
			return ((Construction) Domain.getProps().get(constructionId)).getConstructionProgress() == 1f;
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(float delta) {
			Construction construction = (Construction) Domain.getProps().get(constructionId);
			construction.construct(Domain.getIndividuals().get(hostId.getId()), delta);
		}
	}
}