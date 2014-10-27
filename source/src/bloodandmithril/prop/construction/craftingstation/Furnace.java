package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.item.items.material.Ingot.ingot;
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
import bloodandmithril.item.items.container.GlassBottle;
import bloodandmithril.item.items.material.Brick;
import bloodandmithril.item.items.material.Glass;
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.CraftingStationWindow;
import bloodandmithril.ui.components.window.FurnaceCraftingWindow;
import bloodandmithril.ui.components.window.MessageWindow;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * A Furnace
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Furnace extends CraftingStation implements Container {
	private static final long serialVersionUID = 7693386784097531328L;

	/** {@link TextureRegion} of the {@link Furnace} */
	public static TextureRegion FURANCE, FURNACE_BURNING;

	/** The amount of time taken to smelt one batch of material, in seconds */
	public static final float SMELTING_DURATION = 10f;

	/** The duration which this furnace will combust/smelt, in seconds */
	private float combustionDurationRemaining;

	/** True if burning */
	private boolean burning;

	/** The {@link Container} of this {@link Furnace} */
	private ContainerImpl container;

	private static final Map<Item, Integer> craftables = Maps.newHashMap();

	static {
		craftables.put(new Glass(), 1);
		craftables.put(new GlassBottle(newHashMap()), 3);
		craftables.put(ingot(Iron.class), 1);
		craftables.put(ingot(Steel.class), 1);
	}

	/**
	 * Constructor
	 */
	public Furnace(float x, float y) {
		super(x, y, 49, 76, 0.1f);
		this.container = new ContainerImpl(500f, 300);
	}


	@Override
	public void synchronizeProp(Prop other) {
		if (other instanceof Furnace) {
			this.container.synchronizeContainer(((Furnace)other).container);
			this.burning = ((Furnace) other).burning;
			this.combustionDurationRemaining = ((Furnace) other).combustionDurationRemaining;
		} else {
			throw new RuntimeException("Can not synchronize Furnace with " + other.getClass().getSimpleName());
		}
	}


	/**
	 * Ignites this furnace
	 */
	public synchronized void ignite() {
		if (burning) {
			return;
		}

		burning = true;
	}


	@Override
	protected void internalRender(float constructionProgress) {
		if (burning) {
			spriteBatch.draw(FURNACE_BURNING, position.x - width / 2, position.y);
		} else {
			spriteBatch.draw(FURANCE, position.x - width / 2, position.y);
		}
	}


	public synchronized float getCombustionDurationRemaining() {
		return combustionDurationRemaining;
	}


	public synchronized void setCombustionDurationRemaining(float combustionDurationRemaining) {
		this.combustionDurationRemaining = combustionDurationRemaining;
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
	 * Transforms all items in the {@link Furnace} according to {@link Item#combust(float, float)}
	 */
	private synchronized void combustFuel() {
		synchronized(container) {
			Map<Item, Integer> existing = newHashMap(container.getInventory());
			container.getInventory().clear();


			for (Entry<Item, Integer> entry : existing.entrySet()) {
				for (int i = 0; i < entry.getValue(); i++) {
					if (entry.getKey() instanceof Fuel) {
						container.giveItem(((Fuel) entry.getKey()).consume());
					}

					if (entry.getKey() instanceof Rock && Fuel.class.isAssignableFrom(((Rock) entry.getKey()).getMineral())) {
						container.giveItem(((Fuel) Material.getMaterial(((Rock) entry.getKey()).getMineral())).consume());
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
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> requiredItems = newHashMap();
		requiredItems.put(new Brick(), 5);
		return requiredItems;
	}


	@Override
	public void giveItem(Item item) {
		if (getConstructionProgress() == 1f) {
			container.giveItem(item);
			if (item instanceof Rock && ((Rock)item).getMineral().equals(Coal.class)) {
				if (burning) {
					this.combustionDurationRemaining += Material.getMaterial(Coal.class).getCombustionDuration();
				}
			}
		} else {
			super.giveItem(item);
		}
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
	public String getCustomMessage() {
		return "Furnace is not ignited";
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
	public String getTitle() {
		return "Furnace";
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	public String getDescription() {
		return "A furnace, able to achieve temperatures hot enough to melt most metals";
	}


	@Override
	public String getAction() {
		return "Craft";
	}


	@Override
	public Map<Item, Integer> getCraftables() {
		return craftables;
	}


	@Override
	public CraftingStationWindow getCraftingStationWindow(Individual individual) {
		return new FurnaceCraftingWindow(
			BloodAndMithrilClient.WIDTH/2 - 375,
			BloodAndMithrilClient.HEIGHT/2 + 150,
			individual.getId().getFirstName() + " interacting with " + getTitle(),
			individual,
			this
		);
	}
}