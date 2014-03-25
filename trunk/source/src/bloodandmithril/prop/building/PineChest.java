package bloodandmithril.prop.building;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Task;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A {@link ConstructionWithContainer} made from pine
 */
public class PineChest extends ConstructionWithContainer {

	/** {@link TextureRegion} of the {@link PineChest} */
	public static TextureRegion pineChest;

	/**
	 * Constructor
	 */
	public PineChest(float x, float y, boolean grounded, float capacity) {
		super(x, y, 35, 44, grounded, capacity);
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
								"A chest constructed mostly from pine, used to store items",
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

		if (Domain.getSelectedIndividuals().size() == 1) {
			final Individual selected = Domain.getSelectedIndividuals().iterator().next();
			ContextMenuItem openChestMenuItem = new ContextMenuItem(
				"Open",
				new Task() {
					@Override
					public void execute() {
						if (ClientServerInterface.isServer()) {
							selected.getAI().setCurrentTask(
								new TradeWith(selected, container)
							);
						} else {
							ConstructionContainer chestContainer = (ConstructionContainer) container;
							ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, chestContainer.propId);
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


	@Override
	public void synchronize(Prop other) {
		if (other instanceof PineChest) {
			this.container.synchronize(((PineChest)other).container);
		} else {
			throw new RuntimeException("Can not synchronize Pine Chest with " + other.getClass().getSimpleName());
		}
	}


	@Override
	protected void internalRender(float constructionProgress) {
		BloodAndMithrilClient.spriteBatch.draw(pineChest, position.x - width / 2, position.y);
	}


	@Override
	public void update(float delta) {
	}


	@Override
	protected ContextMenu getConstructionContextMenu() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected ContextMenu getCompletedContextMenu() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected void giveItemDecorator(Item item) {
		// TODO Auto-generated method stub
	}
}