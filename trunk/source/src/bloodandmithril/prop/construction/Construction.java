package bloodandmithril.prop.construction;

import static bloodandmithril.networking.ClientServerInterface.client;
import static bloodandmithril.networking.ClientServerInterface.isServer;

import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.ai.task.ConstructDeconstruct;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.event.events.ConstructionFinished;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.networking.requests.SynchronizeIndividual;
import bloodandmithril.networking.requests.SynchronizePropRequest;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.CraftingStation;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.RequiredMaterialsWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * A Construction
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Construction extends Prop implements Container {
	private static final long serialVersionUID = -7772373095960462479L;

	/** Current construction progress, 1f means fully constructed */
	private float constructionProgress;

	/** The rate at which this {@link Construction} is constructed, in units of /s */
	public final float constructionRate;

	/** The container used to store construction materials during the construction stage */
	private ContainerImpl materialContainer = new ContainerImpl(10000000, 10000000);

	/**
	 * Constructor
	 */
	protected Construction(float x, float y, int width, int height, boolean grounded, float constructionRate, SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf) {
		super(x, y, width, height, grounded, Depth.MIDDLEGROUND, canPlaceOnTopOf, true);
		this.constructionRate = constructionRate;
	}


	@Override
	public void render() {
		internalRender(constructionProgress);
	}


	/**
	 * @return the info window of this construction
	 */
	public Window getInfoWindow() {
		return new MessageWindow(
			getDescription(),
			Color.YELLOW,
			BloodAndMithrilClient.WIDTH/2 - 175,
			BloodAndMithrilClient.HEIGHT/2 + 100,
			450,
			250,
			getTitle(),
			true,
			100,
			100
		);
	}


	public abstract String getDescription();


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
	 * Regresses the construction of this {@link Construction}, in time units measured in seconds
	 */
	public synchronized void deconstruct(Individual individual, float time) {
		if (canDeconstruct()) {
			if (constructionProgress <= 0f) {
				Domain.getWorld(getWorldId()).props().removeProp(id);
				for (Entry<Item, Integer> entry : getRequiredMaterials().entrySet()) {
					for (int i = entry.getValue(); i > 0; i--) {
						Item item = entry.getKey();
						item.setWorldId(getWorldId());
						Domain.getWorld(getWorldId()).items().addItem(
							item.copy(),
							position.cpy().add(0, 10),
							new Vector2(0, 200).rotate(Util.getRandom().nextFloat() * 360f)
						);
					}
				}
			} else {
				constructionProgress = constructionProgress - time * constructionRate <= 0f ? 0f : constructionProgress - time * constructionRate;
			}
		}
	}


	/**
	 * Finalise the construction
	 */
	private void finishConstruction() {
		constructionProgress = 1f;
		materialContainer.getInventory().clear();
		Domain.getWorld(getWorldId()).addEvent(new ConstructionFinished(this));
	}


	@Override
	public ContextMenu getContextMenu() {
		if (constructionProgress == 1f) {
			MenuItem deconstruct = new MenuItem(
				requiresConstruction() ? "Deconstruct" : "Disassemble",
				() -> {
					if (Domain.getSelectedIndividuals().size() == 1) {
						Individual selected = Domain.getSelectedIndividuals().iterator().next();
						if (isServer()) {
							selected.getAI().setCurrentTask(
								new ConstructDeconstruct(selected, this, isServer() ? 0 : client.getID())
							);
						} else {
							ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, id);
						}
					}
				},
				Domain.getSelectedIndividuals().size() == 1 ? Color.WHITE : Colors.UI_DARK_GRAY,
				Domain.getSelectedIndividuals().size() == 1 ? Color.GREEN : Colors.UI_DARK_GRAY,
				Domain.getSelectedIndividuals().size() == 1 ? Color.GRAY : Colors.UI_DARK_GRAY,
				new ContextMenu(0, 0, true, new MenuItem("You must select a single individual", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null)),
				() -> {
					return Domain.getSelectedIndividuals().size() != 1;
				}
			);

			ContextMenu completedContextMenu = getCompletedContextMenu();
			completedContextMenu.addMenuItem(deconstruct);
			return completedContextMenu;
		} else {
			ContextMenu menu = new ContextMenu(0, 0, true);

			MenuItem openTransferItemsWindow = new MenuItem(
				requiresConstruction() ? "Construct" : "Assemble",
				() -> {
					if (Domain.getSelectedIndividuals().size() == 1) {
						Individual selected = Domain.getSelectedIndividuals().iterator().next();
						if (isServer()) {
							selected.getAI().setCurrentTask(
								new ConstructDeconstruct(selected, this, isServer() ? 0 : client.getID())
							);
						} else {
							ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, id);
						}
					}
				},
				Domain.getSelectedIndividuals().size() == 1 ? Color.WHITE : Colors.UI_DARK_GRAY,
				Domain.getSelectedIndividuals().size() == 1 ? Color.GREEN : Colors.UI_DARK_GRAY,
				Domain.getSelectedIndividuals().size() == 1 ? Color.GRAY : Colors.UI_DARK_GRAY,
				new ContextMenu(0, 0, true, new MenuItem("You must select a single individual", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null)),
				() -> {
					return Domain.getSelectedIndividuals().size() != 1;
				}
			);

			MenuItem cancel = new MenuItem(
				"Cancel",
				() -> {
					if (getConstructionProgress() == 0f) {
						Domain.getWorld(getWorldId()).props().removeProp(id);
					}
				},
				getConstructionProgress() != 0f ? Colors.UI_DARK_GRAY : Color.WHITE,
				getConstructionProgress() != 0f ? Colors.UI_DARK_GRAY : Color.GREEN,
				getConstructionProgress() != 0f ? Colors.UI_DARK_GRAY : Color.GRAY,
				new ContextMenu(0, 0, true, new MenuItem("Construction has already been started", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null)),
				() -> {
					return getConstructionProgress() != 0f;
				}
			);

			menu.addMenuItem(openTransferItemsWindow);
			menu.addMenuItem(cancel);
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
		String progress = requiresConstruction() ? "Under construction" : "Incomplete";
		return getTitle() + (constructionProgress == 1f ? "" : " - " + progress + " (" + String.format("%.1f", constructionProgress * 100) + "%)");
	}

	@Override
	public String getTitle() {
		return getClass().getAnnotation(Name.class).name();
	}

	/** Renders this {@link Construction} based on {@link #constructionProgress} */
	protected abstract void internalRender(float constructionProgress);

	/** Get the required items to construct this {@link Construction} */
	public abstract Map<Item, Integer> getRequiredMaterials();

	/** Get the context menu that will be displayed once this {@link Construction} has finished being constructing */
	protected abstract ContextMenu getCompletedContextMenu();

	/** Whether this {@link Construction} can be deconstructed */
	public abstract boolean canDeconstruct();

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
				UserInterface.getLayeredComponents().stream().filter((component) -> {
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


	@Override
	public boolean isEmpty() {
		if (constructionProgress == 1f) {
			return getContainerImpl().isEmpty();
		} else {
			return materialContainer.isEmpty();
		}
	}


	/**
	 * @return true if this actually requires contructions, rather than mere placement
	 */
	public abstract boolean requiresConstruction();
}