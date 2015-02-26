package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.ui.UserInterface.refreshRefreshableWindows;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Fuel;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.container.ContainerImpl;
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.material.Material;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.CraftingStationWindow;
import bloodandmithril.ui.components.window.FueledCraftingWindow;
import bloodandmithril.ui.components.window.MessageWindow;

import com.badlogic.gdx.graphics.Color;

/**
 * A Furnace
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class FueledCraftingStation extends CraftingStation implements Container {
	private static final long serialVersionUID = 7693386784097531328L;

	/** The duration which this furnace will combust/smelt, in seconds */
	private float combustionDurationRemaining;

	/** True if burning */
	private boolean burning;

	/** The {@link Container} of this {@link FueledCraftingStation} */
	private ContainerImpl fuel;

	/**
	 * Constructor
	 */
	public FueledCraftingStation(float x, float y, int width, int height, float constructionRate, ContainerImpl fuelContainer) {
		super(x, y, width, height, constructionRate);
		this.fuel = fuelContainer;
	}


	@Override
	public void synchronizeProp(Prop other) {
		super.synchronizeProp(other);
		if (other instanceof FueledCraftingStation) {
			this.fuel.synchronizeContainer(((FueledCraftingStation)other).fuel);
			this.burning = ((FueledCraftingStation) other).burning;
			this.combustionDurationRemaining = ((FueledCraftingStation) other).combustionDurationRemaining;
		} else {
			throw new RuntimeException("Can not synchronize FueledCraftingStation with " + other.getClass().getSimpleName());
		}
	}


	/**
	 * Ignites this furnace
	 */
	public synchronized void ignite() {
		if (burning) {
			return;
		}

		this.combustionDurationRemaining = calculateCurrentCombutionDuration();
		if (combustionDurationRemaining > 0f) {
			burning = true;
		}
	}


	public synchronized float getCombustionDurationRemaining() {
		return combustionDurationRemaining;
	}


	public synchronized void setCombustionDurationRemaining(float combustionDurationRemaining) {
		this.combustionDurationRemaining = combustionDurationRemaining;
	}


	public float calculateCurrentCombutionDuration() {
		float finalDuration = 0f;
		for (Entry<Item, Integer> entry : fuel.getInventory().entrySet()) {
			Item item = entry.getKey();
			if (item instanceof Fuel) {
				finalDuration = finalDuration + ((Fuel) item).getCombustionDuration() * entry.getValue();
			}
		}

		return finalDuration;
	}


	public boolean isBurning() {
		return burning;
	}


	@Override
	public synchronized void update(float delta) {
		super.update(delta);

		if (burning) {
			synchronized (this) {
				this.combustionDurationRemaining -= delta;

				if (this.combustionDurationRemaining <= 0f) {
					burning = false;
					combustFuel();
					if (!isClient()) {
					} else {
						refreshRefreshableWindows();
					}
				}
			}
		}
	}


	/**
	 * Transforms all items in the {@link FueledCraftingStation} according to {@link Item#combust(float, float)}
	 */
	private synchronized void combustFuel() {
		synchronized(fuel) {
			Map<Item, Integer> existing = newHashMap(fuel.getInventory());
			fuel.getInventory().clear();


			for (Entry<Item, Integer> entry : existing.entrySet()) {
				for (int i = 0; i < entry.getValue(); i++) {
					if (entry.getKey() instanceof Fuel) {
						fuel.giveItem(((Fuel) entry.getKey()).consume());
					}

					if (entry.getKey() instanceof Rock && Fuel.class.isAssignableFrom(((Rock) entry.getKey()).getMineral())) {
						fuel.giveItem(((Fuel) Material.getMaterial(((Rock) entry.getKey()).getMineral())).consume());
					}
				}
			}
		}
	}


	@Override
	protected ContextMenu getCompletedContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY(), true,
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
						new MessageWindow(
							getDescription(),
							Color.ORANGE,
							BloodAndMithrilClient.WIDTH/2 - 175,
							BloodAndMithrilClient.HEIGHT/2 + 100,
							350,
							200,
							"Furnace",
							true,
							350,
							200
						)
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		addCraftMenuItem(menu);
		return menu;
	}


	@Override
	public void giveItem(Item item) {
		if (getConstructionProgress() == 1f) {
			fuel.giveItem(item);
			addToFuelDuration(item);
		} else {
			super.giveItem(item);
		}
	}


	protected abstract void addToFuelDuration(Item item);


	@Override
	public Container getContainerImpl() {
		if (getConstructionProgress() == 1f) {
			return fuel;
		} else {
			return super.getContainerImpl();
		}
	}


	@Override
	public boolean customCanCraft() {
		return isBurning();
	}


	@Override
	public boolean isLocked() {
		return false;
	}


	@Override
	public boolean unlock(Item with) {
		return false;
	}


	@Override
	public boolean lock(Item with) {
		return false;
	}


	@Override
	public boolean isLockable() {
		return false;
	}


	@Override
	public CraftingStationWindow getCraftingStationWindow(Individual individual) {
		return new FueledCraftingWindow(
			BloodAndMithrilClient.WIDTH/2 - 375,
			BloodAndMithrilClient.HEIGHT/2 + 150,
			individual.getId().getFirstName() + " interacting with " + getTitle(),
			individual,
			this
		);
	}


	@Override
	public boolean canDeconstruct() {
		return !burning && fuel.isEmpty();
	}


	public void extinguish() {
		this.burning = false;
	}


	public abstract boolean isValidFuel(Item item);
}