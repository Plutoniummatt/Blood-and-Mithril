package bloodandmithril.prop.building;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.google.common.collect.Maps.newHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Container;
import bloodandmithril.item.ContainerImpl;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.plant.Carrot;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.world.Domain;
import bloodandmithril.character.ai.task.LockUnlockContainer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A {@link ConstructionWithContainer} made from pine
 */
public class PineChest extends Construction implements Container {
	private static final long serialVersionUID = -8935044324226731703L;

	/** {@link TextureRegion} of the {@link PineChest} */
	public static TextureRegion pineChest;

	/** The {@link Container} of this {@link PineChest} */
	private ContainerImpl container;
	
	/**
	 * Constructor
	 */
	public PineChest(float x, float y, boolean grounded, float capacity) {
		super(x, y, 44, 35, grounded, 0.1f);
		container = new ContainerImpl(capacity, true);
	}

	
	/**
	 * Constructor for lockable {@link PineChest}
	 */
	public PineChest(float x, float y, boolean grounded, float capacity, boolean locked, Function<Item, Boolean> unlockingFunction) {
		super(x, y, 44, 35, grounded, 0.1f);
		container = new ContainerImpl(capacity, true, locked, unlockingFunction);
	}


	@Override
	public ContextMenu getCompletedContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY());

		menu.addMenuItem(
			new MenuItem(
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

		if (!isLocked()) {
			if (Domain.getSelectedIndividuals().size() == 1) {
				final Individual selected = Domain.getSelectedIndividuals().iterator().next();
				MenuItem openChestMenuItem = new MenuItem(
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
				
				if (container.isLockable()) {
					MenuItem lockChestMenuItem = new MenuItem(
						"Lock",
						() -> {
							if (ClientServerInterface.isServer()) {
								selected.getAI().setCurrentTask(
									new LockUnlockContainer(selected, this, true)
								);
							} else {
								ClientServerInterface.SendRequest.sendLockUnlockContainerRequest(selected.getId().getId(), id, true);
							}
						},
						Color.WHITE,
						Color.GREEN,
						Color.GRAY,
						null
					);
					menu.addMenuItem(lockChestMenuItem);
				}
			}
		} else {
			if (Domain.getSelectedIndividuals().size() == 1) {
				final Individual selected = Domain.getSelectedIndividuals().iterator().next();
				MenuItem unlockChestMenuItem = new MenuItem(
					"Unlock",
					() -> {
						if (ClientServerInterface.isServer()) {
							selected.getAI().setCurrentTask(
								new LockUnlockContainer(selected, this, false)
							);
						} else {
							ClientServerInterface.SendRequest.sendLockUnlockContainerRequest(selected.getId().getId(), id, false);
						}
					},
					Color.WHITE,
					Color.GREEN,
					Color.GRAY,
					null
				);
				menu.addMenuItem(unlockChestMenuItem);
			}
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
		spriteBatch.draw(pineChest, position.x - width / 2, position.y);
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


	@Override
	public boolean isLocked() {
		if (getConstructionProgress() == 1f) {
			return container.isLocked();
		} else {
			return false;
		}
	}


	@Override
	public boolean unlock(Item with) {
		if (getConstructionProgress() == 1f) {
			return container.unlock(with);
		} else {
			return false;
		}
	}


	@Override
	public boolean lock(Item with) {
		if (getConstructionProgress() == 1f) {
			return container.lock(with);
		} else {
			return false;
		}
	}


	@Override
	public boolean isLockable() {
		if (getConstructionProgress() == 1f) {
			return container.isLockable();
		} else {
			return false;
		}
	}
}