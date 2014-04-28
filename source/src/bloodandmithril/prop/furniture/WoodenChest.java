package bloodandmithril.prop.furniture;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.function.Function;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.LockUnlockContainer;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.Container;
import bloodandmithril.item.ContainerImpl;
import bloodandmithril.item.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A chest made from wood
 */
public class WoodenChest extends Construction implements Container {
	private static final long serialVersionUID = -8935044324226731703L;
	public static final String description = "A chest constructed mostly from wood, used to store items";

	/** {@link TextureRegion} of the {@link WoodenChest} */
	public static TextureRegion woodenChest;

	/** The {@link Container} of this {@link WoodenChest} */
	private ContainerImpl container;

	/**
	 * Constructor
	 */
	public WoodenChest(float x, float y, boolean grounded, float capacity) {
		super(x, y, 44, 35, grounded, 0f);
		container = new ContainerImpl(capacity, true);
		setConstructionProgress(1f);
	}


	/**
	 * Constructor for lockable {@link WoodenChest}
	 */
	public WoodenChest(float x, float y, boolean grounded, float capacity, boolean locked, Function<Item, Boolean> unlockingFunction) {
		super(x, y, 44, 35, grounded, 0.1f);
		container = new ContainerImpl(capacity, true, locked, unlockingFunction);
		setConstructionProgress(1f);
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
							description,
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
			if (Domain.getSelectedIndividuals().size() > 0) {
				final Individual selected = Domain.getSelectedIndividuals().iterator().next();
				MenuItem openChestMenuItem = new MenuItem(
					"Open",
					() -> {
						if (Domain.getSelectedIndividuals().size() == 1) {
							if (ClientServerInterface.isServer()) {
								selected.getAI().setCurrentTask(
										new TradeWith(selected, this)
										);
							} else {
								ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, id);
							}
						}
					},
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
					new ContextMenu(0, 0, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null)),
					() -> {
						return Domain.getSelectedIndividuals().size() > 1;
					}
				);
				menu.addMenuItem(openChestMenuItem);

				if (container.isLockable()) {
					MenuItem lockChestMenuItem = new MenuItem(
						"Lock",
						() -> {
							if (Domain.getSelectedIndividuals().size() == 1) {
								if (ClientServerInterface.isServer()) {
									selected.getAI().setCurrentTask(
											new LockUnlockContainer(selected, this, true)
											);
								} else {
									ClientServerInterface.SendRequest.sendLockUnlockContainerRequest(selected.getId().getId(), id, true);
								}
							}
						},
						Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
						Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
						Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
						new ContextMenu(0, 0, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null)),
						() -> {
							return Domain.getSelectedIndividuals().size() > 1;
						}
					);
					menu.addMenuItem(lockChestMenuItem);
				}
			}
		} else {
			if (Domain.getSelectedIndividuals().size() > 0) {
				final Individual selected = Domain.getSelectedIndividuals().iterator().next();
				MenuItem unlockChestMenuItem = new MenuItem(
					"Unlock",
					() -> {
						if (Domain.getSelectedIndividuals().size() == 1) {
							if (ClientServerInterface.isServer()) {
								selected.getAI().setCurrentTask(
										new LockUnlockContainer(selected, this, false)
										);
							} else {
								ClientServerInterface.SendRequest.sendLockUnlockContainerRequest(selected.getId().getId(), id, false);
							}
						}
					},
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
					new ContextMenu(0, 0, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null)),
					() -> {
						return Domain.getSelectedIndividuals().size() > 1;
					}
				);
				menu.addMenuItem(unlockChestMenuItem);
			}
		}

		return menu;
	}


	@Override
	public void synchronizeProp(Prop other) {
		if (other instanceof WoodenChest) {
			this.container.synchronizeContainer(((WoodenChest)other).container);
		} else {
			throw new RuntimeException("Can not synchronize Wooden Chest with " + other.getClass().getSimpleName());
		}
	}


	@Override
	protected void internalRender(float constructionProgress) {
		spriteBatch.draw(woodenChest, position.x - width / 2, position.y);
	}


	@Override
	public void update(float delta) {
	}


	@Override
	public Container getContainerImpl() {
		if (getConstructionProgress() == 1f) {
			return container;
		} else {
			return super.getContainerImpl();
		}
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		return newHashMap();
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
	public int has(Item item) {
		if (getConstructionProgress() == 1f) {
			return container.has(item);
		} else {
			return 0;
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


	@Override
	public String getTitle() {
		return "Wooden chest";
	}
}