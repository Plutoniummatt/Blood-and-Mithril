package bloodandmithril.character.ai.task;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.item.Container;
import bloodandmithril.prop.building.Chest.ChestContainer;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.TradeWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

/**
 * A {@link CompositeAITask} comprising of:
 *
 * {@link GoToLocation} of the proposee.
 * opening a {@link TradeWindow} with the proposee.
 *
 * @author Matt
 */
public class TradeWith extends CompositeAITask {
	private static final long serialVersionUID = -4098496856332182431L;


	/**
	 * Constructor
	 */
	public TradeWith(final Individual proposer, final Container proposee) {
		super(proposer.id, "Trading");

		Vector2 location = null;

		if (proposee instanceof ChestContainer) {
			location = ((ChestContainer) proposee).getPositionOfChest();
		} else if (proposee instanceof Individual) {
			location = ((Individual) proposee).state.position;
		}

		currentTask = new GoToMovingLocation(
			proposer.id,
			location,
			50f
		);

		appendTask(
			new AITask(proposer.id) {
				private static final long serialVersionUID = 4644624691451364142L;

				@Override
				public void uponCompletion() {
				}

				@Override
				public boolean isComplete() {
					return proposer.ai.getCurrentTask() instanceof Trading;
				}

				@Override
				public String getDescription() {
					return "Trading";
				}

				@Override
				public void execute() {

					if (proposee instanceof Individual) {
						Individual proposeeCasted = (Individual)proposee;

						if (proposer.getDistanceFrom(proposeeCasted.state.position) > 64f) {
							return;
						}

						for (Component component : Lists.newArrayList(UserInterface.layeredComponents)) {
							if (component instanceof Window) {
								if (((Window)component).title.equals(proposer.id.getSimpleName() + " - Inventory") ||
								((Window)component).title.equals(proposeeCasted.id.getSimpleName() + " - Inventory")) {
									UserInterface.removeLayeredComponent(component);
								}
							}
						}

						UserInterface.addLayeredComponentUnique(
							new TradeWindow(
								BloodAndMithrilClient.WIDTH / 2 - 350,
								BloodAndMithrilClient.HEIGHT / 2 + 100,
								700,
								200,
								"Trade between " + proposeeCasted.id.firstName + " and " + proposer.id.firstName,
								true,
								700,
								200,
								true,
								proposer,
								proposeeCasted
							),
							"Trade between " + proposeeCasted.id.firstName + " and " + proposer.id.firstName
						);

						proposer.clearCommands();
						proposer.ai.setCurrentTask(new Trading(proposer.id));
						proposeeCasted.clearCommands();
						proposeeCasted.ai.setCurrentTask(new Trading(proposeeCasted.id));
					} else if (proposee instanceof ChestContainer) {

						if (proposer.getDistanceFrom(((ChestContainer)proposee).getPositionOfChest()) > 64f) {
							return;
						}

						UserInterface.addLayeredComponentUnique(
							new TradeWindow(
								BloodAndMithrilClient.WIDTH/2 - 350,
								BloodAndMithrilClient.HEIGHT/2 + 100,
								700,
								200,
								proposer.id.getSimpleName() + " interacting with pine chest",
								true,
								700,
								200,
								true,
								proposer,
								proposee
							),
							proposer.id.getSimpleName() + " interacting with pine chest"
						);

						proposer.clearCommands();
						proposer.ai.setCurrentTask(new Trading(proposer.id));
					}
				}
			}
		);
	}
}