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
		private int constructionId;
		private boolean stop;
		private boolean deconstruct;

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


		@Override
		public boolean isComplete() {
			if (deconstruct) {
				return !Domain.getWorld(getHost().getWorldId()).props().hasProp(constructionId) || stop || !((Construction) Domain.getWorld(getHost().getWorldId()).props().getProp(constructionId)).canDeconstruct();
			} else {
				return ((Construction) Domain.getWorld(getHost().getWorldId()).props().getProp(constructionId)).getConstructionProgress() == 1f || stop;
			}
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		protected void internalExecute(final float delta) {
			final Construction construction = (Construction) Domain.getWorld(getHost().getWorldId()).props().getProp(constructionId);
			if (construction != null) {
				if (deconstruct) {
					construction.deconstruct(Domain.getIndividual(hostId.getId()), delta);
				} else {
					construction.construct(Domain.getIndividual(hostId.getId()), delta);
				}
			} else {
				stop = true;
			}
		}
	}
}