package bloodandmithril.prop.plant;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import bloodandmithril.character.ai.task.Harvest;
import bloodandmithril.character.ai.task.Trading;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * The {@link Prop} equivalent to {@link bloodandmithril.item.items.food.plant.Carrot}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Carrot extends Plant {
	private static final long serialVersionUID = -4581900482709094877L;

	/** {@link TextureRegion} of the {@link Carrot} */
	public static TextureRegion carrot;

	/**
	 * Constructor
	 */
	public Carrot(float x, float y) {
		super(x, y, 12, 17);
	}


	@Override
	public void render() {
		spriteBatch.draw(carrot, position.x - width / 2, position.y);
	}


	@Override
	public void synchronizeProp(Prop other) {
		// Don't need to synchronize a carrot
	}


	@Override
	public ContextMenu getContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY(), true);
		final Harvestable thisCarrot = this;

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
						new MessageWindow(
							bloodandmithril.item.items.food.plant.Carrot.description,
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
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		if (Domain.getSelectedIndividuals().size() == 1 &&
		  !(Domain.getSelectedIndividuals().iterator().next().getAI().getCurrentTask() instanceof Trading)) {

			menu.addMenuItem(
				new MenuItem(
					"Harvest",
					() -> {
						Individual individual = Domain.getSelectedIndividuals().iterator().next();
						if (ClientServerInterface.isServer()) {
							individual.getAI().setCurrentTask(
								new Harvest(individual, thisCarrot)
							);
						} else {
							ClientServerInterface.SendRequest.sendHarvestRequest(individual.getId().getId(), id);
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


	@Override
	public Item harvest() {
		return new bloodandmithril.item.items.food.plant.Carrot();
	}


	@Override
	public boolean destroyUponHarvest() {
		return true;
	}


	@Override
	public void update(float delta) {
	}


	@Override
	public float getGrowthTime() {
		return 10f;
	}
}