package bloodandmithril.character.ai.task.opencraftingstation;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.ai.task.gotolocation.GoToLocation;
import bloodandmithril.character.ai.task.gotolocation.GoToMovingLocation;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop.ReturnPropPosition;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;

/**
 * A {@link CompositeAITask} comprising of:
 *
 * {@link GoToLocation} of the {@link CraftingStation}.
 * Opening the {@link CraftingStation}.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(CompositeAITaskExecutor.class)
public class OpenCraftingStation extends CompositeAITask {
	private static final long serialVersionUID = -4098496856332182431L;

	/**
	 * Overloaded constructor
	 */
	public OpenCraftingStation(final Individual individual, final CraftingStation craftingStation, final int connectionId) {
		super(individual.getId(), "Opening " + craftingStation.getClass().getSimpleName());

		setCurrentTask(new GoToMovingLocation(
			individual.getId(),
			new ReturnPropPosition(craftingStation),
			50f
		));

		if (ClientServerInterface.isServer()) {
			appendTask(
				new OpenCraftingStationWindow(hostId, craftingStation.id, connectionId)
			);
		} else {
			appendTask(
				new OpenCraftingStationWindow(hostId, craftingStation.id)
			);
		}
	}


	/**
	 * Constructor
	 */
	public OpenCraftingStation(final Individual smith, final CraftingStation craftingStation) {
		super(smith.getId(), "Opening crafting station");

		setCurrentTask(new GoToMovingLocation(
			smith.getId(),
			new ReturnPropPosition(craftingStation),
			50f
		));

		appendTask(
			new OpenCraftingStationWindow(hostId, craftingStation.id)
		);
	}


	@ExecutedBy(OpenCraftingStationWindowExecutor.class)
	public static class OpenCraftingStationWindow extends AITask {
		private static final long serialVersionUID = 4644624691451364142L;

		final int craftingStationId;
		final int connectionId;
		boolean opened;

		/**
		 * Constructor
		 */
		protected OpenCraftingStationWindow(final IndividualIdentifier hostId, final int craftingStationId, final int connectionId) {
			super(hostId);
			this.craftingStationId = craftingStationId;
			this.connectionId = connectionId;
		}

		/**
		 * Constructor
		 */
		protected OpenCraftingStationWindow(final IndividualIdentifier hostId, final int anvil) {
			super(hostId);
			this.craftingStationId = anvil;
			this.connectionId = -1;
		}
		
		
		@Override
		public String getShortDescription() {
			return "Smithing";
		}
	}
}