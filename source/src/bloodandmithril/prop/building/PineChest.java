package bloodandmithril.prop.building;

import static com.google.common.collect.Maps.newHashMap;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Container;
import bloodandmithril.item.ContainerImpl;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.plant.Carrot;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.ContextMenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A {@link ConstructionWithContainer} made from pine
 */
public class PineChest extends Construction implements Container {

	/** {@link TextureRegion} of the {@link PineChest} */
	public static TextureRegion pineChest;

	/** The {@link Container} of this {@link PineChest} */
	private ContainerImpl container;
	
	/**
	 * Constructor
	 */
	public PineChest(float x, float y, boolean grounded, float capacity) {
		super(x, y, 35, 44, grounded, 0.1f);
		container = new ContainerImpl(capacity, true);
	}


	@Override
	public ContextMenu getCompletedContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());

		menu.addMenuItem(
			new ContextMenuItem(
				"Show info",
				() -> {
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
				() -> {
					if (ClientServerInterface.isServer()) {
						selected.getAI().setCurrentTask(
							new TradeWith(selected, this)
						);
					} else {
						ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, id);
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
	public void synchronizeProp(Prop other) {
		if (other instanceof PineChest) {
			this.container.synchronizeContainer(((PineChest)other).container);
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
	public void synchronizeContainer(Container other) {
		if (getConstructionProgress() == 1f) {
			container.synchronizeContainer(other);
		} else {
			super.synchronizeContainer(other);
		}
	}


	@Override
	public void giveItem(Item item) {
		if (getConstructionProgress() == 1f) {
			container.giveItem(item);
		} else {
			super.giveItem(item);
		}
	}


	@Override
	public int takeItem(Item item) {
		if (getConstructionProgress() == 1f) {
			return container.takeItem(item);
		} else {
			return super.takeItem(item);
		}
	}


	@Override
	public Map<Item, Integer> getInventory() {
		if (getConstructionProgress() == 1f) {
			return container.getInventory();
		} else {
			return super.getInventory();
		}
	}


	@Override
	public float getMaxCapacity() {
		if (getConstructionProgress() == 1f) {
			return container.getMaxCapacity();
		} else {
			return super.getMaxCapacity();
		}
	}


	@Override
	public float getCurrentLoad() {
		if (getConstructionProgress() == 1f) {
			return container.getCurrentLoad();
		} else {
			return super.getCurrentLoad();
		}
	}


	@Override
	public boolean canExceedCapacity() {
		if (getConstructionProgress() == 1f) {
			return container.canExceedCapacity();
		} else {
			return super.canExceedCapacity();
		}
	}


	@Override
	protected Map<Item, Integer> getRequiredMaterials() {
		HashMap<Item, Integer> map = newHashMap();
		
		map.put(new Carrot(), 10);
		
		return map;
	}
}