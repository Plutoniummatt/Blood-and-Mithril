package bloodandmithril.prop.construction;

import static bloodandmithril.networking.ClientServerInterface.client;
import static bloodandmithril.networking.ClientServerInterface.isServer;

import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.ai.task.construct.ConstructDeconstruct;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.event.events.ConstructionFinished;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.requests.RefreshWindowsResponse;
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

	/** True if this construction has been completed before */
	private boolean finishedConstruction = false;

	/**
	 * Constructor
	 */
	protected Construction(final float x, final float y, final int width, final int height, final boolean grounded, final float constructionRate, final SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf) {
		super(x, y, width, height, grounded, Depth.MIDDLEGROUND, canPlaceOnTopOf, true);
		this.constructionRate = constructionRate;
	}


	@Override
	public void render(final Graphics graphics) {
		internalRender(constructionProgress, graphics);
	}


	/**
	 * @return the info window of this construction
	 */
	public Window getInfoWindow() {
		return new MessageWindow(
			getDescription(),
			Color.YELLOW,
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
	public synchronized void construct(final Individual individual, final float time) {
		if (constructionProgress == 0f) {
			if (CraftingStation.enoughMaterialsToCraft(individual, getRequiredMaterials())) {
				for (final Entry<Item, Integer> requiredItem : getRequiredMaterials().entrySet()) {
					for (int i = requiredItem.getValue(); i > 0; i--) {
						individual.takeItem(requiredItem.getKey());
					}
				}
			}

			if (ClientServerInterface.isClient()) {
				Wiring.injector().getInstance(UserInterface.class).refreshRefreshableWindows();
			} else {
				ClientServerInterface.sendNotification(-1, true, true,
					new SynchronizePropRequest.SynchronizePropResponse(this),
					new SynchronizeIndividual.SynchronizeIndividualResponse(individual.getId().getId(), System.currentTimeMillis()),
					new RefreshWindowsResponse()
				);
			}
		}

		if (constructionProgress >= 0.99f) {
			finishConstruction();
		} else {
			constructionProgress = constructionProgress + time * constructionRate >= 0.99f ? 0.99f : constructionProgress + time * constructionRate;
		}
	}


	/**
	 * Regresses the construction of this {@link Construction}, in time units measured in seconds
	 */
	public synchronized void deconstruct(final Individual individual, final float time) {
		if (canDeconstruct()) {
			if (constructionProgress <= 0f) {
				Domain.getWorld(getWorldId()).props().removeProp(id);

				if (finishedConstruction) {
					for (final Entry<Item, Integer> entry : getRequiredMaterials().entrySet()) {
						for (int i = entry.getValue(); i > 0; i--) {
							final Item item = entry.getKey();
							item.setWorldId(getWorldId());
							Domain.getWorld(getWorldId()).items().addItem(
								item.copy(),
								position.cpy().add(0, 10),
								new Vector2(0, 200).rotate(Util.getRandom().nextFloat() * 360f)
							);
						}
					}
				} else {
					for (final Entry<Item, Integer> entry : materialContainer.getInventory().entrySet()) {
						for (int i = entry.getValue(); i > 0; i--) {
							final Item item = entry.getKey();
							item.setWorldId(getWorldId());
							Domain.getWorld(getWorldId()).items().addItem(
								item.copy(),
								position.cpy().add(0, 10),
								new Vector2(0, 200).rotate(Util.getRandom().nextFloat() * 360f)
							);
						}
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
		finishedConstruction  = true;
		Domain.getWorld(getWorldId()).addEvent(new ConstructionFinished(this));
	}


	@Override
	public ContextMenu getContextMenu() {
		final GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);

		if (constructionProgress == 1f) {
			final MenuItem deconstruct = new MenuItem(
				requiresConstruction() ? "Deconstruct" : "Disassemble",
				() -> {
					if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
						final Individual selected = gameClientStateTracker.getSelectedIndividuals().iterator().next();
						if (isServer()) {
							selected.getAI().setCurrentTask(
								new ConstructDeconstruct(selected, this, isServer() ? 0 : client.getID())
							);
						} else {
							ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, id);
						}
					}
				},
				gameClientStateTracker.getSelectedIndividuals().size() == 1 ? Color.WHITE : Colors.UI_DARK_GRAY,
				gameClientStateTracker.getSelectedIndividuals().size() == 1 ? Color.GREEN : Colors.UI_DARK_GRAY,
				gameClientStateTracker.getSelectedIndividuals().size() == 1 ? Color.GRAY : Colors.UI_DARK_GRAY,
				() -> {
					return new ContextMenu(0, 0, true, new MenuItem("You must select a single individual", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
				},
				() -> {
					return gameClientStateTracker.getSelectedIndividuals().size() != 1;
				}
			);

			final ContextMenu completedContextMenu = getCompletedContextMenu();
			completedContextMenu.addMenuItem(deconstruct);
			return completedContextMenu;
		} else {
			final ContextMenu menu = new ContextMenu(0, 0, true);

			final MenuItem openTransferItemsWindow = new MenuItem(
				requiresConstruction() ? "Construct" : "Assemble",
				() -> {
					if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
						final Individual selected = gameClientStateTracker.getSelectedIndividuals().iterator().next();
						if (isServer()) {
							selected.getAI().setCurrentTask(
								new ConstructDeconstruct(selected, this, isServer() ? 0 : client.getID())
							);
						} else {
							ClientServerInterface.SendRequest.sendTradeWithPropRequest(selected, id);
						}
					}
				},
				gameClientStateTracker.getSelectedIndividuals().size() == 1 ? Color.WHITE : Colors.UI_DARK_GRAY,
				gameClientStateTracker.getSelectedIndividuals().size() == 1 ? Color.GREEN : Colors.UI_DARK_GRAY,
				gameClientStateTracker.getSelectedIndividuals().size() == 1 ? Color.GRAY : Colors.UI_DARK_GRAY,
				() -> {
					return new ContextMenu(0, 0, true, new MenuItem("You must select a single individual", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
				},
				() -> {
					return gameClientStateTracker.getSelectedIndividuals().size() != 1;
				}
			);

			final MenuItem cancel = new MenuItem(
				"Cancel",
				() -> {
					if (getConstructionProgress() == 0f) {
						Domain.getWorld(getWorldId()).props().removeProp(id);
					}
				},
				getConstructionProgress() != 0f ? Colors.UI_DARK_GRAY : Color.WHITE,
				getConstructionProgress() != 0f ? Colors.UI_DARK_GRAY : Color.GREEN,
				getConstructionProgress() != 0f ? Colors.UI_DARK_GRAY : Color.GRAY,
				() -> {
					return new ContextMenu(0, 0, true, new MenuItem("Construction has already been started", () -> {}, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, Colors.UI_DARK_GRAY, null));
				},
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
	public void setConstructionProgress(final float constructionProgress) {
		this.constructionProgress = constructionProgress;
	}


	@Override
	public String getContextMenuItemLabel() {
		final String progress = requiresConstruction() ? "Under construction" : "Incomplete";
		return getTitle() + (constructionProgress == 1f ? "" : " - " + progress + " (" + String.format("%.1f", constructionProgress * 100) + "%)");
	}

	@Override
	public String getTitle() {
		return getClass().getAnnotation(Name.class).name();
	}

	/** Renders this {@link Construction} based on {@link #constructionProgress} */
	protected abstract void internalRender(float constructionProgress, Graphics graphics);

	/** Get the required items to construct this {@link Construction} */
	public abstract Map<Item, Integer> getRequiredMaterials();

	/** Get the context menu that will be displayed once this {@link Construction} has finished being constructing */
	protected abstract ContextMenu getCompletedContextMenu();

	/** Whether this {@link Construction} can be deconstructed */
	public abstract boolean canDeconstruct();

	@Override
	public void synchronizeContainer(final Container other) {
		materialContainer.synchronizeContainer(other);
	}


	public void synchronizeConstruction(final Construction other) {
		constructionProgress = other.getConstructionProgress();
		finishedConstruction = other.finishedConstruction;
	}


	@Override
	public void giveItem(final Item item) {
		if (constructionProgress == 1f) {
			getContainerImpl().giveItem(item);
		} else {
			materialContainer.giveItem(item);
			if (ClientServerInterface.isClient()) {
				Wiring.injector().getInstance(UserInterface.class).getLayeredComponents().stream().filter((component) -> {
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
	public boolean unlock(final Item with) {
		if (constructionProgress == 1f) {
			return getContainerImpl().unlock(with);
		} else {
			return false;
		}
	}


	@Override
	public boolean lock(final Item with) {
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