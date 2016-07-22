package bloodandmithril.character.ai.task.construct;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.task.GoToMovingLocation;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop.ReturnPropPosition;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.components.window.ConstructionWindow;

/**
 * Instructs an {@link Individual} to walk to a {@link Construction} and opens the {@link ConstructionWindow}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@ExecutedBy(CompositeAITaskExecutor.class)
public class ConstructDeconstruct extends CompositeAITask {
	private static final long serialVersionUID = -5586910244188482156L;

	Construction construction;
	int connectionId;

	/**
	 * Constructor
	 */
	public ConstructDeconstruct(final Individual host, final Construction construction, final int connectionId) {
		super(host.getId(), "Constructing/Deconstructing");
		this.construction = construction;
		this.connectionId = connectionId;

		appendTask(new GoToMovingLocation(
			host.getId(),
			new ReturnPropPosition(construction),
			40f
		));

		appendTask(new OpenWindow(host.getId()));
	}


	@ExecutedBy(OpenWindowExecutor.class)
	public class OpenWindow extends AITask {
		private static final long serialVersionUID = 338634457034287529L;
		boolean opened;

		/**
		 * Constructor
		 */
		public OpenWindow(final IndividualIdentifier hostId) {
			super(hostId);
		}


		public ConstructDeconstruct getParent() {
			return ConstructDeconstruct.this;
		}


		@Override
		public String getShortDescription() {
			return "Constructing/Deconstructing";
		}
	}
}