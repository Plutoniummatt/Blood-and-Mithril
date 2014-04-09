package bloodandmithril.character.ai.task;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.furniture.Anvil;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.AnvilWindow;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

/**
 * A {@link CompositeAITask} comprising of:
 *
 * {@link GoToLocation} of the anvil.
 * Opening the {@link AnvilWindow}.
 *
 * @author Matt
 */
public class Smith extends CompositeAITask {
	private static final long serialVersionUID = -4098496856332182431L;

	/**
	 * Overloaded constructor
	 */
	public Smith(final Individual smith, final Anvil anvil, int connectionId) {
		super(smith.getId(), "Smithing");

		Vector2 location = ((Prop) anvil).position;

		setCurrentTask(new GoToMovingLocation(
			smith.getId(),
			location,
			50f
		));

		if (ClientServerInterface.isServer()) {
			appendTask(
				new BeginSmithing(hostId, anvil.id, connectionId)
			);
		} else {
			appendTask(
				new BeginSmithing(hostId, anvil.id)
			);
		}
	}


	/**
	 * Constructor
	 */
	public Smith(final Individual smith, final Anvil anvil) {
		super(smith.getId(), "Smithing");

		Vector2 location = ((Prop) anvil).position;

		setCurrentTask(new GoToMovingLocation(
			smith.getId(),
			location,
			50f
		));

		appendTask(
			new BeginSmithing(hostId, anvil.id)
		);
	}


	public static class BeginSmithing extends AITask {
		private static final long serialVersionUID = 4644624691451364142L;

		private final int anvil;
		private final int connectionId;

		/**
		 * Constructor
		 */
		protected BeginSmithing(IndividualIdentifier hostId, int anvil, int connectionId) {
			super(hostId);
			this.anvil = anvil;
			this.connectionId = connectionId;
		}

		/**
		 * Constructor
		 */
		protected BeginSmithing(IndividualIdentifier hostId, int anvil) {
			super(hostId);
			this.anvil = anvil;
			this.connectionId = -1;
		}

		@Override
		public void uponCompletion() {
		}

		@Override
		public boolean isComplete() {
			return Domain.getIndividuals().get(hostId.getId()).getAI().getCurrentTask() instanceof Smith;
		}

		@Override
		public String getDescription() {
			return "Smithing";
		}

		@Override
		public void execute(float delta) {
			if (Domain.getIndividuals().get(hostId.getId()).getDistanceFrom(Domain.getProps().get(anvil).position) > 64f) {
				return;
			}

			if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
				ClientServerInterface.SendNotification.notifyOpenAnvilWindow(hostId.getId(), anvil, connectionId);
			} else if (ClientServerInterface.isClient()) {
				openAnvilWindow(Domain.getIndividuals().get(hostId.getId()), (Anvil) Domain.getProps().get(anvil));
			}

			Domain.getIndividuals().get(hostId.getId()).clearCommands();
			Domain.getIndividuals().get(hostId.getId()).getAI().setCurrentTask(new Blacksmithing(hostId, anvil));
		}
	}


	public static void openAnvilWindow(Individual smith, Anvil anvil) {
		UserInterface.addLayeredComponent(
			new AnvilWindow(
				BloodAndMithrilClient.WIDTH/2 - 150,
				BloodAndMithrilClient.HEIGHT/2 + 175,
				300,
				350,
				"Smith",
				true,
				300,
				350,
				smith,
				anvil
			)
		);
	}


	public static class Blacksmithing extends AITask {
		private static final long serialVersionUID = 7785697953568360456L;
		private int anvilId;

		protected Blacksmithing(IndividualIdentifier hostId, int anvilId) {
			super(hostId);
			this.anvilId = anvilId;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public boolean isComplete() {
			return Domain.getIndividuals().get(hostId.getId()).getState().position.cpy().sub(Domain.getProps().get(anvilId).position.cpy()).len() > 64;
		}

		@Override
		public void uponCompletion() {
		}

		@Override
		public void execute(float delta) {
		}
	}
}