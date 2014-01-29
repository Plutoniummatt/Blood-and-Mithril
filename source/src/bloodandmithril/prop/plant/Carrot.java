package bloodandmithril.prop.plant;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.ai.task.Trading;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.PineChest;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Task;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * The {@link Prop} equivalent to {@link bloodandmithril.item.material.plant.Carrot}
 *
 * @author Matt
 */
public class Carrot extends Plant {

	/** {@link TextureRegion} of the {@link PineChest} TODO - texture */
	public static TextureRegion carrot;

	/**
	 * Constructor
	 */
	public Carrot(float x, float y) {
		super(x, y, 16, 24);
	}


	@Override
	public void render() {
		BloodAndMithrilClient.spriteBatch.draw(carrot, position.x - width / 2, position.y);
	}


	@Override
	public void synchronize(Prop other) {
		// Don't need to synchronize a carrot
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
								bloodandmithril.item.material.plant.Carrot.description,
								Color.ORANGE,
								BloodAndMithrilClient.WIDTH/2 - 250,
								BloodAndMithrilClient.HEIGHT/2 + 125,
								500,
								250,
								"Carrot",
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
		  !(GameWorld.selectedIndividuals.iterator().next().getAI().getCurrentTask() instanceof Trading)) {

			menu.addMenuItem(
				new ContextMenuItem(
					"Harvest",
					new Task() {
						@Override
						public void execute() {
							if (ClientServerInterface.isServer()) {

							} else {

							}
						}
					},
					Color.WHITE,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
		}

		return menu;
	}
}