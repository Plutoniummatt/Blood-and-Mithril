package bloodandmithril.prop.building;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.csi.ClientServerInterface.isClient;
import static bloodandmithril.ui.UserInterface.refreshRefreshableWindows;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.AddLightRequest;
import bloodandmithril.csi.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.csi.requests.SynchronizePropRequest;
import bloodandmithril.graphics.Light;
import bloodandmithril.item.Container;
import bloodandmithril.item.ContainerImpl;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.brick.YellowBrick;
import bloodandmithril.item.material.fuel.Coal;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * A Furnace
 *
 * @author Matt
 */
public class Furnace extends Construction implements Container {
	private static final long serialVersionUID = 7693386784097531328L;

	/** {@link TextureRegion} of the {@link Furnace} */
	public static TextureRegion FURANCE, FURNACE_BURNING;

	/** The amount of time taken to smelt one batch of material, in seconds */
	public static final float SMELTING_DURATION = 10f;

	/** The heat level of {@link Furnace}s */
	private static final int HEAT_LEVEL = 2500;

	/** The duration which this furnace will combust/smelt, in seconds */
	private float combustionDurationRemaining, smeltingDurationRemaining;

	/** The {@link Light} that will be rendered if this {@link Furnace} is lit */
	private Light light;

	/** The ID of the {@link Light} that will be rendered if this {@link Furnace} is lit */
	private int lightId;

	/** True if burning */
	private boolean burning, smelting;

	/** The {@link Container} of this {@link Furnace} */
	private ContainerImpl container;

	/**
	 * Constructor
	 */
	public Furnace(float x, float y) {
		super(x, y, 49, 76, false, 0.1f);
		this.container = new ContainerImpl(500f, true);
	}


	@Override
	public void synchronizeProp(Prop other) {
		if (other instanceof Furnace) {
			this.container.synchronizeContainer(((Furnace)other).container);
			this.burning = ((Furnace) other).burning;
			this.combustionDurationRemaining = ((Furnace) other).combustionDurationRemaining;
			this.smeltingDurationRemaining = ((Furnace) other).smeltingDurationRemaining;
			this.lightId = ((Furnace) other).lightId;
			this.light = Domain.getLights().get(((Furnace) other).lightId);
			this.smelting = ((Furnace) other).smelting;
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

		lightId = ParameterPersistenceService.getParameters().getNextLightId();
		light = new Light(500, position.x, position.y + 4, Color.ORANGE, 1f, 0f, 1f);
		Domain.getLights().put(lightId, light);

		smelt();
	}


	/**
	 * Begin smelting
	 */
	public synchronized void smelt() {
		if (smelting) {
			return;
		}

		if (!container.getInventory().isEmpty()) {
			Optional<Item> notCoal = Iterables.tryFind(container.getInventory().keySet(), new Predicate<Item>() {
				@Override
				public boolean apply(Item item) {
					return !(item instanceof Coal);
				}
			});

			if (notCoal.isPresent()) {
				smelting = true;
				smeltingDurationRemaining = SMELTING_DURATION;
			}
		}
	}


	@Override
	protected void internalRender(float constructionProgress) {
		if (burning && light != null) {
			float intensity = 0.75f + 0.25f * Util.getRandom().nextFloat();
			light.intensity = intensity;
		}

		if (burning) {
			spriteBatch.draw(FURNACE_BURNING, position.x - width / 2, position.y);
		} else {
			spriteBatch.draw(FURANCE, position.x - width / 2, position.y);
		}
	}


	public float getCombustionDurationRemaining() {
		return combustionDurationRemaining;
	}


	public float getSmeltingDurationRemaining() {
		return smeltingDurationRemaining;
	}


	public synchronized void setCombustionDurationRemaining(float combustionDurationRemaining) {
		synchronized (this) {
			this.combustionDurationRemaining = combustionDurationRemaining;
		}
	}


	public boolean isBurning() {
		return burning;
	}


	@Override
	public synchronized void update(float delta) {
		if (burning) {
			synchronized (this) {
				this.combustionDurationRemaining -= delta;

				if (smelting) {
					smeltingDurationRemaining -= delta;
				}

				if (this.smeltingDurationRemaining <= 0f && smelting) {
					smeltItems();
					smelting = false;
					if (!isClient()) {
						ClientServerInterface.sendNotification(
							-1,
							true,
							true,
							new SynchronizePropRequest.SynchronizePropResponse(this),
							new RefreshWindowsResponse()
						);
					} else {
						refreshRefreshableWindows();
					}
				}

				if (this.combustionDurationRemaining <= 0f) {
					burning = false;
					smelting = false;
					smeltItems();
					Domain.getLights().remove(lightId);
					if (!isClient()) {
						ClientServerInterface.sendNotification(
							-1,
							true,
							true,
							new AddLightRequest.RemoveLightNotification(lightId),
							new SynchronizePropRequest.SynchronizePropResponse(this),
							new RefreshWindowsResponse()
						);
					} else {
						refreshRefreshableWindows();
					}
				}
			}
		}
	}


	/**
	 * Transmutes all items in the {@link Furnace} according to {@link Item#combust(float, float)}
	 */
	private synchronized void smeltItems() {
		synchronized(container) {
			Map<Item, Integer> existing = newHashMap(container.getInventory());
			container.getInventory().clear();


			for (Entry<Item, Integer> entry : existing.entrySet()) {
				for (int i = 0; i < entry.getValue(); i++) {
					if (entry.getKey() instanceof Coal) {
						if (isBurning()) {
							container.giveItem(new Coal());
						} else {
							container.giveItem(entry.getKey().combust(HEAT_LEVEL, existing));
						}
					} else {
						if (smelting) {
							container.giveItem(entry.getKey().combust(HEAT_LEVEL, existing));
						} else {
							container.giveItem(entry.getKey());
						}
					}
				}
			}
		}
	}


	public boolean isSmelting() {
		return smelting;
	}


	@Override
	protected ContextMenu getCompletedContextMenu() {
		ContextMenu menu = new ContextMenu(BloodAndMithrilClient.getMouseScreenX(), BloodAndMithrilClient.getMouseScreenY(),
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
						new MessageWindow(
							"A furnace, able to achieve temperatures hot enough to melt most metals",
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

		if (Domain.getSelectedIndividuals().size() > 0) {
			final Individual selected = Domain.getSelectedIndividuals().iterator().next();
			MenuItem openChestMenuItem = new MenuItem(
				"Open furnace",
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
		}

		return menu;
	}


	@Override
	protected Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> requiredItems = newHashMap();

		requiredItems.put(new YellowBrick(), 5);

		return requiredItems;
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
			if (item instanceof Coal) {
				if (burning) {
					this.combustionDurationRemaining += ((Coal) item).getCombustionDuration();
				}
			}
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
	public int has(Item item) {
		if (getConstructionProgress() == 1f) {
			return container.has(item);
		} else {
			return super.has(item);
		}
	}
}