package bloodandmithril.prop.construction.craftingstation;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.core.BloodAndMithrilClient.getGraphics;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.task.Craft;
import bloodandmithril.character.ai.task.OpenCraftingStation;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.requests.RefreshWindows.RefreshWindowsResponse;
import bloodandmithril.networking.requests.SynchronizePropRequest;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.CraftingStationWindow;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.util.Shaders;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.SerializableDoubleWrapper;
import bloodandmithril.world.Domain;

/**
 * Superclass for all {@link Prop}s that craft {@link Item}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class CraftingStation extends Construction {
	private static final long serialVersionUID = 2177296386331588828L;

	private float craftingProgress;
	private SerializableDoubleWrapper<Item, Integer> currentlyBeingCrafted;
	private Integer occupiedBy;
	private boolean finished;

	/**
	 * Constructor
	 */
	protected CraftingStation(float x, float y, int width, int height, float constructionRate) {
		super(x, y, width, height, true, constructionRate, null);
	}


	/** Returns the {@link TextureRegion} of the {@link CraftingStation} */
	protected abstract TextureRegion getTextureRegion();

	/** Returns the verb that describes the action of this {@link CraftingStation} */
	public abstract String getAction();

	/** Returns the list of {@link Craftable} {@link Item}s */
	public abstract Map<Item, Integer> getCraftables();


	/**
	 * @return true by default, override for custom functions
	 */
	public boolean customCanCraft() {
		return true;
	}


	@Override
	public Color getContextMenuColor() {
		return Color.CYAN;
	}


	/**
	 * @return The string message to display indicating the reason why something can not be crafted
	 */
	public String getCustomMessage() {
		return "";
	}


	@Override
	protected void internalRender(float constructionProgress) {
		if (constructionProgress == 0f) {
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, 0.90f);
		}
		getGraphics().getSpriteBatch().draw(getTextureRegion(), position.x - width / 2, position.y);
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		return newHashMap();
	}


	@Override
	protected ContextMenu getCompletedContextMenu() {
		ContextMenu menu = new ContextMenu(getMouseScreenX(), getMouseScreenY(), true);

		menu.addMenuItem(
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
						new MessageWindow(
							getDescription(),
							Color.ORANGE,
							500,
							250,
							getTitle(),
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

		addCraftMenuItem(menu);

		return menu;
	}


	protected void addCraftMenuItem(ContextMenu menu) {
		if (Domain.getSelectedIndividuals().size() > 0) {
			final Individual selected = Domain.getSelectedIndividuals().iterator().next();
			menu.addMenuItem(
				new MenuItem(
					getAction(),
					() -> {
						if (Domain.getSelectedIndividuals().size() == 1) {
							AITask currentTask = selected.getAI().getCurrentTask();
							if (currentTask instanceof Craft && ((Craft)currentTask).getCraftingStationId() == id) {
								OpenCraftingStation.openCraftingStationWindow(selected, this);
								return;
							}

							if (ClientServerInterface.isServer()) {
								selected.getAI().setCurrentTask(new OpenCraftingStation(selected, this));
							} else {
								ClientServerInterface.SendRequest.sendOpenCraftingStationRequest(selected, this);
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
				)
			);
		}
	}


	@Override
	public void synchronizeProp(Prop other) {
		this.craftingProgress = ((CraftingStation)other).craftingProgress;
		this.currentlyBeingCrafted = ((CraftingStation)other).currentlyBeingCrafted;
		this.finished = ((CraftingStation)other).finished;
		this.occupiedBy = ((CraftingStation)other).occupiedBy;
	}


	@Override
	public void update(float delta) {
		if (currentlyBeingCrafted != null && occupiedBy != null) {
			occupiedBy = Domain.getIndividual(occupiedBy).getAI().getCurrentTask() instanceof Craft ? occupiedBy : null;
		}
	}


	public float getCraftingProgress() {
		return craftingProgress;
	}


	public SerializableDoubleWrapper<Item, Integer> getCurrentlyBeingCrafted() {
		return currentlyBeingCrafted;
	}


	public void setCurrentlyBeingCrafted(SerializableDoubleWrapper<Item, Integer> currentlyBeingCrafted) {
		this.currentlyBeingCrafted = currentlyBeingCrafted;
	}


	/** Affects the individual using this {@link CraftingStation} */
	public abstract void affectIndividual(Individual individual, float delta);


	/**
	 * Make progress on the crafting of some {@link Craftable}
	 *
	 * @return whether or not the crafting task should continue.
	 */
	public synchronized boolean craft(SerializableDoubleWrapper<Item, Integer> item, Individual individual, float aiTaskDelay) {
		if (!customCanCraft()) {
			return false;
		}

		if (occupiedBy == null) {
			occupiedBy = individual.getId().getId();
		}

		if (currentlyBeingCrafted == null) {
			currentlyBeingCrafted = item;

			if (enoughMaterialsToCraft(individual, ((Craftable)item.t).getRequiredMaterials())) {
				for (Entry<Item, Integer> requiredItem : ((Craftable)item.t).getRequiredMaterials().entrySet()) {
					for (int i = requiredItem.getValue(); i > 0; i--) {
						individual.takeItem(requiredItem.getKey());
					}
				}

				SoundService.play(getCraftingSound(), position, true, Visible.getVisible(this));
			}

			if (ClientServerInterface.isClient()) {
				UserInterface.refreshRefreshableWindows();
			} else {
				ClientServerInterface.sendNotification(-1, true, true,
					new SynchronizePropRequest.SynchronizePropResponse(this),
					new RefreshWindowsResponse()
				);
			}
		}

		if (occupiedBy == individual.getId().getId()) {
			craftingProgress += aiTaskDelay / ((Craftable)currentlyBeingCrafted.t).getCraftingDuration();
			if (item.t instanceof Craftable) {
				((Craftable) item.t).crafterEffects(individual, aiTaskDelay);
			}
			affectIndividual(individual, aiTaskDelay);
			if (craftingProgress >= 1f) {
				craftingProgress = 1f;
				finished = true;
			}
			return true;
		}

		return false;
	}


	/**
	 * @return the ID of the sound that should be played when crafting
	 */
	protected abstract int getCraftingSound();


	/**
	 * @return whether the {@link Individual} has enough items to craft a {@link Craftable}
	 */
	public static boolean enoughMaterialsToCraft(Individual individual, Map<Item, Integer> requiredMaterials) {
		Map<Item, Integer> inventoryCopy = individual.getInventory();
		for (Entry<Item, Integer> requiredItem : requiredMaterials.entrySet()) {
			Optional<Entry<Item, Integer>> tryFind = Iterables.tryFind(inventoryCopy.entrySet(), toMatch -> {
				return toMatch.getKey().sameAs(requiredItem.getKey());
			});

			if (tryFind.isPresent()) {
				if (requiredItem.getValue() > tryFind.get().getValue()) {
					return false;
				}
			} else {
				return false;
			}
		}

		return true;
	}


	public boolean isFinished() {
		return finished;
	}


	/**
	 * Take the currently crafted item from this {@link CraftingStation}, if the inventory is full, it will be dropped instead
	 */
	public void takeItem(Individual individual) {
		if (currentlyBeingCrafted != null && finished) {
			for (int i = currentlyBeingCrafted.s; i > 0; i--) {
				if (individual.canReceive(currentlyBeingCrafted.t)) {
					individual.giveItem(currentlyBeingCrafted.t);
				} else {
					Item item = currentlyBeingCrafted.t.copy();
					Domain.getWorld(getWorldId()).items().addItem(
						item,
						position.cpy().add(0f, height / 2),
						new Vector2(25f, 0f).rotate(Util.getRandom().nextFloat() * 360f)
					);
				}
			}
			setCurrentlyBeingCrafted(null);
			craftingProgress = 0f;
			finished = false;
		}

		if (ClientServerInterface.isClient()) {
			UserInterface.refreshRefreshableWindows();
		} else {
			ClientServerInterface.SendNotification.notifyRefreshWindows();
		}
	}


	public boolean isOccupied() {
		return occupiedBy != null;
	}


	public CraftingStationWindow getCraftingStationWindow(Individual individual) {
		return new CraftingStationWindow(
			individual.getId().getFirstName() + " interacting with " + getTitle(),
			individual,
			this
		);
	}
}