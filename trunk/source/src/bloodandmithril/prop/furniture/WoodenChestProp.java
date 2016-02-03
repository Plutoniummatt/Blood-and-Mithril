package bloodandmithril.prop.furniture;

import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;

import java.util.function.Function;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.character.ai.task.LockUnlockContainer;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.wood.Wood;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A chest made from wood
 */
@Copyright("Matthew Peck 2014")
public class WoodenChestProp extends Furniture implements Container {
	private static final long serialVersionUID = -8935044324226731703L;

	private Class<? extends Wood> wood;

	/** {@link TextureRegion} of the {@link WoodenChestProp} */
	public static TextureRegion woodenChest;

	/** The {@link Container} of this {@link WoodenChestProp} */
	private ContainerImpl container;

	/**
	 * Constructor
	 */
	public WoodenChestProp(float x, float y, float capacity, int volume, Class<? extends Wood> wood) {
		super(x, y, 56, 31, true);
		this.wood = wood;
		container = new ContainerImpl(capacity, volume);
	}


	/**
	 * Constructor for lockable {@link WoodenChestProp}
	 */
	public WoodenChestProp(float x, float y, float capacity, int volume, boolean locked, Function<Item, Boolean> unlockingFunction, Class<? extends Wood> wood) {
		super(x, y, 56, 31, true);
		this.wood = wood;
		container = new ContainerImpl(capacity, volume, locked, unlockingFunction);
	}


	public String description() {
		return "A chest constructed mostly from wood, used to store items, this one is made from " + Material.getMaterial(wood).getName() + ".";
	}


	@Override
	public ContextMenu getContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY(), true);

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
						new MessageWindow(
							description(),
							Color.ORANGE,
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
					() -> {
						return new ContextMenu(0, 0, true, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
					},
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
									try {
										selected.getAI().setCurrentTask(
											new LockUnlockContainer(selected, this, true)
										);
									} catch (NoTileFoundException e) {}
								} else {
									ClientServerInterface.SendRequest.sendLockUnlockContainerRequest(selected.getId().getId(), id, true);
								}
							}
						},
						Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
						Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
						Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
						() -> {
							return new ContextMenu(0, 0, true, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
						},
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
								try {
									selected.getAI().setCurrentTask(
										new LockUnlockContainer(selected, this, false)
									);
								} catch (NoTileFoundException e) {}
							} else {
								ClientServerInterface.SendRequest.sendLockUnlockContainerRequest(selected.getId().getId(), id, false);
							}
						}
					},
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
					Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
					() -> { return
						new ContextMenu(0, 0, true, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
					},
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
		if (other instanceof WoodenChestProp) {
			this.container.synchronizeContainer(((WoodenChestProp)other).container);
		} else {
			throw new RuntimeException("Can not synchronize Wooden Chest with " + other.getClass().getSimpleName());
		}
	}


	@Override
	public void render() {
		getGraphics().getSpriteBatch().draw(woodenChest, position.x - width / 2, position.y);
	}


	@Override
	public void update(float delta) {
	}


	@Override
	public Container getContainerImpl() {
		return container;
	}


	@Override
	public boolean isLocked() {
		return container.isLocked();
	}


	@Override
	public boolean unlock(Item with) {
		return container.unlock(with);
	}


	@Override
	public int has(Item item) {
		return container.has(item);
	}


	@Override
	public boolean lock(Item with) {
		return container.lock(with);
	}


	@Override
	public boolean isLockable() {
		return container.isLockable();
	}


	@Override
	public String getContextMenuItemLabel() {
		return "Wooden chest" + (isLocked() ? " (Locked)" : "");
	}


	@Override
	public void preRender() {
	}


	@Override
	public boolean isEmpty() {
		return container.isEmpty();
	}


	@Override
	public boolean canBeUsedAsFireSource() {
		return false;
	}


	@Override
	public boolean getWeightLimited() {
		return false;
	}
}