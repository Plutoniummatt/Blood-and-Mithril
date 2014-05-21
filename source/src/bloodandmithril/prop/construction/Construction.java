package bloodandmithril.prop.construction;

import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.csi.requests.SynchronizeIndividual;
import bloodandmithril.csi.requests.SynchronizePropRequest;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.RequiredMaterialsWindow;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.Color;

/**
 * A Construction
 *
 * @author Matt
 */
public abstract class Construction extends Prop implements Container {
	private static final long serialVersionUID = -7772373095960462479L;

	/** Dimensions of this {@link Construction} */
	protected final int width, height;

	/** Current construction progress, 1f means fully constructed */
	private float constructionProgress;

	/** The rate at which this {@link Construction} is constructed, in units of /s */
	private float constructionRate;

	/** The container used to store construction materials during the construction stage */
	private ContainerImpl materialContainer = new ContainerImpl(1000, true);

	/**
	 * Constructor
	 */
	protected Construction(float x, float y, int width, int height, boolean grounded, float constructionRate) {
		super(x, y, grounded, Depth.MIDDLEGROUND);
		this.width = width;
		this.height = height;
		this.constructionRate = constructionRate;
	}


	@Override
	public boolean isMouseOver() {
		float mx = BloodAndMithrilClient.getMouseWorldX();
		float my = BloodAndMithrilClient.getMouseWorldY();

		return mx > position.x - width/2 && mx < position.x + width/2 && my > position.y && my < position.y + height;
	}


	@Override
	public void render() {
		internalRender(constructionProgress);
	}


	/**
	 * Progresses the construction of this {@link Construction}, in time units measured in seconds
	 */
	public synchronized void construct(Individual individual, float time) {
		if (constructionProgress == 0f) {
			if (CraftingStation.enoughMaterialsToCraft(individual, getRequiredMaterials())) {
				for (Entry<Item, Integer> requiredItem : getRequiredMaterials().entrySet()) {
					for (int i = requiredItem.getValue(); i > 0; i--) {
						individual.takeItem(requiredItem.getKey());
					}
				}
			}

			if (ClientServerInterface.isClient()) {
				UserInterface.refreshRefreshableWindows();
			} else {
				ClientServerInterface.sendNotification(-1, true, true,
					new SynchronizePropRequest.SynchronizePropResponse(this),
					new SynchronizeIndividual.SynchronizeIndividualResponse(individual.getId().getId(), System.currentTimeMillis()),
					new RefreshWindowsResponse()
				);
			}
		}

		if (constructionProgress == 1f) {
			finishConstruction();
		} else {
			constructionProgress = constructionProgress + time * constructionRate >= 1f ? 1f : constructionProgress + time * constructionRate;
		}
	}


	/**
	 * Finalise the construction
	 */
	private void finishConstruction() {
		constructionProgress = 1f;
		materialContainer.getInventory().clear();
	}


	@Override
	public boolean leftClick() {
		if (!isMouseOver()) {
			return false;
		}
		return true;
	}


	@Override
	public boolean rightClick() {
		if (!isMouseOver()) {
			return false;
		}
		return true;
	}


	@Override
	public ContextMenu getContextMenu() {
		if (constructionProgress == 1f) {
			return getCompletedContextMenu();
		} else {
			ContextMenu menu = new ContextMenu(0, 0);

			if (Domain.getSelectedIndividuals().size() > 0) {
				final Individual selected = Domain.getSelectedIndividuals().iterator().next();
				MenuItem openTransferItemsWindow = new MenuItem(
					"Construct",
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

				menu.addMenuItem(openTransferItemsWindow);
			}

			return menu;
		}
	}


	/**
	 * See {@link #constructionStage}
	 */
	public float getConstructionProgress() {
		return constructionProgress;
	}


	/**
	 * See {@link #constructionStage}
	 */
	public void setConstructionProgress(float constructionProgress) {
		this.constructionProgress = constructionProgress;
	}


	@Override
	public String getContextMenuItemLabel() {
		return getTitle() + (constructionProgress == 1f ? "" : " - Under construction (" + String.format("%.1f", constructionProgress * 100) + "%)");
	}


	/** Returns the string title of this {@link Construction} */
	public abstract String getTitle();

	/** Renders this {@link Construction} based on {@link #constructionProgress} */
	protected abstract void internalRender(float constructionProgress);

	/** Get the required items to construct this {@link Construction} */
	public abstract Map<Item, Integer> getRequiredMaterials();

	/** Get the context menu that will be displayed once this {@link Construction} has finished being constructing */
	protected abstract ContextMenu getCompletedContextMenu();


	@Override
	public void synchronizeContainer(Container other) {
		materialContainer.synchronizeContainer(other);
	}


	public void synchronizeConstruction(Construction other) {
		constructionProgress = other.getConstructionProgress();
	}


	@Override
	public void giveItem(Item item) {
		if (constructionProgress == 1f) {
			getContainerImpl().giveItem(item);
		} else {
			materialContainer.giveItem(item);
			if (ClientServerInterface.isClient()) {
				UserInterface.layeredComponents.stream().filter((component) -> {
					return component instanceof RequiredMaterialsWindow;
				}).forEach((component) -> {
					((RequiredMaterialsWindow) component).refresh();
				});
			} else {
				ClientServerInterface.SendNotification.notifyRefreshWindows();
			}
		}
	}


	@Override
	public Container getContainerImpl() {
		return materialContainer;
	}


	@Override
	public boolean isLocked() {
		if (constructionProgress == 1f) {
			return getContainerImpl().isLocked();
		} else {
			return false;
		}
	}


	@Override
	public boolean isLockable() {
		if (constructionProgress == 1f) {
			return getContainerImpl().isLockable();
		} else {
			return false;
		}
	}


	@Override
	public boolean unlock(Item with) {
		if (constructionProgress == 1f) {
			return getContainerImpl().unlock(with);
		} else {
			return false;
		}
	}


	@Override
	public boolean lock(Item with) {
		if (constructionProgress == 1f) {
			return getContainerImpl().lock(with);
		} else {
			return false;
		}
	}
}