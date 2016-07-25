package bloodandmithril.prop.furniture;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;

import java.util.function.Function;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.character.ai.task.lockunlockcontainer.LockUnlockContainer;
import bloodandmithril.character.ai.task.trade.TradeWith;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.wood.Wood;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.StaticallyRenderedProp;
import bloodandmithril.prop.renderservice.StaticSpritePropRenderingService;
import bloodandmithril.prop.updateservice.NoOpPropUpdateService;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A chest made from wood
 */
@Copyright("Matthew Peck 2014")
@UpdatedBy(NoOpPropUpdateService.class)
@RenderPropWith(StaticSpritePropRenderingService.class)
public class WoodenChestProp extends Furniture implements Container, StaticallyRenderedProp {
	private static final long serialVersionUID = -8935044324226731703L;

	private Class<? extends Wood> wood;

	/** {@link TextureRegion} of the {@link WoodenChestProp} */
	public static TextureRegion WOODEN_CHEST;

	/** The {@link Container} of this {@link WoodenChestProp} */
	private ContainerImpl container;

	/**
	 * Constructor
	 */
	public WoodenChestProp(final float x, final float y, final float capacity, final int volume, final Class<? extends Wood> wood) {
		super(x, y, 56, 31, true);
		this.wood = wood;
		container = new ContainerImpl(capacity, volume);
	}


	/**
	 * Constructor for lockable {@link WoodenChestProp}
	 */
	public WoodenChestProp(final float x, final float y, final float capacity, final int volume, final boolean locked, final Function<Item, Boolean> unlockingFunction, final Class<? extends Wood> wood) {
		super(x, y, 56, 31, true);
		this.wood = wood;
		container = new ContainerImpl(capacity, volume, locked, unlockingFunction);
	}


	public String description() {
		return "A chest constructed mostly from wood, used to store items, this one is made from " + Material.getMaterial(wood).getName() + ".";
	}


	@Override
	public ContextMenu getContextMenu() {
		final ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);
		final GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					Wiring.injector().getInstance(UserInterface.class).addLayeredComponent(
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
			if (gameClientStateTracker.getSelectedIndividuals().size() > 0) {
				final Individual selected = gameClientStateTracker.getSelectedIndividuals().iterator().next();
				final MenuItem openChestMenuItem = new MenuItem(
					"Open",
					() -> {
						if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
							if (ClientServerInterface.isServer()) {
								selected.getAI().setCurrentTask(
										new TradeWith(selected, this)
										);
							} else {
								ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, id);
							}
						}
					},
					gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
					gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
					gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
					() -> {
						return new ContextMenu(0, 0, true, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
					},
					() -> {
						return gameClientStateTracker.getSelectedIndividuals().size() > 1;
					}
				);
				menu.addMenuItem(openChestMenuItem);

				if (container.isLockable()) {
					final MenuItem lockChestMenuItem = new MenuItem(
						"Lock",
						() -> {
							if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
								if (ClientServerInterface.isServer()) {
									try {
										selected.getAI().setCurrentTask(
											new LockUnlockContainer(selected, this, true)
										);
									} catch (final NoTileFoundException e) {}
								} else {
									ClientServerInterface.SendRequest.sendLockUnlockContainerRequest(selected.getId().getId(), id, true);
								}
							}
						},
						gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
						gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
						gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
						() -> {
							return new ContextMenu(0, 0, true, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
						},
						() -> {
							return gameClientStateTracker.getSelectedIndividuals().size() > 1;
						}
					);
					menu.addMenuItem(lockChestMenuItem);
				}
			}
		} else {
			if (gameClientStateTracker.getSelectedIndividuals().size() > 0) {
				final Individual selected = gameClientStateTracker.getSelectedIndividuals().iterator().next();
				final MenuItem unlockChestMenuItem = new MenuItem(
					"Unlock",
					() -> {
						if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
							if (ClientServerInterface.isServer()) {
								try {
									selected.getAI().setCurrentTask(
										new LockUnlockContainer(selected, this, false)
									);
								} catch (final NoTileFoundException e) {}
							} else {
								ClientServerInterface.SendRequest.sendLockUnlockContainerRequest(selected.getId().getId(), id, false);
							}
						}
					},
					gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
					gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
					gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
					() -> { return
						new ContextMenu(0, 0, true, new MenuItem("You have multiple individuals selected", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
					},
					() -> {
						return gameClientStateTracker.getSelectedIndividuals().size() > 1;
					}
				);
				menu.addMenuItem(unlockChestMenuItem);
			}
		}

		return menu;
	}


	@Override
	public void synchronizeProp(final Prop other) {
		if (other instanceof WoodenChestProp) {
			this.container.synchronizeContainer(((WoodenChestProp)other).container);
		} else {
			throw new RuntimeException("Can not synchronize Wooden Chest with " + other.getClass().getSimpleName());
		}
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
	public boolean unlock(final Item with) {
		return container.unlock(with);
	}


	@Override
	public int has(final Item item) {
		return container.has(item);
	}


	@Override
	public boolean lock(final Item with) {
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


	@Override
	public TextureRegion getTextureRegion() {
		return WOODEN_CHEST;
	}
}