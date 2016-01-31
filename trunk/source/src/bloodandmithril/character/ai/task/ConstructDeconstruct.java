package bloodandmithril.character.ai.task;

import static bloodandmithril.networking.ClientServerInterface.isClient;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop.ReturnPropPosition;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.ConstructionWindow;

/**
 * Instructs an {@link Individual} to walk to a {@link Construction} and opens the {@link ConstructionWindow}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ConstructDeconstruct extends CompositeAITask {
	private static final long serialVersionUID = -5586910244188482156L;
	private Construction construction;
	private int connectionId;

	/**
	 * Constructor
	 */
	public ConstructDeconstruct(final Individual host, final Construction construction, int connectionId) {
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


	public class OpenWindow extends AITask {
		private static final long serialVersionUID = 338634457034287529L;
		private boolean opened;

		/**
		 * Constructor
		 */
		public OpenWindow(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getShortDescription() {
			return "Constructing/Deconstructing";
		}


		@Override
		public boolean isComplete() {
			return opened;
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(float delta) {
			if (getHost().getInteractionBox().isWithinBox(construction.position)) {
				if (isClient()) {
					UserInterface.addLayeredComponentUnique(
						new ConstructionWindow(
							getHost().getId().getSimpleName() + " interacting with " + construction.getTitle(),
							true,
							getHost(),
							construction
						)
					);
				} else {
					ClientServerInterface.SendNotification.notifyConstructionWindowOpen(getHost().getId().getId(), construction.id, connectionId);
				}
				opened = true;
			}
		}
	}
}