package bloodandmithril.prop.building;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.ai.task.Trading;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Task;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A {@link Chest} made from pine
 */
public class PineChest extends Chest {

	/** {@link TextureRegion} of the {@link PineChest} TODO - New texture */
	private static TextureRegion pineChest = new TextureRegion(GameWorld.gameWorldTexture, 350, 175, 57, 68);

	/**
	 * Constructor
	 */
	public PineChest(float x, float y, boolean grounded, float capacity) {
		super(x, y, 57, 68, grounded, capacity);
	}


	@Override
	public void render() {
		BloodAndMithrilClient.spriteBatch.draw(pineChest, position.x - width / 2, position.y);
	}


	@Override
	public ContextMenu getContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());

		menu.addMenuItem(
			new ContextMenuItem(
				"Show info",
				new Task() {
					@Override
					public void execute() {
						UserInterface.addLayeredComponent(
							new MessageWindow(
								"A chest made from pine",
								Color.ORANGE,
								BloodAndMithrilClient.WIDTH/2 - 250,
								BloodAndMithrilClient.HEIGHT/2 + 125,
								500,
								250,
								"Wooden chest",
								true,
								300,
								150
							)
						);
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		if (GameWorld.selectedIndividuals.size() == 1 &&
		  !(GameWorld.selectedIndividuals.iterator().next().ai.getCurrentTask() instanceof Trading)) {
			final Individual selected = GameWorld.selectedIndividuals.iterator().next();
			ContextMenuItem openChestMenuItem = new ContextMenuItem(
				"Open",
				new Task() {
					@Override
					public void execute() {
						if (ClientServerInterface.isServer()) {
							if (ClientServerInterface.isServer()) {
								selected.ai.setCurrentTask(
									new TradeWith(selected, container)
								);
							} else {
								ChestContainer chestContainer = (ChestContainer) container;
								ClientServerInterface.tradeWithProp(selected, chestContainer.propId);
							}
						} else {
							ChestContainer chestContainer = (ChestContainer) container;
							ClientServerInterface.tradeWithProp(selected, chestContainer.propId);
						}
					}
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			);

			menu.addMenuItem(openChestMenuItem);
		}

		return menu;
	}
}