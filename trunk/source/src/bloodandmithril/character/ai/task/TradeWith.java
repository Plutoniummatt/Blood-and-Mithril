package bloodandmithril.character.ai.task;

import bloodandmithril.Fortress;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.item.Container;
import bloodandmithril.prop.building.Chest.ChestContainer;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.TradeWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.world.GameWorld;

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

		currentTask = new GoToLocation(
			proposer,
			new WayPoint(location, 40f),
			false,
			40f,
			true
		);

		appendTask(
			new AITask(proposer.id) {

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
						Individual indi = (Individual)proposee;

						for (Component component : Lists.newArrayList(UserInterface.layeredComponents)) {
							if (component instanceof Window) {
								if (((Window)component).title.equals(proposer.id.getSimpleName() + " - Inventory") ||
								((Window)component).title.equals(indi.id.getSimpleName() + " - Inventory")) {
									UserInterface.removeLayeredComponent(component);
								}
							}
						}

						UserInterface.addLayeredComponentUnique(
							new TradeWindow(
								Fortress.WIDTH / 2 - 350,
								Fortress.HEIGHT / 2 + 100,
								700,
								200,
								"Trade between " + indi.id.firstName + " and " + proposer.id.firstName,
								true,
								700,
								200,
								true,
								GameWorld.selectedIndividuals.iterator().next(),
								proposer
							),
							"Trade between " + indi.id.firstName + " and " + proposer.id.firstName
						);

						proposer.clearCommands();
						proposer.ai.setCurrentTask(new Trading(proposer.id));
						indi.clearCommands();
						indi.ai.setCurrentTask(new Trading(indi.id));
					} else if (proposee instanceof ChestContainer) {
						UserInterface.addLayeredComponentUnique(
							new TradeWindow(
								Fortress.WIDTH/2 - 350,
								Fortress.HEIGHT/2 + 100,
								700,
								200,
								GameWorld.selectedIndividuals.iterator().next().id.getSimpleName() + " interacting with pine chest",
								true,
								700,
								200,
								true,
								GameWorld.selectedIndividuals.iterator().next(),
								proposee
							),
							GameWorld.selectedIndividuals.iterator().next().id.getSimpleName() + " interacting with pine chest"
						);
					}
				}
			}
		);
	}
}