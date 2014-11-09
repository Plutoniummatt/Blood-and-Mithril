package bloodandmithril.prop.construction;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
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
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;
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

	/** Returns whether this {@link Construction} can be built on a tile type */
	private final SerializableMappingFunction<Tile, Boolean> canBuildOnTopOf;

	/**
	 * Constructor
	 */
	protected Construction(float x, float y, int width, int height, boolean grounded, float constructionRate, SerializableMappingFunction<Tile, Boolean> canBuildOnTopOf) {
		super(x, y, width, height, grounded, Depth.MIDDLEGROUND);
		this.constructionRate = constructionRate;
		this.canBuildOnTopOf = canBuildOnTopOf;
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
	 * Finalise the construction
	 */
	private void finishConstruction() {
		constructionProgress = 1f;
		materialContainer.getInventory().clear();
	}


	@Override
	public ContextMenu getContextMenu() {
		if (constructionProgress == 1f) {
			return getCompletedContextMenu();
		} else {
			ContextMenu menu = new ContextMenu(0, 0, true);

			MenuItem openTransferItemsWindow = new MenuItem(
				"Construct",
				() -> {
					if (Domain.getSelectedIndividuals().size() == 1) {
						Individual selected = Domain.getSelectedIndividuals().iterator().next();
						if (ClientServerInterface.isServer()) {
							selected.getAI().setCurrentTask(
								new TradeWith(selected, this)
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

			menu.addMenuItem(openTransferItemsWindow);
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


	/**
	 * @return whether this construction can be built at this constructions location
	 */
	public boolean canBuildAtCurrentPosition() {
		return canBuildAt(position);
	}


	/**
	 * @return whether this construction can be built at a given location
	 */
	public boolean canBuildAt(Vector2 position) {
		return canBuildAt(position.x, position.y);
	}


	/**
	 * @return whether this construction can be built at a given location
	 */
	public boolean canBuildAt(float x, float y) {
		float xStep = (float)width / (float)TILE_SIZE;
		long xSteps = Math.round(Math.ceil(xStep));
		float xIncrement = (float)width / (float)xSteps;

		float yStep = (float)height / (float)TILE_SIZE;
		long ySteps = Math.round(Math.ceil(yStep));
		float yIncrement = (float)height / (float)ySteps;


		for (int i = 0; i <= xSteps; i++) {
			Tile tileUnder = Domain.getActiveWorld().getTopography().getTile(x - width / 2 + i * xIncrement, y - TILE_SIZE/2, true);
			if (tileUnder.isPassable() || canBuildOnTopOf != null && !canBuildOnTopOf.apply(tileUnder)) {
				return false;
			}

			for (int j = 1; j <= ySteps; j++) {
				Tile tileOverlapping = Domain.getActiveWorld().getTopography().getTile(x - width / 2 + i * xIncrement, y + j * yIncrement - TILE_SIZE/2, true);
				if (!tileOverlapping.isPassable()) {
					return false;
				}
			}
		}

		for (Integer propId : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Prop.class, x, y)) {
			Prop prop = Domain.getProp(propId);
			if (prop instanceof Construction && Domain.getActiveWorld().getProps().contains(propId)) {
				if (this.overlapsWith(prop)) {
					return false;
				}
			}
		}

		return true;
	}


	public boolean overlapsWith(Prop other) {
		float left = position.x - width/2;
		float right = position.x + width/2;
		float top = position.y + height;
		float bottom = position.y;

		float otherLeft = other.position.x - other.width/2;
		float otherRight = other.position.x + other.width/2;
		float otherTop = other.position.y + other.height;
		float otherBottom = other.position.y;

		return
			!(left >= otherRight) &&
			!(right <= otherLeft) &&
			!(top <= otherBottom) &&
			!(bottom >= otherTop);
	}


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