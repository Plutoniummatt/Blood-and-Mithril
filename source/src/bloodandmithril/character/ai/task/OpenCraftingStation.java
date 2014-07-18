package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

/**
 * A {@link CompositeAITask} comprising of:
 *
 * {@link GoToLocation} of the {@link CraftingStation}.
 * Opening the {@link CraftingStation}.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class OpenCraftingStation extends CompositeAITask {
	private static final long serialVersionUID = -4098496856332182431L;

	/**
	 * Overloaded constructor
	 */
	public OpenCraftingStation(final Individual individual, final CraftingStation craftingStation, int connectionId) {
		super(individual.getId(), "Opening " + craftingStation.getClass().getSimpleName());

		Vector2 location = ((Prop) craftingStation).position;

		setCurrentTask(new GoToMovingLocation(
			individual.getId(),
			location,
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

		Vector2 location = ((Prop) craftingStation).position;

		setCurrentTask(new GoToMovingLocation(
			smith.getId(),
			location,
			50f
		));

		appendTask(
			new OpenCraftingStationWindow(hostId, craftingStation.id)
		);
	}


	public static class OpenCraftingStationWindow extends AITask {
		private static final long serialVersionUID = 4644624691451364142L;

		private final int craftingStationId;
		private final int connectionId;
		private boolean opened;

		/**
		 * Constructor
		 */
		protected OpenCraftingStationWindow(IndividualIdentifier hostId, int craftingStationId, int connectionId) {
			super(hostId);
			this.craftingStationId = craftingStationId;
			this.connectionId = connectionId;
		}

		/**
		 * Constructor
		 */
		protected OpenCraftingStationWindow(IndividualIdentifier hostId, int anvil) {
			super(hostId);
			this.craftingStationId = anvil;
			this.connectionId = -1;
		}

		@Override
		public boolean uponCompletion() {
			return false;
		}

		@Override
		public boolean isComplete() {
			return opened;
		}

		@Override
		public String getDescription() {
			return "Smithing";
		}

		@Override
		public void execute(float delta) {
			if (Domain.getIndividuals().get(hostId.getId()).getDistanceFrom(Domain.getProps().get(craftingStationId).position) > 64f) {
				return;
			}

			if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
				ClientServerInterface.SendNotification.notifyOpenCraftingStationWindow(hostId.getId(), craftingStationId, connectionId);
			} else if (ClientServerInterface.isClient()) {
				openCraftingStationWindow(Domain.getIndividuals().get(hostId.getId()), (CraftingStation) Domain.getProps().get(craftingStationId));
			}

			opened = true;
			Domain.getIndividuals().get(hostId.getId()).clearCommands();
		}
	}


	public static void openCraftingStationWindow(Individual individual, CraftingStation craftingStation) {
		UserInterface.addLayeredComponentUnique(
			craftingStation.getCraftingStationWindow(individual)
		);
	}
}